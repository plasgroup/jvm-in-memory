package pim.dpu;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.algorithm.DPUTreeNode;
import pim.logger.Logger;
import pim.utils.BytesUtils;
import pim.utils.StringUtils;
import pim.UPMEM;

import java.io.IOException;
import java.io.InputStream;
import java.net.CacheRequest;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;


public class DPUClassFileManager {
    static Logger classfileLogger = Logger.getLogger("pim:classfile");
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
        if("".equals(c.getName())) return null;
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
        classfileLogger.logln(" ==========--> Try load class " + className + " to dpu#" + dpuID + " <--==========");

        // query cache
        DPUClassFileCacheItem cl = getLoadedClassRecord(className);
        if(cl != null){
            classfileLogger.logln("- Class " + className + " already loaded in DPU#" + dpuID);
            return cl.dpuClassStrut;
        }

        // get bytes of class file
        InputStream is = c.getResourceAsStream((c.getSimpleName().split("\\$")[0] + ".class"));
        if(is == null) {
            // TODO class name with form of "[....;" cannot be load
            throw new IOException("cannot find class " + c.getSimpleName().split("\\$")[0] + ".class");
        }
        byte[] classFileBytes = is.readAllBytes();
        is.close();

        // preliminary analysis the Class
        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
        DPUJClass jc = classFileAnalyzer.preResolve();

        // load super class recursively
        String superClassName = jc.superClassNameIndex == 0 ? "" : getUTF8(jc, jc.superClassNameIndex);
        classfileLogger.logln("super class name = " + superClassName);
        className = formalClassName(superClassName);
        

        // recursive resolve super class
        if(!"".equals(className)) {
            if(!isClassLoaded(superClassName)){
                classfileLogger.logln(" ---- load super class " + className + ", index " + jc.superClassNameIndex);
                loadClassForDPU(c.getSuperclass());
            }
        }else{
            classfileLogger.logln(" ---- No superclass ----");
            className = formalClassName(c.getName());
            classfileLogger.logln(" - Push class " + className + " to DPU#" + dpuID);
            // TODO, currently skip the resolution of java/lang/Object

            int classAddr =
                    upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);


