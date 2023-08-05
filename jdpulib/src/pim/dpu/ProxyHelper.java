package pim.dpu;

import com.upmem.dpu.DpuException;
import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.algorithm.DPUTreeNodeProxyAutoGen;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

public class ProxyHelper {
    static Logger pimProxy = PIMLoggers.pimProxy;
    static UPMEM upmem = UPMEM.getInstance();
    public static int getIReturnValue(int dpuID){
        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            // pimProxy.logf( "pim:proxy","return int = %d\n", returnVal);
            return returnVal;
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    public static IDPUProxyObject getAReturnValue(int dpuID){
        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            pimProxy.logf("pim:proxy","return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;
            return UPMEM.generateProxyObjectFromHandler(DPUTreeNodeProxyAutoGen.class, new DPUObjectHandler(dpuID, returnVal));
        } catch (DpuException | NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeMethod(int dpuID, int address, String className, String methodDescriptor, Object... params){
        // pimProxy.logf("--------- Invoke proxy %s handler = " + objectHandler + " ------------\n", methodDescriptor.split(":")[0]);
        // pimProxy.logf(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(dpuID).classCacheManager;
        int methodMRAMAddr = cm.getMethodCacheItem(className, methodDescriptor).mramAddr;
        int classMRAMAddr = cm.getClassStrutCacheLine(className).marmAddr;
        // pimProxy.logf("pim:proxy: class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMRAMAddr, methodMRAMAddr, objectHandler.address);
        try {
            upmem.getDPUManager(dpuID).callNonstaticMethod(classMRAMAddr, methodMRAMAddr, address, params);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }
}
