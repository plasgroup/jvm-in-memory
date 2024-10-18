package pim;

import pim.dpu.DPUManager;
import pim.dpu.DPUObjectHandler;
import pim.dpu.PIMManager;
import pim.logger.Logger;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * UPMEM class
 * - The facade of the UPMEM library
 * - The UPMEM class use singleton pattern, you need use getInstance() to get an
 * instance.
 * - It should call the initialization() method before get and instance
 * -
 **/

public class UPMEM {
    /* Configurations Fields */
    public static final int TOTAL_DPU_COUNT = 1;
    public static final int TOTAL_HARDWARE_THREADS_COUNT = 1;
    public static int dpuInUse = 1;
    public static int perDPUThreadsInUse = TOTAL_HARDWARE_THREADS_COUNT;

    /* Facade Class for PIM management */
    private static PIMManager pimManager;

    /* Singleton */
    private static volatile UPMEM instance = null;
    private static final Object locker = new Object();

    public static void endRecordBatchDispatching() {
        batchDispatchingRecording = false;
    }

    public static boolean batchDispatchingRecording = false;
    public static BatchDispatcher batchDispatcher;

    public static void beginRecordBatchDispatching(BatchDispatcher batchDispatcher) {
        UPMEM.batchDispatcher = batchDispatcher;
        batchDispatchingRecording = true;
    }

    static Logger upmemLogger = Logger.getLogger("pim:upmem");
    {
        upmemLogger.setEnable(false);
    }

    public DPUManager getDPUManager(int dpuID) {
        return pimManager.getDPUManager(dpuID);
    }

    /*
     * Unsafe class. It will be used to create a proxy class instance without
     * initialize it
     */
    static Unsafe unsafe;
    static {
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe) singleoneInstanceField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * create a proxy object of a given class, by using a pim object handler
     * (contains a dpuID and an object instance address in DPU MRAM)
     */
    public static IDPUProxyObject generateProxyObject(Class proxyClass, int dpuID, int address)
            throws NoSuchFieldException, InstantiationException {
        IDPUProxyObject proxyObject;
        proxyObject = (IDPUProxyObject) unsafe.allocateInstance(proxyClass);
        long dpuIDOffset = unsafe.objectFieldOffset(proxyClass.getField("dpuID"));
        unsafe.getAndSetObject(proxyObject, dpuIDOffset, dpuID);
        long addressOffset = unsafe.objectFieldOffset(proxyClass.getField("address"));
        unsafe.getAndSetObject(proxyObject, addressOffset, address);

        return proxyObject;
    }

    /*
     * Create a proxy object of a given class, by create a new object at the DPU
     * side.
     * - parameter 1: the dpu id that indicate in which dpu should the new object be
     * created.
     * - parameter 2: the class for which create a proxy object of it
     * - parameter 3~: the arguments for initialization method call
     */
    public IDPUProxyObject createObject(int dpuID, Class objectClass, Object... arguments) {
        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objectClass, arguments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "ProxyAutoGen <----");

        try {
            Class pClass = Class.forName(objectClass.getName() + "ProxyAutoGen");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }

    public static UPMEM initialize() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null)
                    instance = new UPMEM();
                pimManager = new PIMManagerUPMEM().init(dpuInUse);
            }
        }
        return instance;
    }

    public static UPMEM initialize(UPMEMConfigurator configurator) {
        perDPUThreadsInUse = configurator.getThreadPerDPU();
        dpuInUse = configurator.getDpuInUseCount();
        return initialize();
    }

    public static UPMEM getInstance() {
        if (instance == null)
            throw new RuntimeException("UPMEM class has not been initialized.");
        return instance;
    }

    private UPMEM() {
    }

    /* Facade methods */
    public int getDPUHeapMemoryRemain(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainHeapMemory();
    }

    public int getDPUMetaSpaceMemoryRemain(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainMetaMemory();
    }

}
