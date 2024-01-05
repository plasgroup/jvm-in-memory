package simulator;

import com.upmem.dpu.DpuException;
import framework.pim.UPMEM;
import framework.pim.dpu.cache.DPUCacheManager;
import framework.pim.dpu.cache.DPUClassFileCacheItem;
import framework.pim.dpu.cache.DPUFieldCacheItem;
import framework.pim.dpu.cache.DPUMethodCacheItem;
import framework.pim.dpu.classloader.ClassFileAnalyzer;
import framework.pim.dpu.classloader.ClassFileAnalyzerConstants;
import framework.pim.dpu.classloader.DPUClassFileManager;
import framework.pim.dpu.java_strut.*;
import framework.pim.utils.BytesUtils;
import framework.pim.utils.StringUtils;
import transplant.index.search.IndexTable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.*;

import static framework.pim.dpu.classloader.ClassFileAnalyzer.printEntryTable;
import static framework.pim.dpu.classloader.ClassWriter.pushJClassToDPU;
import static framework.pim.utils.ClassLoaderUtils.*;
import static framework.pim.utils.ClassLoaderUtils.getMethodDescriptor;


public class DPUClassFileManagerSimulator extends DPUClassFileManager {
    private final DPUJVMRemote dpujvmRemote;
    public DPUClassFileManagerSimulator(int dpuID, simulator.DPUJVMRemote dpujvmRemote) {
        this.dpuID = dpuID;
        this.dpujvmRemote = dpujvmRemote;
    }

