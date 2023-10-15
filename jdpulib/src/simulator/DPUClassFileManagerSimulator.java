package simulator;

import com.upmem.dpu.DpuException;
import pim.UPMEM;
import pim.dpu.cache.DPUCacheManager;
import pim.dpu.cache.DPUClassFileCacheItem;
import pim.dpu.cache.DPUFieldCacheItem;
import pim.dpu.cache.DPUMethodCacheItem;
import pim.dpu.classloader.ClassFileAnalyzer;
import pim.dpu.classloader.ClassFileAnalyzerConstants;
import pim.dpu.classloader.DPUClassFileManager;
import pim.dpu.java_strut.*;
import pim.utils.BytesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.*;

import static pim.dpu.classloader.ClassWriter.pushJClassToDPU;
import static pim.utils.ClassLoaderUtils.*;
import static pim.utils.ClassLoaderUtils.getMethodDescriptor;

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
            List<Class> classes = descriptorToClasses(desc.substring(1, desc.indexOf(')')));
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


    List<Class> descriptorToClasses(String desc){
        System.out.println("parse " + desc);
        String matched = "";
        int state = 0;
        List<Class> classes = new ArrayList<>();
        for(int ci = 0; ci < desc.length(); ci++){
            char ch = desc.charAt(ci);
            if(state == 0){
                switch (ch){
                    case 'B':
                        classes.add(byte.class);
                        break;
                    case 'C':
                        classes.add(char.class);
                        break;
                    case 'D':
                        classes.add(double.class);
                        break;
                    case 'F':
                        classes.add(float.class);
                        break;
                    case 'I':
                        classes.add(int.class);
                        break;
                    case 'J':
                        classes.add(long.class);
                        break;
                    case 'S':
                        classes.add(short.class);
                        break;
                    case 'Z':
                        classes.add(boolean.class);
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

                    Class c = null;
                    try {
                        c = Class.forName(matched.replace("/", "."));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
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
            DPUJClass superClassJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(superClassName).dpuClassStructure;
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
                            DPUJClass methodReferenceJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStrutCacheLine(className).dpuClassStructure;

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
                Arrays.stream(jc.fields).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElse(0) +
                Arrays.stream(jc.methodTable).map(e -> e.size).reduce((s1, s2) -> s1 + s2).orElse(0)
                + ((jc.stringINTConstantPoolLength + 0b111) & (~0b111))
                + ((8 * jc.virtualTable.items.size()) + 0b111 & (~0b111));
        ;
    }

    @Override
    public DPUJClass loadClassForDPU(Class c) {

        String className = formalClassName(c.getName());
        classfileLogger.logln(" ==========--> Try load class " + className + " to dpu#" + dpuID + " <--==========");

        // query cache
        DPUClassFileCacheItem cl = getLoadedClassRecord(className);
        if(cl != null){
            classfileLogger.logln("- Class " + className + " already loaded in DPU#" + dpuID);
            return cl.dpuClassStructure;
        }

        // get bytes of class file
        InputStream is = c.getResourceAsStream((c.getSimpleName().split("\\$")[0] + ".class"));
        if(is == null) {
            // TODO class name with form of "[....;" cannot be load
            try {
                throw new IOException("cannot find class " + c.getSimpleName().split("\\$")[0] + ".class");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        byte[] classFileBytes = new byte[0];
        try {
            classFileBytes = is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // preliminary analysis the Class
        ClassFileAnalyzer classFileAnalyzer = ClassFileAnalyzer.fromClassBytes(classFileBytes);
        DPUJClass jc = classFileAnalyzer.preResolve();

        // load super class recursively
        String superClassName = jc.superClassNameIndex == 0 ? "" : getUTF8(jc, jc.superClassNameIndex);
        classfileLogger.logln("super class name = " + superClassName);
        className = formalClassName(superClassName);
        if(!"".equals(className)) {
            if(!isClassLoaded(superClassName)){
                classfileLogger.logln(" ---- load super class " + className + ", index " + jc.superClassNameIndex);
                loadClassForDPU(c.getSuperclass());
            }
        }else{
            classfileLogger.logln(" ---- No superclass ----");
            className = formalClassName(c.getName());
            classfileLogger.logln(" - Push class " + className + " to DPU#" + dpuID);

            int classAddr = upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);

            int metaspaceIndex;
            try {
                  metaspaceIndex = dpujvmRemote.getMetaSpaceIndex();
            } catch (RemoteException e) {
                throw new RuntimeException();
            }
            try {
                recordClass(className, jc, dpujvmRemote.pushToMetaSpace(c));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            recordMethodDistribution(c, jc, metaspaceIndex);
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

            try {
                pushJClassToDPU(jc, classAddr , dpuID);

            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

            // TODO, currently skip the resolution of java/lang/Object.
            /** We skip all subsequent analysis of java/lang/Object **/
            return jc;
        }


        int classAddr =
                upmem.getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_METASPACE, jc.totalSize);
        int metaspaceIndex;

        try {
            metaspaceIndex = dpujvmRemote.getMetaSpaceIndex();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try {
            recordClass(formalClassName(c.getName()), jc, dpujvmRemote.pushToMetaSpace(c));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        recordMethodDistribution(c, jc, metaspaceIndex);
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

                    DPUJClass methodJc = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure(vItem.className);
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
                    jc.entryItems[i] |= ((long) BytesUtils.readU2BigEndian(classFileBytes, jc.itemBytesEntries[i] + 1) << 48) & 0xFFFF000000000000L;
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
        UPMEM.getInstance().getDPUManager(dpuID).classCacheManager.getClassStructure(formalClassName(c.getName()))
                .virtualTable = jc.virtualTable;
        DPUCacheManager classCacheManager = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
        try {
            pushJClassToDPU(jc, classAddr, dpuID);

        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        return jc;
    }
}
