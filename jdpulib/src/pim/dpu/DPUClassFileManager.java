package pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.BytesUtils;
import pim.UPMEM;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class DPUClassFileManager {

    static final int block = 1024;
    int dpuID;
    Dpu dpu;
    UPMEM upmem = UPMEM.getInstance();
    public DPUClassFileManager(int dpuID, Dpu dpu) {
        this.dpuID = dpuID;
        this.dpu = dpu;
    }

    private boolean isClassLoaded(Class c){
        return isClassLoaded(c.getName());
    }
    private boolean isClassLoaded(String className){
        className = className.replace(".", "/");
        DPUClassFileCacheItem item = upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
        return item != null;
    }
    private DPUClassFileCacheItem getLoadedClassRecord(String className){
        className = className.replace(".", "/");
        DPUClassFileCacheItem item = upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
        return item;
    }
    private DPUClassFileCacheItem getLoadedClassRecord(Class c){
       return getLoadedClassRecord(c.getName());
    }


    private DPUJClass loadClassIfNotLoaded(String className) throws ClassNotFoundException, DpuException, IOException {
        className = className.replace(".", "/");
        DPUClassFileCacheItem item = upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
        if(item != null) return item.dpuClassStrut;
        return loadClassForDPU(Class.forName(className));
    }
    private DPUJClass loadClassIfNotLoaded(Class c) throws DpuException, IOException {
        String className = c.getName().replace(".", "/");
        DPUClassFileCacheItem item = upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
        if(item != null) return item.dpuClassStrut;
        return loadClassForDPU(c);
    }

    public void recordClass(String className, DPUJClass jc, int classMramAddr){
        upmem.getDPUManager(dpuID).classCacheManager.setClassStrut(className, jc, classMramAddr);
    }

    public String formalClassName(String className){
        return className.replace(".", "/").split("\\$")[0];
    }
    public DPUJClass loadClassForDPU(Class c) throws IOException, DpuException {
        String className = formalClassName(c.getName());
        System.out.println(" ==========--> Try load class " + className + " to dpu#" + dpuID + " <--==========");

        // query cache
        DPUClassFileCacheItem cl = getLoadedClassRecord(className);
        if(cl != null){
            System.out.println("- Class " + className + " already loaded in DPU#" + dpuID);
            return cl.dpuClassStrut;
        }

        // get bytes of class file
        InputStream is = c.getResourceAsStream((c.getSimpleName().split("\\$")[0] + ".class"));
        if(is == null) {
            // TODO class with form of "[....;" cannot be load
            throw new IOException();
        }
        byte[] classFileBytes = is.readAllBytes();
        is.close();

        // preliminary analysis the Class
        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
        DPUJClass jc = classFileAnalyzer.preResolve();

        // Super class ref
        System.out.println(" =================== Resolve class of " + c.getName() + " ==================");
        String superClassName = jc.superClassNameIndex == 0 ? "" : getUTF8(jc, jc.superClassNameIndex);
        System.out.println("super class name = " + superClassName);

        className = formalClassName(superClassName);

        // recursive resolve super class
        if(!"".equals(className)) {
            if(!isClassLoaded(superClassName)){
                System.out.println(" ---- load super class " + className + ", index " + jc.superClassNameIndex);

                loadClassIfNotLoaded(c.getSuperclass());
            }
        }else{
            System.out.println(" ---- No superclass");
            className = formalClassName(c.getName());
            System.out.println(" - Push class " + className + " to DPU#" + dpuID);
            // TODO, currently skip the resolution of java/lang/Object
            int classAddr = pushJClassToDPU(jc);
            recordClass(className, jc, classAddr);
            recordMethodDistribution(c, jc, classAddr);
            return jc;
        }

        int classAddr =
                upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);


        recordClass(formalClassName(c.getName()), jc, classAddr);
        recordMethodDistribution(c, jc, classAddr);
        recordFieldDistribution(c, jc);

        System.out.println(" - In class " + c.getName());


        // resolve each entry item from preprocessed entry table.
        for(int i = 0; i < jc.cpItemCount; i++){
            int tag = (int) ((jc.entryItems[i] >> 56) & 0xFF);
            int classIndex;
            int nameAndTypeIndex;
            switch (tag){
                case ClassFileAnalyzerConstants.CT_Class:
                    System.out.println("In #" + (i) + " ClassRef: ");
                    String classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[i]) & 0xFFFF));
                    System.out.println("className = " + classNameUTF8 + ", index = #" + (jc.entryItems[i] & 0xFFFF));
                    DPUClassFileCacheItem cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if(cacheLine != null){
                        System.out.printf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    }else{
                        if(!"java/lang/System".equals(classNameUTF8)){
                            try {
                                // TODO className$1 loading..
                                loadClassForDPU(Class.forName(classNameUTF8.replace("/", ".")));
                            } catch (ClassNotFoundException e) {
                                System.out.println("cannot find class " + classNameUTF8);
                            } catch (IOException ioException){
                                System.out.println("cannot find class " + classNameUTF8);
                            }
                        }else{
                            System.out.println("ignore " + classNameUTF8);
                        }
                    }

                    jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                    if(cacheLine != null){
                        jc.entryItems[i] |= cacheLine.marmAddr;
                    }
                    break;
                case ClassFileAnalyzerConstants.CT_Fieldref:
                    System.out.println("In #" + (i) + " FieldRef: ");
                    classIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
                    nameAndTypeIndex = (int) ((jc.entryItems[i]) & 0xFFFF);
                    classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[classIndex] >> 32) & 0xFFFF));
                    System.out.println("class name = " + classNameUTF8);
                    cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if(cacheLine != null){
                        System.out.printf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    } else{
                        if("java/io/PrintStream".equals(classNameUTF8)){
                            System.out.println("ignore " + classNameUTF8);
                        }else{
                            try {
                                loadClassForDPU(Class.forName(classNameUTF8.replace("/", ".")));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    String fieldName = getUTF8(jc, (int) ((jc.entryItems[nameAndTypeIndex] >> 16) & 0xFFFF));
                    System.out.println("fieldName = " + fieldName);
                    DPUFieldCacheItem fieldCacheItem = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager
                            .getFieldCacheItem(formalClassName(classNameUTF8), fieldName);

                    jc.entryItems[i] &= 0xFFFFFFFFFFFF0000L;
                    if(fieldCacheItem == null){
                        // TODO, maybe static of final field
                        //throw new RuntimeException("field analysis exception");
                    }else{
                        jc.entryItems[i] |= fieldCacheItem.indexInInstance;
                    }
                    break;
                case ClassFileAnalyzerConstants.CT_Methodref:
                    System.out.println("In #" + (i) + " MethodRef: ");
                    classIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
                    System.out.println("class index = " + classIndex);
                    classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[classIndex] >> 32) & 0xFFFF));
                    System.out.println("class name = " + classNameUTF8);
                    cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if(cacheLine != null){
                        System.out.printf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    } else{
                        if("java/io/PrintStream".equals(classNameUTF8)){
                            System.out.println("ignore " + classNameUTF8);
                        }else{
                            try {
                                // formalize class file name
                                String cName = classNameUTF8.replace("/", ".");
                                if(!"".equals(cName) && cName.charAt(0) == '['){
                                    cName = cName.substring(1).replace(";", "");
                                    if(cName.charAt(0) == 'L'){
                                        loadClassForDPU(Class.forName(cName.substring(1)));
                                    }
                                }else{
                                    loadClassForDPU(Class.forName(cName));
                                }
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    nameAndTypeIndex = (int) (jc.entryItems[i] & 0xFFFF);
                    System.out.printf("name and type index = %d\n", nameAndTypeIndex);
                    String methodName = getUTF8(jc, (int) ((jc.entryItems[nameAndTypeIndex] >> 16) & 0xFFFF));
                    System.out.println("methodName = " + methodName);
                    String TypeDesc = getUTF8(jc, (int) (jc.entryItems[nameAndTypeIndex] & 0xFFFF));
                    System.out.println("type desc = " + TypeDesc);
                    String returnVal = TypeDesc.substring(1).split("\\)")[1];
                    System.out.println("return val = " + returnVal);
                    String paramsDesc = TypeDesc.substring(1).split("\\)")[0];
                    System.out.println("params desc = " + paramsDesc);

                    if(returnVal.length() > 1 && returnVal.charAt(0) == 'L'){
                        int returnTypeNameLen = returnVal.length();
                        String returnTypeName = returnVal.substring(1, returnTypeNameLen - 1);
                        if(upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(returnTypeName) == null){
                            System.out.println("class " + returnVal + " unloaded");
                            try {
                                loadClassForDPU(Class.forName((returnVal.substring(1).replace("/", ".").replace(";", "")) ));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }else{
                            System.out.println("class " + returnTypeName + " loaded");
                        }
                    }

                    String matched = "";
                    int state = 0;
                    for(int ci = 0; ci < paramsDesc.length(); ci++){
                        char ch = paramsDesc.charAt(ci);
                        if(state == 0){
                            switch (ch){
                                case 'B':
                                case 'C':
                                case 'D':
                                case 'F':
                                case 'I':
                                case 'J':
                                case 'S':
                                case 'Z':
                                    break;
                                case 'L':
                                    state = 1;
                                    break;
                                case '[':
                                    break;
                            }
                        }else if(state == 1){
                            if(ch != ';'){
                                matched += ch;
                            }else{
                                // TODO, some class are ignored
                                String[] ignores = new String[]{"java/lang/String"};

                                if(upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(matched) == null){
                                    System.out.println("class " + matched + " unloaded");
                                    if("java/lang/String".equals(matched)){
                                        System.out.println("ignore class " + matched);
                                    }else{
                                        try {
                                            loadClassForDPU(Class.forName(matched.replace("/", ".")));
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                }else{
                                    System.out.println("class " + matched + " loaded");
                                }
                                matched = "";
                                state = 0;
                            }
                        }


                    }

                    // find cache
                    DPUMethodCacheItem jmc = upmem.getDPUManager(dpuID).classCacheManager.getMethodCacheItem(classNameUTF8,
                            methodName + ":" + TypeDesc);


                    jc.entryItems[i] = 0;
                    jc.entryItems[i] |= ((long)classIndex << 48) & 0xFFFF000000000000L;
                    jc.entryItems[i] |= ((long)nameAndTypeIndex << 32) & 0x0000FFFF00000000L;
                    if(jmc != null){
                        jc.entryItems[i] |=  jmc.mramAddr;
                        System.out.printf("%x\n", jmc.mramAddr);
                    }
                    break;
            }
        }

        pushJClassToDPU(jc, classAddr);

        return jc;
    }

    private void recordFieldDistribution(Class c, DPUJClass jc) {
        for(int i = 0; i < jc.fieldCount; i++){
            String className = formalClassName(c.getName());
            String fieldName = getUTF8(jc, jc.fields[i].nameIndex);
            UPMEM.getInstance().getDPUManager(dpuID).classCacheManager
                    .setFieldCacheItem(className,fieldName , jc.fields[i].indexInInstance);
        }
    }

    private void recordMethodDistribution(Class c, DPUJClass jc, int classAddr) {
        for(int mIndex = 0; mIndex < jc.methodCount; mIndex++){
            upmem.getDPUManager(dpuID).classCacheManager
                    .setMethodCacheItem(c.getName().replace(".", "/"),

                            getUTF8(jc, jc.methodTable[mIndex].nameIndex) + ":" + getUTF8(jc,jc.methodTable[mIndex].descriptorIndex),
                            jc.methodOffset[mIndex] + 48
                                    + 8 * jc.cpItemCount +
                                    Arrays.stream(jc.fields).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0)
                                    + classAddr
                    );
        }
    }


    public void pushJClassToDPU(DPUJClass jc, int addr) throws DpuException {
        byte[] classBytes = cvtDPUClassStrut2Bytes(jc, addr);
        upmem.getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_METASPACE, classBytes, addr);
    }


    public int pushJClassToDPU(DPUJClass jc) throws DpuException {
        int addr = upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);
        byte[] classBytes =
                cvtDPUClassStrut2Bytes(jc, addr);
        upmem.getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_METASPACE, classBytes, addr);

        return addr;
    }


    public static byte[] cvtDPUClassStrut2Bytes(DPUJClass ds, int classAddr){
        byte[] bs = new byte[(ds.totalSize + 0b111) & ~(0b111)];

        int pos = 0;
        int entryTablePointer = 0;
        int fieldPointer = 0;
        int methodPointer = 0;
        int constantAreaPointer = 0;
        int entryTablePointerPos = 0;
        int fieldPointerPos = 0;
        int methodPointerPos = 0;
        int constantAreaPointerPos = 0;
        BytesUtils.writeU4LittleEndian(bs, ds.totalSize, pos);
        pos += 4;
        BytesUtils.writeU2LittleEndian(bs, ds.thisClassNameIndex, pos);
        pos += 2;
        BytesUtils.writeU2LittleEndian(bs, ds.superClassNameIndex, pos);
        pos += 2;
        BytesUtils.writeU4LittleEndian(bs, ds.superClass, pos);
        pos += 4;
        BytesUtils.writeU2LittleEndian(bs, ds.accessFlags, pos);
        pos += 2;
        BytesUtils.writeU2LittleEndian(bs, ds.cp2BOffset, pos);
        pos += 2;

        BytesUtils.writeU4LittleEndian(bs, ds.cpItemCount, pos);
        pos += 4;
        // entry table pointer
        entryTablePointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.fieldCount, pos);
        pos += 4;
        fieldPointerPos = pos;
        pos += 4;


        BytesUtils.writeU4LittleEndian(bs, ds.methodCount, pos);
        pos += 4;

        methodPointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.stringINTConstantPoolLength, pos);
        System.out.printf("print 0x%x to %x\n", ds.stringINTConstantPoolLength, pos);
        pos += 4;


        constantAreaPointerPos = pos;
        pos += 4;
        entryTablePointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, entryTablePointer, entryTablePointerPos);
        System.out.printf("print %x in %x\n", entryTablePointer, entryTablePointerPos);
        for(int i = 0; i < ds.cpItemCount; i++){
            long v = ds.entryItems[i];
            BytesUtils.writeU4LittleEndian(bs, (int) (((long)v >> 32) & 0xFFFFFFFF),pos);
            pos += 4;
            BytesUtils.writeU4LittleEndian(bs, (int) (((long)v) & 0xFFFFFFFF),pos);
            pos += 4;
        }

        //fields
        fieldPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, fieldPointer, fieldPointerPos);
        // TODO: use field
        pos += Arrays.stream(ds.fields).map(f -> f.size).reduce((s1, s2) -> s1 + s2).orElseGet( ()->0);

        //method
        methodPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, methodPointer, methodPointerPos);
        System.out.printf("print 0x%x to %x\n", methodPointer, methodPointerPos);

        for(int i = 0; i < ds.methodCount; i++){
            System.out.printf("method %d from 0x%x === 0x%x\n", i, pos, ds.methodOffset[i]);
            DPUJMethod dm = ds.methodTable[i];
            // writeU4(bs, pos, methodPointer + i * 4);

            BytesUtils.writeU4LittleEndian(bs, dm.size, pos);
            pos += 4;
            BytesUtils.writeU2LittleEndian(bs, dm.accessFlag, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.paramCount, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.nameIndex, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.methodAttrCode.maxStack, pos);
            pos += 2;
            BytesUtils.writeU2LittleEndian(bs, dm.methodAttrCode.maxLocals, pos);
            pos += 2;
            pos += 2; //retained

            BytesUtils.writeU4LittleEndian(bs, dm.methodAttrCode.codeLength, pos);
            pos += 4;

            // bytecode pt
            BytesUtils.writeU4LittleEndian(bs, classAddr + pos + 4, pos);
            pos += 4;

            for(int k = 0; k < dm.methodAttrCode.codeLength; k++){
                bs[pos + k] = dm.methodAttrCode.code[k];
            }

            pos += (dm.methodAttrCode.codeLength + 0b111) & (~0b111);

        }

        constantAreaPointer = DPUGarbageCollector.metaSpaceBeginAddr + pos;

        BytesUtils.writeU4LittleEndian(bs, constantAreaPointer, constantAreaPointerPos);

        for(int offset = 0; offset < ds.stringINTConstantPoolLength; offset ++){
            bs[pos + offset] = ds.constantBytes[offset];
        }
        pos += (ds.stringINTConstantPoolLength + 0b111) & (~0b111);
        System.out.printf("=============== !Alert pos = %d === total-size = %d ================\n", pos, ds.totalSize);
        if(pos != ds.totalSize) throw new RuntimeException();
        return bs;
    }

    public static String getUTF8(DPUJClass ds, int utf8Index)
    {
        return  ClassFileAnalyzer.getStringFromBuffer(ds.constantBytes,
                (int) (ds.entryItems[utf8Index] & 0xFFFF),
                (int) (((ds.entryItems[utf8Index]) >> 40) & 0xFFFF)
        );
    }

    public static void printDPUClassStrut(DPUJClass ds){
        System.out.println("-----------------------");
        int thisClassNameIndex = (int) ((ds.entryItems[ds.thisClassNameIndex] >> 40) & 0xFF) - 1;
        int superClassNameIndex =  (int) ((ds.entryItems[ds.superClass] >> 40) & 0xFF) - 1;
        String thisClassName = getUTF8(ds, thisClassNameIndex);
        String superClassName = getUTF8(ds, superClassNameIndex);
        System.out.println("This class = #" + (ds.thisClassNameIndex) + " #" + ((ds.entryItems[ds.thisClassNameIndex - 1] >> 40) & 0xFF)+ " " + thisClassName);
        System.out.println("Super class = #" + (ds.superClassNameIndex) + " #" + ((ds.entryItems[ds.superClassNameIndex - 2] >> 40) & 0xFF)+ " " + superClassName);
    }

}

