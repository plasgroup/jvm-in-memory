package framework.pim;

import application.transplant.index.search.Searcher;
import framework.lang.struct.dist.DPUInt32ArrayHandler;
import framework.pim.dpu.DPUManager;
import framework.lang.struct.DPUObjectHandler;
import framework.pim.dpu.PIMManager;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.logger.Logger;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.utils.BytesUtils;

import simulator.PIMManagerSimulator;
import sun.misc.Unsafe;
import transplant.index.search.Document;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;


/** UPMEM class
 *      - The facade of the UPMEM library
 *      - The UPMEM class use singleton pattern, you need use getInstance() to get an instance.
 *      - It should call the initialization() method before get and instance
 *      -
 * **/

public class UPMEM {
    /* Configurations Fields */
    public static final int TOTAL_DPU_COUNT = 1024; // Total DPUs
    public static final int TOTAL_HARDWARE_THREADS_COUNT = 24; // hardware thread used in each DPU
    public static int dpuInUse = 1024; // DPUs in using
    public static int perDPUThreadsInUse = TOTAL_HARDWARE_THREADS_COUNT;

    /* Facade Class for PIM management */
    private static PIMManager pimManager;

    // specifiedTasklet[i]: whether require i-th DPU use a specific tasklet decidedTasklet[i] to execute tasks.
    static boolean[] specifiedTasklet = new boolean[UPMEM.dpuInUse];
    // decidedTasklet[i]: when specifiedTasklet[i] = true, this saves tasklet id that the tasklet identified by it would be used to execute tasks.
    static int[] decidedTasklet = new int[UPMEM.dpuInUse];

    // Configuration
    private static UPMEMConfigurator configurator;

    /* Unsafe class. It will be used to create a proxy class instance without initialize it */
    static Unsafe unsafe;
    public static boolean useAllowSet;


    static {
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe) singleoneInstanceField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Singleton */
    private static volatile UPMEM instance = null;
    private static final Object locker = new Object();


    public static boolean isSpecifyTasklet(int dpu){
        return specifiedTasklet[dpu];
    }

    public static void cancelSpecifyTasklet(int dpu){
        specifiedTasklet[dpu] = false;
    }
    public static int getSpecifiedTasklet(int dpu){
        return decidedTasklet[dpu];
    }
    public static int getSpecifiedTaskletAndCancel(int dpu){
        int t =  decidedTasklet[dpu];
        cancelSpecifyTasklet(dpu);
        return t;
    }
    public static void inTasklet(int dpu, int tasklet){
        specifiedTasklet[dpu] = true;
        decidedTasklet[dpu] = tasklet;
    }

    public static UPMEMConfigurator getConfigurator() {
        return configurator;
    }

    static Logger upmemLogger = Logger.getLogger("framework.pim:upmem");
    {
        upmemLogger.setEnable(false);
    }


    /** Batch Dispatching **/
    public static boolean batchDispatchingRecording = false;
    public static BatchDispatcher batchDispatcher;
    public static void beginRecordBatchDispatching(BatchDispatcher batchDispatcher) {
        UPMEM.batchDispatcher = batchDispatcher;
        batchDispatchingRecording = true;
    }
    public static void endRecordBatchDispatching() {
        batchDispatchingRecording = false;
    }


    public static void setPackageSearchPath(String packageSearchPath) {
        UPMEM.packageSearchPath = packageSearchPath;
    }


    public DPUManager getDPUManager(int dpuID){
        return pimManager.getDPUManager(dpuID);
    }


    /* create a proxy object of a given class, by using a framework.pim object handler (contains a dpuID and an object instance address in DPU MRAM) */
    public static IDPUProxyObject generateProxyObject(Class proxyClass, int dpuID, int address) throws NoSuchFieldException, InstantiationException {
        IDPUProxyObject proxyObject;
        proxyObject = (IDPUProxyObject) unsafe.allocateInstance(proxyClass);
        long dpuIDOffset = unsafe.objectFieldOffset(proxyClass.getField("dpuID"));
        unsafe.getAndSetObject(proxyObject, dpuIDOffset, dpuID);
        long addressOffset = unsafe.objectFieldOffset(proxyClass.getField("address"));
        unsafe.getAndSetObject(proxyObject, addressOffset, address);

        return proxyObject;
    }


