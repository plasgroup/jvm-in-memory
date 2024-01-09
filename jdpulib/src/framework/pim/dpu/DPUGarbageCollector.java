package framework.pim.dpu;

import com.upmem.dpu.Dpu;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;
import framework.lang.struct.DPUObjectHandler;

public abstract class DPUGarbageCollector {
    public int dpuID;
    Dpu dpu;
    protected int heapSpacePt;
    protected int metaSpacePt;
    public final static int heapSpaceBeginAddr = 0x90000;
    public final static int metaSpaceBeginAddr = 0x3090000;
    public final static int parameterBufferBeginAddr = 0x3890000;
    public final static int parameterBufferSize = 6 * 1024;
    public final static int perDPUBufferSize = 6 * 1024 / 24;
    public final static int heapSpaceSize = 48 * 1024 * 1024;
    public final static int metaSpaceSize = 16 * 1024 * 1024;

    protected Logger gcLogger = PIMLoggers.gcLogger;




    public abstract int getHeapSpacePt();
    public abstract int getMetaSpacePt();
    public abstract void updateHeapPointerToDPU();

    public abstract void updateMetaSpacePointerToDPU();

    public abstract int pushParameters(int[] params);

    public abstract int pushParameters(int[] params, int tasklet);
    public abstract void readBackHeapSpacePt();
    public abstract void readBackMetaSpacePt();
    public abstract void transfer(DPUJVMMemSpaceKind spaceKind, byte[] data, int pt);
    public abstract int allocate(DPUJVMMemSpaceKind spaceKind, byte[] data);
    public abstract int allocate(DPUJVMMemSpaceKind spaceKind, int size);
    public static DPUObjectHandler dpuAddress2ObjHandler(int addr, int dpuID) {
        DPUObjectHandler handler = new DPUObjectHandler(dpuID, addr);
        return handler;
    }

    public abstract int freeFromBack(DPUJVMMemSpaceKind spaceKind, int size);

    public abstract int getRemainHeapMemory();

    public abstract int getRemainMetaMemory();

    public abstract int getReturnVal();

    public abstract int getInt32(int i);
}
