package framework.pim.dpu;

import framework.lang.struct.DummyProxy;
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


    /** get int result value **/
    public static int getIReturnValue(int dpuID){
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);
        return returnVal;
    }

    /** get boolean result value **/

    public static boolean getBooleanReturnValue(int dpuID){
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal() ;
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);
        return returnVal == 0 ? false : true;
    }

    /** get reference result value **/

    public static IDPUProxyObject getAReturnValue(int dpuID, Class proxyClass){
        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            // pimProxy.logf("framework.pim:proxy","return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;
            return UPMEM.generateProxyObject(proxyClass, dpuID, returnVal);
        } catch (NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }



    /** invoke method **/

    public static void invokeMethod(int dpuID, int address, String className, String methodDescriptor, Object... params){
        DPUCacheManager cm = upmem.getDPUManager(dpuID).classCacheManager;
        int methodMRAMAddr = cm.getMethodCacheItem(className, methodDescriptor).mramAddr;
        int classMRAMAddr = cm.getClassStrutCacheLine(className).marmAddr;
        // pimProxy.logf("framework.pim:proxy: class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMRAMAddr, methodMRAMAddr, objectHandler.address);
        upmem.getDPUManager(dpuID).callNonstaticMethod(classMRAMAddr, methodMRAMAddr, address, params);
    }

    public static Object getAReturnValue(int dpuID) {
        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            // pimProxy.logf("framework.pim:proxy","return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;
            return UPMEM.generateProxyObject(DummyProxy.class, dpuID, returnVal);
        } catch (NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Object[] ArrayHandlerFromAddress(IDPUProxyObject aReturnValue) {
//    }
}