            recordClass(className, jc, classAddr);
            recordMethodDistribution(c, jc, classAddr);
            recordFieldDistribution(c, jc);
            createVirtualTable(jc, classFileBytes);
            upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE,
                    ((8 * jc.virtualTable.items.size()) + 0b111) & (~0b111));
            for(int i = 0; i < jc.virtualTable.items.size(); i++){
                String vClassName = jc.virtualTable.items.get(i).className;
                String vDescriptor = jc.virtualTable.items.get(i).descriptor;
                DPUCacheManager classCacheManager = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
                classfileLogger.logln("" + classCacheManager);
                DPUMethodCacheItem methodCacheItem =
                        UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getMethodCacheItem(vClassName, vDescriptor);
                if(methodCacheItem != null)
                {
                    jc.virtualTable.items.get(i).methodReferenceAddress = methodCacheItem.mramAddr;
                    jc.virtualTable.items.get(i).classReferenceAddress = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(vClassName).marmAddr;
                }
            }
            pushJClassToDPU(jc, classAddr);
            return jc;
        }


        int classAddr =
                upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);

        recordClass(formalClassName(c.getName()), jc, classAddr);
        recordMethodDistribution(c, jc, classAddr);
        recordFieldDistribution(c, jc);
        createVirtualTable(jc, classFileBytes);
        upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE,
                ((8 * jc.virtualTable.items.size()) + 0b111) & (~0b111));




        classfileLogger.logln(" - In class " + c.getName() + " resolve unknow name");


        // resolve each entry item from preprocessed entry table.

        for(int i = 0; i < jc.cpItemCount; i++){
            int tag = (int) ((jc.entryItems[i] >> 56) & 0xFF);
            int classIndex;
            int nameAndTypeIndex;
            switch (tag){
                case ClassFileAnalyzerConstants.CT_Class:
                    classfileLogger.logln("In #" + (i) + " ClassRef: ");
                    String classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[i]) & 0xFFFF));
                    classfileLogger.logln("className = " + classNameUTF8 + ", index = #" + (jc.entryItems[i] & 0xFFFF));
                    DPUClassFileCacheItem cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if(cacheLine != null){
                        classfileLogger.logf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    }else{
                        if(!"java/lang/System".equals(classNameUTF8)){
                            try {
                                // TODO className$1 loading..
                                loadClassForDPU(Class.forName(classNameUTF8.replace("/", ".")));
                            } catch (ClassNotFoundException e) {
                                classfileLogger.logln("cannot find class " + classNameUTF8);
                            } catch (IOException ioException){
                                classfileLogger.logln("cannot find class " + classNameUTF8);
                            }
                        }else{
                            classfileLogger.logln("ignore " + classNameUTF8);
                        }
                    }

                    jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                    if(cacheLine != null){
                        jc.entryItems[i] |= cacheLine.marmAddr;
                    }
                    break;
                case ClassFileAnalyzerConstants.CT_Fieldref:
                    classfileLogger.logln("In #" + (i) + " FieldRef: ");
                    classIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
                    nameAndTypeIndex = (int) ((jc.entryItems[i]) & 0xFFFF);
                    classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[classIndex] >> 32) & 0xFFFF));
                    classfileLogger.logln("class name = " + classNameUTF8);
                    cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if(cacheLine != null){
                        classfileLogger.logf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    } else{
                        if("java/io/PrintStream".equals(classNameUTF8)){
                            classfileLogger.logln("ignore " + classNameUTF8);
                        }else{
                            try {
                                loadClassForDPU(Class.forName(classNameUTF8.replace("/", ".")));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    String fieldName = getUTF8(jc, (int) ((jc.entryItems[nameAndTypeIndex] >> 16) & 0xFFFF));
                    classfileLogger.logln("fieldName = " + fieldName);
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
                    classfileLogger.logln("In #" + (i) + " MethodRef: ");

                    int methodTableIndex = (int) ((jc.entryItems[i]) & 0xFFFFFFFF);
                    classfileLogger.logln("method Table Index = " + methodTableIndex);

                    if(methodTableIndex == 0) continue;
                    VirtualTableItem vItem = jc.virtualTable.items.get(methodTableIndex);
                    classfileLogger.logln("description = " + vItem.className + "." + vItem.descriptor);

                    DPUMethodCacheItem methodCacheItem = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getMethodCacheItem(
                            vItem.className, vItem.descriptor
                    );

                    if(methodCacheItem != null){

                    }

                    cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(vItem.className);
                    if(cacheLine != null){
                        classfileLogger.logf("class %s loaded, mram addr = 0x%x\n", vItem.className, cacheLine.marmAddr);
                    } else{
                        if("java/io/PrintStream".equals(vItem.className)){
                            classfileLogger.logln("ignore " + vItem.className);
                        }else{
                            try {
                                // formalize class file name
                                String cName = vItem.className.replace("/", ".");
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
                            methodCacheItem = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getMethodCacheItem(
                                    vItem.className, vItem.descriptor
                            );
                        }
                    }
                    if(methodCacheItem == null) continue;

                    DPUJClass methodJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrut(vItem.className);
                    String methodName = getUTF8(methodJc, methodCacheItem.dpujMethod.nameIndex);
                    classfileLogger.logln("methodName = " + methodName);
                    String TypeDesc = getUTF8(methodJc, methodCacheItem.dpujMethod.descriptorIndex);
                    classfileLogger.logln("type desc = " + TypeDesc);
                    String returnVal = TypeDesc.substring(1).split("\\)")[1];
                    classfileLogger.logln("return val = " + returnVal);
                    String paramsDesc = TypeDesc.substring(1).split("\\)")[0];
                    classfileLogger.logln("params desc = " + paramsDesc);

                    if(returnVal.length() > 1 && returnVal.charAt(0) == 'L'){
                        int returnTypeNameLen = returnVal.length();
                        String returnTypeName = returnVal.substring(1, returnTypeNameLen - 1);
                        if(upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(returnTypeName) == null){
                            classfileLogger.logln("class " + returnVal + " unloaded");
                            try {
                                loadClassForDPU(Class.forName((returnVal.substring(1).replace("/", ".").replace(";", "")) ));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }else{
                            classfileLogger.logln("class " + returnTypeName + " loaded");
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
                                    classfileLogger.logln("class " + matched + " unloaded");
                                    if("java/lang/String".equals(matched)){
                                        classfileLogger.logln("ignore class " + matched);
                                    }else{
                                        try {
                                            loadClassForDPU(Class.forName(matched.replace("/", ".")));
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                }else{
                                    classfileLogger.logln("class " + matched + " loaded");
                                }
                                matched = "";
                                state = 0;
                            }
                        }


                    }

                    // find cache
                    jc.entryItems[i] = 0;
                    if(getUTF8(jc, jc.thisClassNameIndex).equals("pim/algorithm/TreeNode")){
                        classfileLogger.logln("");
                    }
                    jc.entryItems[i] |= ((long)BytesUtils.readU2BigEndian(classFileBytes, jc.itemBytesEntries[i] + 1) << 48) & 0xFFFF000000000000L;
                    jc.entryItems[i] |= ((long)BytesUtils.readU2BigEndian(classFileBytes, jc.itemBytesEntries[i] + 3) << 32) & 0x0000FFFF00000000L;

                    jc.entryItems[i] |=  methodTableIndex;
                    classfileLogger.logf("%x\n", methodCacheItem.mramAddr);

                    break;
            }
        }



        for(int i = 0; i < jc.virtualTable.items.size(); i++){
            String vClassName = jc.virtualTable.items.get(i).className;
            String vDescriptor = jc.virtualTable.items.get(i).descriptor;
            DPUMethodCacheItem methodCacheItem =
                    UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getMethodCacheItem(vClassName, vDescriptor);
            if(methodCacheItem != null){
                jc.virtualTable.items.get(i).methodReferenceAddress = methodCacheItem.mramAddr;
                jc.virtualTable.items.get(i).classReferenceAddress = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(vClassName).marmAddr;
            }
        }
        UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrut(formalClassName(c.getName()))
                .virtualTable = jc.virtualTable;
        DPUCacheManager classCacheManager = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
        classfileLogger.logln("" + classCacheManager);
        pushJClassToDPU(jc, classAddr);

        return jc;
    }


    String getClassNameFromClassRef(DPUJClass jc, byte[] classBytes, int classRefIndex){
        int classNameUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[classRefIndex] + 1);
        return getUTF8(jc, classNameUTF8Index);
    }
    String getMethodDescriptor(DPUJClass jc, byte[] classBytes, int methodRefIndex){
        int classIndex = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[methodRefIndex] + 1);
        int nameAndTypeIndex =  BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[methodRefIndex] + 3);
        int classUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[classIndex] + 1);
        int nameUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[nameAndTypeIndex] + 1);
        int typeUTF8Index = BytesUtils.readU2BigEndian(classBytes, jc.itemBytesEntries[nameAndTypeIndex] + 3);

        String classUTF8 = getUTF8(jc, classUTF8Index);
        String nameUTF8 = getUTF8(jc, nameUTF8Index);
        String typeUTF8 = getUTF8(jc, typeUTF8Index);
        String descriptor = classUTF8 + "." + nameUTF8 + ":" + typeUTF8;
        return descriptor;
    }

    Dictionary<String, Integer> globalVirtualTableIndexCache = new Hashtable<>();
    private void createVirtualTable(DPUJClass jc, byte[] classBytes) {
        /* iterate method table */
        String thisClassName = getUTF8(jc, jc.thisClassNameIndex);
        if("pim/algorithm/DPUTreeNode".equals(thisClassName)){
            classfileLogger.logln("");
        }

        classfileLogger.logln("in java class = " + thisClassName);
        VirtualTable thisClassVirtualTable = new VirtualTable();
        jc.virtualTable = thisClassVirtualTable;
        if(jc.superClass == 0){
            // java/lang/Object
            /* iterate method table to put all methods to v_table */
            thisClassVirtualTable.items.add(new VirtualTableItem("", ""));
            for(int i = 0; i < jc.methodTable.length; i++){
                DPUJMethod method = jc.methodTable[i];
                String methodName = getUTF8(jc, method.nameIndex);
                String methodDescriptor = getUTF8(jc, method.descriptorIndex);
                String descriptor = methodName + ":" + methodDescriptor;
                int index = thisClassVirtualTable.items.size();
                thisClassVirtualTable.items.add(new VirtualTableItem(thisClassName, descriptor));

                classfileLogger.logf(" - Add method %s to Vtable of %s, className = %s, index = %d\n", descriptor, thisClassName, thisClassName, index);

                globalVirtualTableIndexCache.put(thisClassName + "." + descriptor, index);
            }

            for(int i = 0; i < jc.entryItems.length; i++){
                int tag = classBytes[jc.itemBytesEntries[i]];
                if(tag == ClassFileAnalyzerConstants.CT_Methodref){
                    String description = getMethodDescriptor(jc, classBytes, i);
                    String className = description.split("\\.")[0];
                    String descriptor = description.split("\\.")[1];
                    classfileLogger.logln(" - #" + i + " is MethodRef, description = " + className + "." + descriptor);
                    if(isClassLoaded(className)){
                        classfileLogger.logln(className + " is loaded");
                        int index = globalVirtualTableIndexCache.get(className + "." + descriptor);
                        classfileLogger.logln(" - get method index = " + index);
                        jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                        jc.entryItems[i] |= index;
                    }else{
                        classfileLogger.logln(className + " is not loaded, skip");
                    }
                }
            }
        }else{
            // get super jc;
            String superClassName = getUTF8(jc, jc.superClassNameIndex);
            DPUJClass superClassJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(superClassName).dpuClassStrut;
            if(superClassJc == null) throw new RuntimeException("super class not loaded");

            /* copy all methods from super */
            VirtualTable superVirtualTable = superClassJc.virtualTable;
            for(int i = 0; i < superVirtualTable.items.size(); i++){
                VirtualTableItem item = superVirtualTable.items.get(i);
                jc.virtualTable.items.add(new VirtualTableItem(item.className, item.descriptor));
            }

            /* append or rewrite virtual table item */
            for(int i = 0; i < jc.methodTable.length; i++){
                DPUJMethod method = jc.methodTable[i];
                String methodName = getUTF8(jc, method.nameIndex);
                String methodDescriptor = getUTF8(jc, method.descriptorIndex);
                String descriptor = methodName + ":" + methodDescriptor;

                /* find whether super classes/interfaces has the method with the same descriptor */
                boolean written = false;
                for(int j = 0; j < jc.virtualTable.items.size(); j++){
                    String vClassName = jc.virtualTable.items.get(j).className;
                    String vDescriptor = jc.virtualTable.items.get(j).descriptor;
                    if(vDescriptor.startsWith("<init>")){
                        continue;
                    }
                    if(vDescriptor.equals(descriptor)) {
                        try {
                            Class thisClass = Class.forName(thisClassName.replace("/", "."));
                            Class vClass = Class.forName(vClassName.replace("/", "."));
                            if (vClass.isAssignableFrom(thisClass)) {
                                // rewrite
                                jc.virtualTable.items.get(j).className = thisClassName;
                                written = true;
                                break;
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if(!written) {
                    int index = jc.virtualTable.items.size();
                    jc.virtualTable.items.add(new VirtualTableItem(thisClassName, descriptor));
                    globalVirtualTableIndexCache.put(thisClassName + "." + descriptor, index);
                }
            }


            /* set index in entry table */
            for(int i = 0; i < jc.entryItems.length; i++){
                int tag = classBytes[jc.itemBytesEntries[i]];
                if(tag == ClassFileAnalyzerConstants.CT_Methodref){
                    String description = getMethodDescriptor(jc, classBytes, i);
                    String className = description.split("\\.")[0];
                    String descriptor = description.split("\\.")[1];
                    classfileLogger.logln(" - #" + i + " is MethodRef, description = " + className + "." + descriptor);
                    while(!"".equals(className)){
                       if(globalVirtualTableIndexCache.get(className + "." + descriptor) != null){
                           Integer index = globalVirtualTableIndexCache.get(className + "." + descriptor);
                           classfileLogger.logln("set #" + i + " " + className + "." + descriptor + " index " + index);
                           jc.entryItems[i] &= 0xFFFFFFFF00000000L;
                           jc.entryItems[i] |= index;
                           break;
                       }else{
                           DPUJClass methodReferenceJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className).dpuClassStrut;
                           if(methodReferenceJc.superClassNameIndex != 0){
                               className = getUTF8(methodReferenceJc, methodReferenceJc.superClassNameIndex);
                           }else{
                               className = "";
                           }

                       }
                    }
                }
            }

            for(int j = 0; j < jc.virtualTable.items.size(); j++){
                String vClassName = jc.virtualTable.items.get(j).className;
                String vDescriptor = jc.virtualTable.items.get(j).descriptor;
                classfileLogger.logln("vtable #" + j + " " + vClassName + "." + vDescriptor);
            }


            classfileLogger.logln("");
            ClassFileAnalyzer.printEntryTable(jc);
        }


        jc.totalSize = 48 + jc.cpItemCount * 8 + 8 +
                Arrays.stream(jc.fields).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0) +
                Arrays.stream(jc.methodTable).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0)
                + ((jc.stringINTConstantPoolLength + 0b111) & (~0b111))
                + ((8 * jc.virtualTable.items.size()) + 0b111 & (~0b111));
        ;
    }

    private void recordFieldDistribution(Class c, DPUJClass jc) {
        for(int i = 0; i < jc.fieldCount; i++){
            String className = formalClassName(c.getName());
            String fieldName = getUTF8(jc, jc.fields[i].nameIndex);
            UPMEM.getInstance().getDPUManager(dpuID).classCacheManager
                    .setFieldCacheItem(className,fieldName, jc.fields[i].indexInInstance);
        }
    }

    private void recordMethodDistribution(Class c, DPUJClass jc, int classAddr) {
        for(int mIndex = 0; mIndex < jc.methodCount; mIndex++){
            upmem.getDPUManager(dpuID).classCacheManager
                    .setMethodCacheItem(c.getName().replace(".", "/"),
                            getUTF8(jc, jc.methodTable[mIndex].nameIndex) + ":" + getUTF8(jc,jc.methodTable[mIndex].descriptorIndex),
                            jc.methodOffset[mIndex] + 48 + 8 +
                                    + 8 * jc.cpItemCount +
                                    Arrays.stream(jc.fields).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElseGet(()->0)
                                    + classAddr
                    , jc.methodTable[mIndex]);
        }
    }


    public void pushJClassToDPU(DPUJClass jc, int addr) throws DpuException {

        byte[] classBytes = cvtDPUClassStrut2Bytes(jc, addr);
        upmem.getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_METASPACE, classBytes, addr);
    }


    public static byte[] cvtDPUClassStrut2Bytes(DPUJClass ds, int classAddr){
        byte[] bs = new byte[(ds.totalSize + 0b111) & ~(0b111)];
        int pos = 0;
        int entryTablePointer;
        int fieldPointer;
        int methodPointer;
        int constantAreaPointer;
        int entryTablePointerPos;
        int fieldPointerPos;
        int methodPointerPos;
        int constantAreaPointerPos;
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
        classfileLogger.logf("print 0x%x to %x\n", ds.stringINTConstantPoolLength, pos);
        pos += 4;
        constantAreaPointerPos = pos;
        pos += 4;

        BytesUtils.writeU4LittleEndian(bs, ds.virtualTable.items.size(), pos);
        pos += 4;
        int virtualTablePointerPos = pos;
        pos += 4;

        entryTablePointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, entryTablePointer, entryTablePointerPos);
        classfileLogger.logf("print %x in %x\n", entryTablePointer, entryTablePointerPos);
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
        classfileLogger.logf("print 0x%x to %x\n", methodPointer, methodPointerPos);

        for(int i = 0; i < ds.methodCount; i++){
            classfileLogger.logf("method %d from 0x%x === 0x%x\n", i, pos, ds.methodOffset[i]);
            DPUJMethod dm = ds.methodTable[i];

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

        constantAreaPointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, constantAreaPointer, constantAreaPointerPos);

        for(int offset = 0; offset < ds.stringINTConstantPoolLength; offset ++){
            bs[pos + offset] = ds.constantBytes[offset];
        }
        pos += (ds.stringINTConstantPoolLength + 0b111) & (~0b111);

        // vtable
        int virtualTablePointer = classAddr + pos;
        BytesUtils.writeU4LittleEndian(bs, virtualTablePointer, virtualTablePointerPos);

        // items
        for(int i = 0; i < ds.virtualTable.items.size(); i++){
            VirtualTableItem item = ds.virtualTable.items.get(i);
            BytesUtils.writeU4LittleEndian(bs, item.classReferenceAddress , pos);
            BytesUtils.writeU4LittleEndian(bs, item.methodReferenceAddress , pos + 4);
            pos += 8;
        }
        pos = (pos + 0b111) & (~0b111);


        Logger.logf("pim:classfile","=============== !Alert pos = %d === total-size = %d ================\n", pos, ds.totalSize);
        if(pos != ds.totalSize) throw new RuntimeException();
        return bs;
    }

    public static String getUTF8(DPUJClass ds, int utf8Index)
    {
        return StringUtils.getStringFromBuffer(ds.constantBytes, (int) (ds.entryItems[utf8Index] & 0xFFFF), (int) (((ds.entryItems[utf8Index]) >> 40) & 0xFFFF));
    }

}

