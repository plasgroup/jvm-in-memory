package framework.pim.dpu;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import application.bst.DPUTreeNodeProxy;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;


/** contains result retrieving helper functions and method invocation function **/
public class RPCHelper {
    static Logger pimProxy = PIMLoggers.pimProxy;
    static UPMEM upmem = UPMEM.getInstance();
    static {
        pimProxy.setEnable(false);
    }



    /** get int result value **/
    public static int getIReturnValue(int dpuID){
        if(UPMEM.isBatchDispatchingRecording()) return -1;
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);

        return returnVal;
    }

    /** get boolean result value **/

    public static boolean getBooleanReturnValue(int dpuID){
        if(UPMEM.isBatchDispatchingRecording()) return false;
        int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal() ;
        // pimProxy.logf( "framework.pim:proxy","return int = %d\n", returnVal);

        return returnVal == 0 ? false : true;
    }

    /** get reference result value **/

    public static IDPUProxyObject getAReturnValue(int dpuID, Class proxyClass){
        if(UPMEM.isBatchDispatchingRecording()) return null;

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
        DPULookupTableManager cm = upmem.getDPUManager(dpuID).classCacheManager;
        int methodMRAMAddr = cm.getMethodLookupTableItem(className, methodDescriptor).mramAddr;
        int classMRAMAddr = cm.getClassLookupTableItem(className).marmAddr;


        upmem.getDPUManager(dpuID).callNonstaticMethod(classMRAMAddr, methodMRAMAddr, address, params);
    }

    public static IDPUProxyObject getAReturnValue(int dpuID){
        if(UPMEM.isBatchDispatchingRecording()) return null;

        try {
            int returnVal = upmem.getDPUManager(dpuID).garbageCollector.getReturnVal();
            // pimProxy.logf("pim:proxy","return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;

            return UPMEM.generateProxyObject(DPUTreeNodeProxy.class, dpuID, returnVal);
        } catch (NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Object[] ArrayHandlerFromAddress(IDPUProxyObject aReturnValue) {
//    }
}