    @Override
    public void recordClass(String className, DPUJClass jc, int classMramAddr) {
        System.out.println("record " + className + ":" + classMramAddr);
        upmem.getDPUManager(dpuID).classCacheManager.setClassStructure(className, jc, classMramAddr);
    }
    private DPUClassFileCacheItem getLoadedClassRecord(String className){
        className = className.replace(".", "/");
        return upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
    }
    private boolean isClassLoaded(String className){
        className = className.replace(".", "/");
        DPUClassFileCacheItem item = upmem.getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className);
        return item != null;
    }
    private void recordMethodDistribution(Class c, DPUJClass jc, int classAddr) {
        for(int mIndex = 0; mIndex < jc.methodCount; mIndex++){
            String desc = getUTF8(jc,jc.methodTable[mIndex].descriptorIndex);
            List<Class> classes = null;
            try {
                classes = descriptorToClasses(desc.substring(1, desc.indexOf(')')));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                int addr;
                if(classes.size() == 0){
                    addr = dpujvmRemote.pushToMetaSpace(c, (getUTF8(jc,jc.methodTable[mIndex].nameIndex)));
                }else{
                    Class[] classesArray = new Class[classes.size()];
                    for(int i = 0; i < classes.size(); i++){
                        classesArray[i] = classes.get(i);
                    }
                    addr = dpujvmRemote.pushToMetaSpace(c, (getUTF8(jc,jc.methodTable[mIndex].nameIndex)), classesArray);

                }

                upmem.getDPUManager(dpuID).classCacheManager
                        .setMethodCacheItem(c.getName().replace(".", "/"),
                                getUTF8(jc, jc.methodTable[mIndex].nameIndex) + ":" + getUTF8(jc,jc.methodTable[mIndex].descriptorIndex),
                                addr
                                , jc.methodTable[mIndex]);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }


    List<Class> descriptorToClasses(String desc) throws ClassNotFoundException {
        System.out.println("parse " + desc);
        String matched = "";
        int state = 0;
        int arrayDim = 0;
        List<Class> classes = new ArrayList<>();
        for(int ci = 0; ci < desc.length(); ci++){
            char ch = desc.charAt(ci);
            if(state == 0){
                switch (ch){
                    case 'B':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "B"));
                            arrayDim = 0;
                        }else{
                            classes.add(byte.class);
                        }
                        break;
                    case 'C':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "C"));
                            arrayDim = 0;
                        }else{
                            classes.add(char.class);
                        }
                        break;
                    case 'D':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "D"));
                            arrayDim = 0;
                        }else{
                            classes.add(double.class);
                        }
                        break;
                    case 'F':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "F"));
                            arrayDim = 0;
                        }else{
                            classes.add(float.class);
                        }
                        break;
                    case 'I':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "I"));
                            arrayDim = 0;
                        }else{
                            classes.add(int.class);
                        }
                        break;
                    case 'J':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "J"));
                            arrayDim = 0;
                        }else{
                            classes.add(long.class);
                        }
                        break;
                    case 'S':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "S"));
                            arrayDim = 0;
                        }else{
                            classes.add(short.class);
                        }
                        break;
                    case 'Z':
                        if(arrayDim != 0){
                            classes.add(Class.forName("[".repeat(arrayDim) + "S"));
                            arrayDim = 0;
                        }else{
                            classes.add(boolean.class);
                        }
                        break;
                    case 'L':
                        state = 1;
                        break;

                    case '[':
                        arrayDim++;
                        break;
                }
            }else if(state == 1){
                if(ch != ';'){
                    matched += ch;
                }else{
                    Class c = null;
                    if(arrayDim > 0){
                        c = Class.forName("[".repeat(arrayDim) + "L".repeat(arrayDim) + matched.replace("/",
                                ".") + ";");
                        arrayDim = 0;

                    }else{
                        c = Class.forName(matched.replace("/", "."));
                    }

                    classes.add(c);
                    matched = "";
                    state = 0;
                }


                }
            }

        return classes;
    }
    private void recordFieldDistribution(Class c, DPUJClass jc) {
        for(int i = 0; i < jc.fieldCount; i++){
            String className = formalClassName(c.getName());
            String fieldName = getUTF8(jc, jc.fields[i].nameIndex);
            UPMEM.getInstance().getDPUManager(dpuID).classCacheManager
                    .setFieldCacheItem(className,fieldName, jc.fields[i].indexInInstance);
        }
    }
    Dictionary<String, Integer> globalVirtualTableIndexCache = new Hashtable<>();


    static {
        classfileLogger.setEnable(true);
    }

    public DPUJClass loadClassesToDPUFromDescriptorSingle(String descriptor) throws ClassNotFoundException {
        StringBuilder sb = new StringBuilder(descriptor);
        int arrayDim = 0;
        while(sb.length() > 0 && sb.charAt(0) == 'p'){
            sb.deleteCharAt(0);
        }
        descriptor = sb.toString();
        if("B".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "B"));
            }else{
                return loadClassToDPU(byte.class);
            }
        }else if("C".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "C"));
            }else{
                return loadClassToDPU(char.class);
            }
        }else if("D".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "D"));
            }else{
                return loadClassToDPU(double.class);
            }
        }else if("F".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "F"));
            }else{
                return loadClassToDPU(float.class);
            }
        }else if("I".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "I"));
            }else{
                return loadClassToDPU(int.class);
            }
        }else if("J".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "J"));
            }else{
                return loadClassToDPU(long.class);
            }
        }else if("Z".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "S"));
            }else{
                return loadClassToDPU(boolean.class);
            }
        }else if("S".equals(descriptor)){
            if(arrayDim != 0){
                return loadClassToDPU(Class.forName("[".repeat(arrayDim) + "S"));
            }else{
                return loadClassToDPU(short.class);
            }
        }else{
            Class c = null;
            if(arrayDim > 0){
                c = Class.forName("[".repeat(arrayDim) +  descriptor.replace("/",
                        ".") + ";");
                arrayDim = 0;

            }else{
                if(descriptor.length() == 0) return null;
                if(descriptor.charAt(0) == 'L'){
                    c = Class.forName(descriptor.replace('/', '.').substring(1).replace(";",""));
                }else{
                    c = Class.forName(descriptor.replace('/', '.'));
                }
            }

            return loadClassToDPU(c);
        }

    }
    public void loadClassesToDPUFromDescriptor(String descriptor) throws ClassNotFoundException {
        if(descriptor.charAt(0) == '('){
            int state = 0;
            for(int ci = 0; ci < descriptor.length(); ci++){
                char ch = descriptor.charAt(ci);
                String matched = "";
                if(state == 0){
                    switch (ch){
                        case '(':
                            break;
                        case 'V':
                            break;
                        case 'B':
                            loadClassesToDPUFromDescriptorSingle(matched + "B");
                            break;
                        case 'C':
                            loadClassesToDPUFromDescriptorSingle(matched + "C");
                            break;
                        case 'D':
                            loadClassesToDPUFromDescriptorSingle(matched + "D");
                            break;
                        case 'F':
                            loadClassesToDPUFromDescriptorSingle(matched + "F");
                            break;
                        case 'I':
                            loadClassesToDPUFromDescriptorSingle(matched + "I");
                            break;
                        case 'J':
                            loadClassesToDPUFromDescriptorSingle(matched + "J");
                            break;
                        case 'S':
                            loadClassesToDPUFromDescriptorSingle(matched + "S");
                            break;
                        case 'Z':
                            loadClassesToDPUFromDescriptorSingle(matched + "Z");
                            break;
                        case 'L':
                            state = 1;
                            break;

                        case '[':
                            matched += "[";
                            break;
                    }
                }else if(state == 1){
                    if(ch != ';'){
                        matched += ch;
                    }else{
                        loadClassesToDPUFromDescriptorSingle(matched);
                        matched = "";
                        state = 0;
                    }


                }
            }
        }else{
            loadClassesToDPUFromDescriptorSingle(descriptor);
        }

    }
    static HashSet<String> allowSet = new HashSet<>();

    static {
        allowSet.add("java.lang.Object");
        allowSet.add("java.util.HashTable");
        allowSet.add("application.transplant.index.search.IndexTable");
    }
    @Override
    public DPUJClass loadClassToDPU(Class c) {
        System.out.println(c.getName().replace("/","."));
        if(!allowSet.contains(c.getName().replace("/",".")))
            return null;
        String className = formalClassName(c.getName());
        byte[] classFileBytes;

        classfileLogger.logln(" ==========--> Try load class " + className + " to dpu#" + dpuID + " <--==========");

        /** query cache **/
        DPUClassFileCacheItem cl = getLoadedClassRecord(className);
        if (cl != null) {
            classfileLogger.logln("- Class " + className + " already loaded in DPU#" + dpuID);
            return cl.dpuClassStructure;
        }

        /** get bytes of a class file **/
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream((c.getName().replace('.','/') + ".class"));

        if (is == null) {
            // TODO class name with form of "[....;" cannot be load

                System.err.println("class " + (c.getName().replace('.','/') + ".class not found"));
                return null;
        }


        try {
            classFileBytes = is.readAllBytes();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        /**
         *
         *    preliminary analyze the class
         *    place basis information to class structure
         *    generate unresolved constant table and entry table
         *    place constant data in the constant area of class structure
         *    calculate size
         *
         * **/
        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
        DPUJClass jc = classFileAnalyzer.preResolve();

        printEntryTable(jc);

        /** load super class **/
        String superClassName = jc.superClassNameIndex == 0 ? "" : getUTF8(jc, jc.superClassNameIndex);
        classfileLogger.logln("super class name = " + superClassName);
        className = formalClassName(superClassName);
        if (!"".equals(className)) {
            if (!isClassLoaded(superClassName)) {
                classfileLogger.logln(" ---- load super class " + className + ", index " + jc.superClassNameIndex);
                loadClassToDPU(c.getSuperclass());
            }
        } else {

            /** it should be java/lang/Object **/
            classfileLogger.logln(" ---- No superclass ----");
            className = formalClassName(c.getName());
            classfileLogger.logln(" - push class " + className + " to DPU#" + dpuID);

            int classAddr = upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);

            int metaspaceIndex;
            try {
                metaspaceIndex = dpujvmRemote.getMetaSpaceIndex();
                recordClass(className, jc, dpujvmRemote.pushToMetaSpace(c));

            } catch (RemoteException e) {
                throw new RuntimeException();
            }


            recordMethodDistribution(c, jc, metaspaceIndex);
            recordFieldDistribution(c, jc);

            // append virtual table's space (simulate)
            upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE,
                    ((8 * jc.methodTable.length) + 0b111) & (~0b111));
            jc.totalSize += 8 * jc.methodTable.length;

            try {
                pushJClassToDPU(jc, classAddr, dpuID);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

            // TODO, currently skip the resolution of java/framework.lang/Object.
            /** We skip all subsequent analysis of java/framework.lang/Object **/
            return jc;
        }


        int classAddr =
                upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);
        int metaspaceIndex;

        try {
            metaspaceIndex = dpujvmRemote.getMetaSpaceIndex();
            recordClass(formalClassName(c.getName()), jc, dpujvmRemote.pushToMetaSpace(c));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }


        recordMethodDistribution(c, jc, metaspaceIndex);
        recordFieldDistribution(c, jc);

        if (c.getName().equals("application.transplant.index.search.IndexTable")) {
            System.out.println();
        }

        upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE,
                ((8 * jc.methodTable.length) + 0b111) & (~0b111));
        jc.totalSize += 8 * jc.methodTable.length;

        /** resolve each entry item from preprocessed entry table. **/

        for (int i = 0; i < jc.cpItemCount; i++) {
            int tag = (int) ((jc.entryItems[i] >> 56) & 0xFF);
            int classIndex;
            int nameAndTypeIndex;
            switch (tag) {
                case ClassFileAnalyzerConstants.CT_Class:
                    classfileLogger.logln("In #" + (i) + " ClassRef: ");
                    String classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[i]) & 0xFFFF));
                    classfileLogger.logln("className = " + classNameUTF8 + ", index = #" + (jc.entryItems[i] & 0xFFFF));
                    DPUClassFileCacheItem cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(classNameUTF8);
                    if (cacheLine != null) {
                        classfileLogger.logf("class %s loaded, mram addr = 0x%x\n", classNameUTF8, cacheLine.marmAddr);
                    } else {
                        if (allowSet.contains(classNameUTF8)) {
                            try {
                                // TODO className$1 loading..
                                loadClassToDPU(Class.forName(classNameUTF8.replace("/", ".")));

                            } catch (ClassNotFoundException e) {
                                classfileLogger.logln("cannot find class " + classNameUTF8);
                            }
                        } else {
                            classfileLogger.logln("ignore " + classNameUTF8);
                        }
                    }


                    break;
                case ClassFileAnalyzerConstants.CT_Fieldref:
                    classfileLogger.logln("In #" + (i) + " FieldRef: ");
                    classIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
                    nameAndTypeIndex = (int) ((jc.entryItems[i]) & 0xFFFF);
                    classNameUTF8 = getUTF8(jc, (int) ((jc.entryItems[classIndex] >> 32) & 0xFFFF));
                    classfileLogger.logln("class name = " + classNameUTF8);

                    try {
                        loadClassToDPU(Class.forName(classNameUTF8.replace("/", ".")));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }


                    String fieldName = getUTF8(jc, (int) ((jc.entryItems[nameAndTypeIndex] >> 16) & 0xFFFF));
                    String fieldType = getUTF8(jc, (int) ((jc.entryItems[nameAndTypeIndex]) & 0xFFFF));
                    classfileLogger.logln("fieldName = " + fieldName + ", fieldType = " + fieldType);
                    try {
                        loadClassesToDPUFromDescriptor(fieldType);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }


                    break;
                case ClassFileAnalyzerConstants.CT_Methodref:
                    classfileLogger.logln("In #" + (i) + " MethodRef: ");
                    int classCPIndex = (int) ((jc.entryItems[i] >> 16) & 0xFFFF);
                    int nameAndTypeCPIndex = (int) ((jc.entryItems[i]) & 0xFFFF);
                    int nameCPIndex = (int) ((jc.entryItems[nameAndTypeCPIndex] >> 16) & 0xFFFF);
                    int typeCPIndex = (int) ((jc.entryItems[nameAndTypeCPIndex]) & 0xFFFF);
                    int classNameUTF8CPIndex = (int) ((jc.entryItems[classCPIndex]) & 0xFFFF);
                    String methodClassNameUTF8 = getUTF8(jc, (int) (classNameUTF8CPIndex));
                    String methodNameUTF8 = getUTF8(jc, (int) (nameCPIndex));
                    String methodTypeUTF8 = getUTF8(jc, (int) (typeCPIndex));



                    classfileLogger.logln("description = " + methodClassNameUTF8 + "." + methodNameUTF8 + ":" + methodTypeUTF8 + ":::" + c.getName());
                    String descriptor = methodNameUTF8 + ":" + methodTypeUTF8;
                    DPUMethodCacheItem methodCacheItem = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getMethodCacheItem(
                            methodClassNameUTF8, descriptor
                    );

                    cacheLine =
                            upmem.getDPUManager(dpuID).classCacheManager.dpuClassCache.cache.get(methodClassNameUTF8);

                    DPUJClass dpujClass = null;
                    try {
                        dpujClass = loadClassesToDPUFromDescriptorSingle(methodClassNameUTF8);
                    } catch (ClassNotFoundException e) {
                        continue;
                    }
                    if(dpujClass == null) continue;
                    DPUJClass methodJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure(methodClassNameUTF8);
                    if(methodCacheItem == null || methodJc == null) continue;

                    String methodName = getUTF8(methodJc, methodCacheItem.dpujMethod.nameIndex);
                    classfileLogger.logln("methodName = " + methodName);
                    String TypeDesc = getUTF8(methodJc, methodCacheItem.dpujMethod.descriptorIndex);
                    classfileLogger.logln("type desc = " + TypeDesc);
                    String returnVal = TypeDesc.substring(1).split("\\)")[1];
                    classfileLogger.logln("return val = " + returnVal);
                    String paramsDesc = TypeDesc.substring(1).split("\\)")[0];
                    classfileLogger.logln("params desc = " + paramsDesc);


                    if (returnVal.length() > 1) {
                        try {
                            loadClassesToDPUFromDescriptor(returnVal);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

            }


            DPUCacheManager classCacheManager = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
            try {
                pushJClassToDPU(jc, classAddr, dpuID);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

        }
        return jc;

    }

}
