package pim.dpu.cache;

import pim.dpu.java_strut.DPUJClass;
import pim.dpu.java_strut.DPUJMethod;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public abstract class DPUCacheManager {
    public DPUClassCache dpuClassCache;
    public DPUMethodCache methodCache;
    public DPUFieldCache fieldCache;
    protected int dpuID;

    protected Logger pimCacheLogger = PIMLoggers.pimCacheLogger;
//
//    public DPUCacheManager(int dpuID, DPUJVMRemote DPUJVMRemote) {
//
//        this.dpuID = dpuID;
//        this.DPUJVMRemote = DPUJVMRemote;
//        this.dpuClassCache = new DPUClassCache();
//        this.methodCache = new DPUMethodCache();
//        this.fieldCache = new DPUFieldCache();
//    }

    public static class DPUClassCache {
        public Dictionary<String, DPUClassFileCacheItem> cache = new Hashtable<>();
        public List<DPUClassFileCacheItem> dpuClassFileCacheItemList = new ArrayList<>();
    }

    public static class DPUMethodCache {
        public Dictionary<String, Dictionary<String, DPUMethodCacheItem>> cache = new Hashtable<>();
    }
    
    public static class DPUFieldCache{
        public Dictionary<String, Dictionary<String, DPUFieldCacheItem>> cache = new Hashtable<>();
    }


    public abstract DPUFieldCacheItem getFieldCacheItem(String className, String fieldName);


    public abstract void setFieldCacheItem(String className, String fieldName, int indexInInstance);
    public abstract DPUMethodCacheItem getMethodCacheItem(String classDesc, String methodDesc);
    public abstract void setMethodCacheItem(String classDesc, String methodDesc, int marmAddr, DPUJMethod dpujMethod);
    public abstract DPUClassFileCacheItem getClassStrutCacheLine(String desc);

    public abstract DPUJClass getClassStructure(String desc) ;
    public abstract void setClassStructure(String desc, DPUJClass dpuClassStrut, int marmAddr);
}
