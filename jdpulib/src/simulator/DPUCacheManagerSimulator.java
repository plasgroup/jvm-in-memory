package simulator;

import framework.pim.dpu.cache.DPUCacheManager;
import framework.pim.dpu.cache.DPUClassFileLookupTableItem;
import framework.pim.dpu.cache.DPUFieldLookupTableItem;
import framework.pim.dpu.cache.DPUMethodLookupTableItem;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJMethod;
import framework.pim.utils.ClassLoaderUtils;


import java.util.Dictionary;
import java.util.Hashtable;

public class DPUCacheManagerSimulator extends DPUCacheManager {
    private DPUJVMRemote dpujvmRemote; // instance of remote simulated JVM


    /** constructor **/
    public DPUCacheManagerSimulator(int dpuID, simulator.DPUJVMRemote dpujvmRemote)
    {
        this.dpuID = dpuID;
        this.dpujvmRemote = dpujvmRemote;
        this.dpuClassLookupTable = new DPUClassLookupTable();
        this.methodCache = new DPUMethodLookupTable();
        this.fieldCache = new DPUFieldLookupTable();
    }


    @Override
    public DPUFieldLookupTableItem getFieldLookupTableItem(String className, String fieldName) {
        if(fieldCache.cache.get(className) == null) return null;
        return fieldCache.cache.get(className).get(fieldName);
    }


    @Override
    public void setFieldLookupTableItem(String className, String fieldName, int indexInInstance) {
        Dictionary<String, DPUFieldLookupTableItem> fieldCacheOfClass = fieldCache.cache.get(className);
        if (fieldCacheOfClass == null) {
            fieldCacheOfClass = new Hashtable<>();
            fieldCache.cache.put(className, fieldCacheOfClass);
        }
        DPUFieldLookupTableItem fieldCacheItem = new DPUFieldLookupTableItem();
        fieldCacheItem.indexInInstance = indexInInstance;
        this.fieldCache.cache.get(className).put(fieldName, fieldCacheItem);

        pimCacheLogger.logf("set field: " + fieldName + " of class " + className + " to " +
                dpujvmRemote + " v(index) = %x\n", indexInInstance);
    }

    @Override
    public DPUMethodLookupTableItem getMethodLookupTableItem(String classDesc, String methodDesc) {
        pimCacheLogger.log("get DPUID = " + dpuID + " method =  " + methodDesc + " of class " +
                classDesc + " from cache");
        if(methodCache.cache.get(classDesc) == null) return null;
        return methodCache.cache.get(classDesc).get(methodDesc);
    }

    @Override
    public void setMethodLookupTableItem(String classDesc, String methodDesc, int marmAddr, DPUJMethod dpujMethod) {
        Dictionary<String, DPUMethodLookupTableItem> classCacheItem = methodCache.cache.get(classDesc);
        if(classCacheItem == null) {
            classCacheItem = new Hashtable<>();
            methodCache.cache.put(classDesc, classCacheItem);
        }
        DPUMethodLookupTableItem dpuMethodLookupTableItem = new DPUMethodLookupTableItem();
        dpuMethodLookupTableItem.mramAddr = marmAddr;
        dpuMethodLookupTableItem.dpujMethod = dpujMethod;
        this.methodCache.cache.get(classDesc).put(methodDesc, dpuMethodLookupTableItem);
        pimCacheLogger.logf("set method: " + methodDesc + " of class " + classDesc + " to " + dpujvmRemote + " v = %x\n", marmAddr);
    }

    @Override
    public DPUClassFileLookupTableItem getClassLookupTableItem(String desc) {
        pimCacheLogger.logf("get DPUID = " + dpuID + " class =  " + desc + " from cache");
        return dpuClassLookupTable.cache.get(desc);
    }


    @Override
    public DPUJClass getClassStructure(String desc) {
        DPUClassFileLookupTableItem classFileCacheLine = getClassLookupTableItem(desc);
        if(classFileCacheLine == null) return null;
        return classFileCacheLine.dpuClassStructure;
    }


    @Override
    public void setClassStructure(String desc, DPUJClass dpuClassStrut, int marmAddr) {
        DPUClassFileLookupTableItem classFileCacheLine = getClassLookupTableItem(desc);
        if(classFileCacheLine == null) {
            DPUClassFileLookupTableItem dpuClassFileLookupTableItem = new DPUClassFileLookupTableItem();
            dpuClassFileLookupTableItem.classId = dpuClassLookupTable.cache.size();
            dpuClassFileLookupTableItem.marmAddr = marmAddr;
            dpuClassFileLookupTableItem.dpuClassStructure = dpuClassStrut;
            dpuClassLookupTable.cache.put(desc, dpuClassFileLookupTableItem);
            dpuClassLookupTable.dpuClassFileLookupTableItemList.add(dpuClassFileLookupTableItem);
        }

        dpuClassLookupTable.cache.get(desc).dpuClassStructure = dpuClassStrut;
        dpuClassLookupTable.cache.get(desc).marmAddr = marmAddr;
        pimCacheLogger.logf("set " + dpuClassStrut + " to " + dpujvmRemote + ", key=" + desc + ", val = %x"  + " class name = " + ClassLoaderUtils.getUTF8(dpuClassStrut, dpuClassStrut.thisClassNameIndex) + "\n", marmAddr);
    }
}
