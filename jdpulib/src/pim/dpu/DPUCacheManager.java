package pim.dpu;

import com.upmem.dpu.Dpu;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class DPUCacheManager {
    int dpuID;
    Dpu dpu;

    static class DPUClassCache {
        public Dictionary<String, DPUClassFileCacheItem> cache = new Hashtable<>();
        public List<DPUClassFileCacheItem> dpuClassFileCacheItemList = new ArrayList<>();
    }

    static class DPUMethodCache {
        public Dictionary<String, Dictionary<String, DPUMethodCacheItem>> cache = new Hashtable<>();
    }
    
    static class DPUFieldCache{
        public Dictionary<String, Dictionary<String, DPUFieldCacheItem>> cache = new Hashtable<>();
    }

    DPUClassCache dpuClassCache;
    DPUMethodCache methodCache;
    DPUFieldCache fieldCache;


    public DPUCacheManager(int dpuID, Dpu dpu){
        this.dpuID = dpuID;
        this.dpu = dpu;
        this.dpuClassCache = new DPUClassCache();
        this.methodCache = new DPUMethodCache();
        this.fieldCache = new DPUFieldCache();
    }

    public DPUFieldCacheItem getFieldCacheItem(String className, String fieldName){
        System.out.println("get DPUID = " + dpuID + " field =  " + fieldName + ", of class " + className + " from cache");
        if(fieldCache.cache.get(className) == null) return null;
        return fieldCache.cache.get(className).get(fieldName);
    }


    public void setFieldCacheItem(String className, String fieldName, int indexInInstance) {
        Dictionary<String, DPUFieldCacheItem> fieldCacheOfClass = fieldCache.cache.get(className);
        if (fieldCacheOfClass == null) {
            fieldCacheOfClass = new Hashtable<>();
            fieldCache.cache.put(className, fieldCacheOfClass);
        }
        DPUFieldCacheItem fieldCacheItem = new DPUFieldCacheItem();
        fieldCacheItem.indexInInstance = indexInInstance;
        this.fieldCache.cache.get(className).put(fieldName, fieldCacheItem);

        System.out.printf("set field: " + fieldName + " of class " + className + " to " + dpu + " v(index) = %x\n", indexInInstance);
    }



    public DPUMethodCacheItem getMethodCacheItem(String classDesc, String methodDesc){
        System.out.println("get DPUID = " + dpuID + " method =  " + methodDesc + " of class " + classDesc + " from cache");
        if(methodCache.cache.get(classDesc) == null) return null;
        return methodCache.cache.get(classDesc).get(methodDesc);
    }
    public void setMethodCacheItem(String classDesc, String methodDesc, int marmAddr, DPUJMethod dpujMethod) {
        Dictionary<String, DPUMethodCacheItem> classCacheItem = methodCache.cache.get(classDesc);
        if(classCacheItem == null) {
            classCacheItem = new Hashtable<>();
            methodCache.cache.put(classDesc, classCacheItem);
        }
        DPUMethodCacheItem dpuMethodCacheItem = new DPUMethodCacheItem();
        dpuMethodCacheItem.mramAddr = marmAddr;
        dpuMethodCacheItem.dpujMethod = dpujMethod;
        this.methodCache.cache.get(classDesc).put(methodDesc, dpuMethodCacheItem);

        System.out.printf("set method: " + methodDesc + " of class " + classDesc + " to " + dpu + " v = %x\n", marmAddr);
    }

    public DPUClassFileCacheItem getClassStrutCacheLine(String desc) {
        System.out.println("get DPUID = " + dpuID + " class =  " + desc + " from cache");
        return dpuClassCache.cache.get(desc);
    }



    public DPUJClass getClassStrut(String desc) {
        DPUClassFileCacheItem classFileCacheLine = getClassStrutCacheLine(desc);
        if(classFileCacheLine == null) return null;
        return classFileCacheLine.dpuClassStrut;
    }

    public void setClassStrut(String desc, DPUJClass dpuClassStrut, int marmAddr) {

        DPUClassFileCacheItem classFileCacheLine = getClassStrutCacheLine(desc);
        if(classFileCacheLine == null) {
            DPUClassFileCacheItem dpuClassFileCacheItem = new DPUClassFileCacheItem();
            dpuClassFileCacheItem.classId = dpuClassCache.cache.size();
            dpuClassFileCacheItem.marmAddr = marmAddr;
            dpuClassFileCacheItem.dpuClassStrut = dpuClassStrut;
            dpuClassCache.cache.put(desc, dpuClassFileCacheItem);
            dpuClassCache.dpuClassFileCacheItemList.add(dpuClassFileCacheItem);
        }

        dpuClassCache.cache.get(desc).dpuClassStrut = dpuClassStrut;
        dpuClassCache.cache.get(desc).marmAddr = marmAddr;
        System.out.printf("set " + dpuClassStrut + " to " + dpu + ", key=" + desc + ", val = %x"  + " class name = " + DPUClassFileManager.getUTF8(dpuClassStrut, dpuClassStrut.thisClassNameIndex) + "\n", marmAddr);
    }
}
