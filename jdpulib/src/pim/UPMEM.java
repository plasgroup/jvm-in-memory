package pim;

import com.upmem.dpu.DpuException;
import pim.dpu.*;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;


public class UPMEM {
    public static final int TOTAL_DPU_COUNT = 64;
    public static final int TOTAL_HARDWARE_THREADS_COUNT = 24;
    public static int dpuInUse = TOTAL_DPU_COUNT;
    public static int perDPUThreadsInUse = TOTAL_HARDWARE_THREADS_COUNT;
    private static PIMManager pimManager;
    private static volatile UPMEM instance = null;
    private static Object locker = new Object();
    public DPUManager getDPUManager(int dpuID){
        return pimManager.getDPUManager(dpuID);
    }
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


    public static IDPUProxyObject createProxyObjectFromHandler(Class proxyClass, PIMObjectHandler objectHandler) throws NoSuchFieldException, InstantiationException {
        IDPUProxyObject proxyObject;
        proxyObject = (IDPUProxyObject) unsafe.allocateInstance(proxyClass);
        long handlerOffset = unsafe.objectFieldOffset(proxyClass.getField("objectHandler"));
        unsafe.getAndSetObject(proxyObject, handlerOffset, objectHandler);
        return proxyObject;
    }
    public IDPUProxyObject createObject(int dpuID, Class objClass, Object... params){
        IDPUProxyObject proxyObject;
        PIMObjectHandler handler;
        try {
            handler = getDPUManager(dpuID).createObject(objClass, params);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("----> create proxy object of Type = " + objClass + "ProxyAutoGen <----");
        try {
            Class pClass = Class.forName( objClass.getName() + "ProxyAutoGen");
            proxyObject = (IDPUProxyObject) unsafe.allocateInstance(pClass);
            long handlerOffset = unsafe.objectFieldOffset(pClass.getField("objectHandler"));
            unsafe.getAndSetObject(proxyObject, handlerOffset, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }  catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return proxyObject;

    }
    public static UPMEM init(){
        if(instance == null){
            synchronized (locker){
                if(instance == null) instance = new UPMEM();
                try {
                    pimManager = PIMManager.init(dpuInUse);
                } catch (DpuException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return instance;
    }
    public static UPMEM init(UPMEMConfigurator configurator){
        perDPUThreadsInUse = configurator.getThreadPerDPU();
        dpuInUse = configurator.getDpuInUseCount();
        return init();
    }


    public static UPMEM getInstance(){
        if(instance == null) throw new RuntimeException("UPMEM class has not been initialized.");
        return instance;
    }

    private UPMEM(){}
    public int getDPURemainHeapMemory(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainHeapMemory();
    }
    public int getDPURemainMetaSpaceMemory(int dpuID) {
        return getDPUManager(dpuID).garbageCollector.getRemainMetaMemory();
    }

}
