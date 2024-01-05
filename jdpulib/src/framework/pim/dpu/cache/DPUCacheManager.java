package framework.pim.dpu.cache;

import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.dpu.java_strut.DPUJMethod;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


/** for saving loaded JVM structures at the host **/
public abstract class DPUCacheManager {
    public DPUClassLookupTable dpuClassLookupTable;
    public DPUMethodLookupTable methodCache;
    public DPUFieldLookupTable fieldCache;
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


    public static class DPUClassLookupTable {
        public Dictionary<String, DPUClassFileLookupTableItem> cache = new Hashtable<>();
        public List<DPUClassFileLookupTableItem> dpuClassFileLookupTableItemList = new ArrayList<>();
    }

    public static class DPUMethodLookupTable {
        public Dictionary<String, Dictionary<String, DPUMethodLookupTableItem>> cache = new Hashtable<>();
    }
    
    public static class DPUFieldLookupTable {
        public Dictionary<String, Dictionary<String, DPUFieldLookupTableItem>> cache = new Hashtable<>();
    }


    public abstract DPUFieldLookupTableItem getFieldLookupTableItem(String className, String fieldName);


    public abstract void setFieldLookupTableItem(String className, String fieldName, int indexInInstance);
    public abstract DPUMethodLookupTableItem getMethodLookupTableItem(String classDesc, String methodDesc);
    public abstract void setMethodLookupTableItem(String classDesc, String methodDesc, int marmAddr, DPUJMethod dpujMethod);
    public abstract DPUClassFileLookupTableItem getClassLookupTableItem(String desc);

    public abstract DPUJClass getClassStructure(String desc) ;
    public abstract void setClassStructure(String desc, DPUJClass dpuClassStrut, int marmAddr);
}
