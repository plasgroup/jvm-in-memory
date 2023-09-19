package pim.dpu.classloader;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import pim.ExperimentConfigurator;
import pim.dpu.cache.DPUCacheManager;
import pim.dpu.cache.DPUClassFileCacheItem;
import pim.dpu.cache.DPUFieldCacheItem;
import pim.dpu.cache.DPUMethodCacheItem;
import pim.dpu.java_strut.VirtualTable;
import pim.dpu.java_strut.VirtualTableItem;
import pim.dpu.java_strut.DPUJClass;
import pim.dpu.java_strut.DPUJMethod;
import pim.dpu.java_strut.DPUJVMMemSpaceKind;
import pim.logger.Logger;
import pim.logger.PIMLoggers;
import pim.utils.BytesUtils;
import pim.UPMEM;
import simulator.DPUJVMRemote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import static pim.dpu.classloader.ClassWriter.pushJClassToDPU;
import static pim.utils.ClassLoaderUtils.*;

public abstract class DPUClassFileManager {
    protected Logger classfileLogger = PIMLoggers.classfileLogger;

    public int dpuID;
    protected Dpu dpu;
    public UPMEM upmem = UPMEM.getInstance();

//    public DPUClassFileManager(int dpuID, DPUJVMRemote registry) {
//        this.dpuID = dpuID;
//        this.DPUJVMRemote = registry;
//    }



    public abstract void recordClass(String className, DPUJClass jc, int classMramAddr);



    public abstract DPUJClass loadClassForDPU(Class c);




}

