package framework.pim.dpu;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import application.bst.DPUTreeNodeProxyAutoGen;
import framework.pim.dpu.cache.DPUCacheManager;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;


/** contains result retrieving helper functions and method invocation function **/
public class RPCHelper {
    static Logger pimProxy = PIMLoggers.pimProxy;
    static UPMEM upmem = UPMEM.getInstance();
    public static int getIReturnValue(int dpuID){
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);
        return returnVal;
    }
    public static boolean getBooleanReturnValue(int dpuID){
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);
        return returnVal == 0 ? false : true;
    }

    public static IDPUProxyObject getAReturnValue(int dpuID){
        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            // pimProxy.logf("framework.pim:proxy","return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;
            return UPMEM.generateProxyObject(DPUTreeNodeProxyAutoGen.class, dpuID, returnVal);
        } catch (NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeMethod(int dpuID, int address, String className, String methodDescriptor, Object... params){
        DPUCacheManager cm = upmem.getDPUManager(dpuID).classCacheManager;
        int methodMRAMAddr = cm.getMethodCacheItem(className, methodDescriptor).mramAddr;
        int classMRAMAddr = cm.getClassStrutCacheLine(className).marmAddr;
        // pimProxy.logf("framework.pim:proxy: class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMRAMAddr, methodMRAMAddr, objectHandler.address);
        upmem.getDPUManager(dpuID).callNonstaticMethod(classMRAMAddr, methodMRAMAddr, address, params);
    }
}

