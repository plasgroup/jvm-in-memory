package framework.pim.dpu.classloader;

import com.upmem.dpu.Dpu;
import framework.pim.dpu.java_strut.DPUJClass;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;
import framework.pim.UPMEM;

public abstract class DPUClassFileManager {
    protected static Logger classfileLogger = PIMLoggers.classfileLogger;

    public int dpuID;
    protected Dpu dpu;
    public UPMEM upmem = UPMEM.getInstance();

//    public DPUClassFileManager(int dpuID, DPUJVMRemote registry) {
//        this.dpuID = dpuID;
//        this.DPUJVMRemote = registry;
//    }

    public abstract void recordClass(String className, DPUJClass jc, int classMramAddr);

    public abstract DPUJClass loadClassToDPU(Class c);




}

