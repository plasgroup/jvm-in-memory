package framework.pim.dpu;
import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import framework.pim.UPMEM;
import framework.pim.dpu.cache.DPUCacheManager;
import framework.pim.dpu.classloader.DPUClassFileManager;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;
import framework.lang.struct.DPUObjectHandler;

import java.io.IOException;
import java.io.PrintStream;

public abstract class DPUManager {
    public int dpuID;
    public DPUGarbageCollector garbageCollector;
    public DPUClassFileManager dpuClassFileManager;
    public DPUCacheManager classCacheManager;

    public Dpu dpu;

    public byte[] dispatchingBuffer = new byte[1024];

    protected Logger dpuManagerLogger = PIMLoggers.dpuManagerLogger;
    protected int currentTasklet = 0;
    protected int[] taskletSemaphore = new int[UPMEM.TOTAL_HARDWARE_THREADS_COUNT];


    public abstract void dpuExecute(PrintStream printStream) throws DpuException;

    public abstract void callNonstaticMethod(int classPt, int methodPt, int instanceAddr, Object[] params);

    protected int calcFieldCount(Class c){
        if(c.getSuperclass() == null){
            return c.getDeclaredFields().length;
        }
        return calcFieldCount(c.getSuperclass()) + c.getDeclaredFields().length;
    }

    protected String generateInitializationDescriptor(Object[] params){
        String desc = "<init>:(";
        for(Object obj : params){
            if(obj instanceof Integer){
                desc += "I";
            }else {
                desc += "L" + obj.getClass().getName().replace(".", "/");
            }
        }

        return desc + ")V";
    }

    public abstract  <T> DPUObjectHandler createObject(Class c, Object... params) throws IOException;

    protected DPUManager(){}


}