    public static String packageSearchPath = "";


    /* Create a proxy object of a given class, by create a new object at the DPU side.
    *    - parameter 1: the dpu id that indicate in which dpu should the new object be created.
    *    - parameter 2: the class for which create a proxy object of it
    *    - parameter 3~: the arguments for initialization method call
    * */
    public IDPUProxyObject createObject(int dpuID, Class objectClass){

        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
                handler = getDPUManager(dpuID).createObject(objectClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "Proxy <----");

        try {
            Class pClass = Class.forName( packageSearchPath + objectClass.getSimpleName() + "Proxy");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }
    public IDPUProxyObject createObject(int dpuID, Class objectClass, Object arg0){

        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objectClass, arg0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "Proxy <----");

        try {
            Class pClass = Class.forName( packageSearchPath + objectClass.getSimpleName() + "Proxy");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }
    public static HashSet<String> allowSet = new HashSet<>();

    static {
        allowSet.add("java.lang.Object");
        allowSet.add("java.util.HashTable");
        allowSet.add("application.transplant.index.search.IndexTable");
        allowSet.add("application.transplant.index.search.Document");
        allowSet.add("java.util.ArrayList");
        allowSet.add("application.transplant.index.search.Searcher");
        allowSet.add("application.transplant.index.search.pojo.SearchResult");
    }



    public IDPUProxyObject createObject(int dpuID, Class objectClass, Object arg0, Object arg1){

        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objectClass, arg0, arg1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "Proxy <----");

        try {
            Class pClass = Class.forName( packageSearchPath + objectClass.getSimpleName() + "Proxy");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }
    public IDPUProxyObject createObject(int dpuID, Class objectClass, Object arg0, Object arg1, Object arg2){

        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objectClass, arg0, arg1, arg2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "Proxy <----");

        try {
            Class pClass = Class.forName( packageSearchPath + objectClass.getSimpleName() + "Proxy");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }


    public IDPUProxyObject createObject(int dpuID, Class objectClass, Object arg0, Object arg1, Object arg2, Object arg3){

        IDPUProxyObject proxyObject;
        DPUObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objectClass, arg0, arg1, arg2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        upmemLogger.logln("----> create proxy object of Class = " + objectClass + "Proxy <----");

        try {
            Class pClass = Class.forName( packageSearchPath + objectClass.getSimpleName() + "Proxy");
            proxyObject = generateProxyObject(pClass, handler.dpuID, handler.address);
        } catch (ClassNotFoundException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;
    }


    public static UPMEM initialize(UPMEMConfigurator configurator){
        if(instance == null){
            synchronized (locker){
                if(instance == null) {
                    instance = new UPMEM();
                    instance.configurator = configurator;
                    UPMEM.dpuInUse = configurator.getDpuInUseCount();
                    UPMEM.perDPUThreadsInUse = configurator.getThreadPerDPU();
                    UPMEM.packageSearchPath = configurator.getPackageSearchPath();
                    UPMEM.useAllowSet = configurator.isUseAllowSet();
                    allowSet.clear();
                    allowSet = configurator.getAllowSet();
                }

                if(!configurator.isUseSimulator()){
                    pimManager = new PIMManagerUPMEM().init(dpuInUse);
                }
                else{
                    pimManager = new PIMManagerSimulator().init(dpuInUse);
                }
            }
        }
        return instance;
    }


    public static UPMEM getInstance(){
        if(instance == null) throw new RuntimeException("UPMEM class has not been initialized.");
        return instance;
    }

    private UPMEM(){}

    /* Facade methods */
    public int getDPUHeapMemoryRemain(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainHeapMemory();
    }
    public int getDPUMetaSpaceMemoryRemain(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainMetaMemory();
    }

    public <T> DPUInt32ArrayHandler createArray(int dpuID, int len) {
        // must 1 + 4 * a bit
        int addr = UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_HEAPSPACE, len * 4 + 4);
        byte[] lenBytes = new byte[4];
        BytesUtils.writeU4LittleEndian(lenBytes, len ,0);
        UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE, lenBytes, addr);
        return new DPUInt32ArrayHandler(dpuID, addr, len);
    }

    public Object createObjectSpecific(int dpuID, String descriptor, Object... params) {
        return null;
    }
}
