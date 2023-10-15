package pim.dpu;

import com.upmem.dpu.Dpu;
import pim.dpu.cache.DPUCacheManager;
import pim.dpu.cache.DPUClassFileCacheItem;
import pim.dpu.cache.DPUFieldCacheItem;
import pim.dpu.cache.DPUMethodCacheItem;
import pim.dpu.java_strut.DPUJClass;
import pim.dpu.java_strut.DPUJMethod;
import pim.utils.ClassLoaderUtils;

import java.util.Dictionary;
import java.util.Hashtable;

public class DPUCacheManagerUPMEM extends DPUCacheManager {
    Dpu dpu;

    public DPUCacheManagerUPMEM(int dpuID, Dpu dpu){
        this.dpuID = dpuID;
        this.dpu = dpu;
        this.dpuClassCache = new DPUClassCache();
        this.methodCache = new DPUMethodCache();
        this.fieldCache = new DPUFieldCache();
    }


    @Override
    public DPUFieldCacheItem getFieldCacheItem(String className, String fieldName) {
        pimCacheLogger.log("get DPUID = " + dpuID + " field =  " + fieldName + ", of class " + className + " from cache");
        if(fieldCache.cache.get(className) == null) return null;
        return fieldCache.cache.get(className).get(fieldName);
    }
    @Override
    public void setFieldCacheItem(String className, String fieldName, int indexInInstance)  {
        Dictionary<String, DPUFieldCacheItem> fieldCacheOfClass = fieldCache.cache.get(className);
        if (fieldCacheOfClass == null) {
            fieldCacheOfClass = new Hashtable<>();
            fieldCache.cache.put(className, fieldCacheOfClass);
        }
        DPUFieldCacheItem fieldCacheItem = new DPUFieldCacheItem();
        fieldCacheItem.indexInInstance = indexInInstance;
        this.fieldCache.cache.get(className).put(fieldName, fieldCacheItem);

        pimCacheLogger.logf("set field: " + fieldName + " of class " + className + " to " + dpu + " v(index) = %x\n", indexInInstance);
    }

    @Override
    public DPUMethodCacheItem getMethodCacheItem(String classDesc, String methodDesc){
        pimCacheLogger.log("get DPUID = " + dpuID + " method =  " + methodDesc + " of class " + classDesc + " from cache");
        if(methodCache.cache.get(classDesc) == null) return null;
        return methodCache.cache.get(classDesc).get(methodDesc);
    }
    @Override
    public void setMethodCacheItem(String classDesc, String methodDesc, int marmAddr, DPUJMethod dpujMethod){
        Dictionary<String, DPUMethodCacheItem> classCacheItem = methodCache.cache.get(classDesc);
        if(classCacheItem == null) {
            classCacheItem = new Hashtable<>();
            methodCache.cache.put(classDesc, classCacheItem);
        }
        DPUMethodCacheItem dpuMethodCacheItem = new DPUMethodCacheItem();
        dpuMethodCacheItem.mramAddr = marmAddr;
        dpuMethodCacheItem.dpujMethod = dpujMethod;
        this.methodCache.cache.get(classDesc).put(methodDesc, dpuMethodCacheItem);
        pimCacheLogger.logf("set method: " + methodDesc + " of class " + classDesc + " to " + dpu + " v = %x\n", marmAddr);
    }

    @Override
    public DPUClassFileCacheItem getClassStrutCacheLine(String desc)  {
        pimCacheLogger.logf("get DPUID = " + dpuID + " class =  " + desc + " from cache");
        return dpuClassCache.cache.get(desc);
    }
    @Override
    public DPUJClass getClassStructure(String desc){
        DPUClassFileCacheItem classFileCacheLine = getClassStrutCacheLine(desc);
        if(classFileCacheLine == null) return null;
        return classFileCacheLine.dpuClassStructure;
    }

    @Override
    public void setClassStructure(String desc, DPUJClass dpuClassStrut, int marmAddr) {
        DPUClassFileCacheItem classFileCacheLine = getClassStrutCacheLine(desc);
        if(classFileCacheLine == null) {
            DPUClassFileCacheItem dpuClassFileCacheItem = new DPUClassFileCacheItem();
            dpuClassFileCacheItem.classId = dpuClassCache.cache.size();
            dpuClassFileCacheItem.marmAddr = marmAddr;
            dpuClassFileCacheItem.dpuClassStructure = dpuClassStrut;
            dpuClassCache.cache.put(desc, dpuClassFileCacheItem);
            dpuClassCache.dpuClassFileCacheItemList.add(dpuClassFileCacheItem);
        }

        dpuClassCache.cache.get(desc).dpuClassStructure = dpuClassStrut;
        dpuClassCache.cache.get(desc).marmAddr = marmAddr;
        pimCacheLogger.logf("set " + dpuClassStrut + " to " + dpu + ", key=" + desc + ", val = %x"  + " class name = " + ClassLoaderUtils.getUTF8(dpuClassStrut, dpuClassStrut.thisClassNameIndex) + "\n", marmAddr);
    }
}
