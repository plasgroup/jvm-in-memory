package pim.algorithm;

import com.upmem.dpu.DpuException;
import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUObjectHandler;

public class DPUTreeNodeProxyAutoGen extends DPUTreeNode implements IDPUProxyObject {
    public DPUObjectHandler objectHandler = null;
    static UPMEM upmem = UPMEM.getInstance();

    @Override
    public int getDpuID() {
        return objectHandler.dpuID;
    }

    @Override
    public int getAddr() {
        return objectHandler.address;
    }

    public DPUTreeNodeProxyAutoGen(int k, int v) {
        super(k, v);
    }

    public void invokeMethod(String className, String methodDescriptor, Object[] params){
        System.out.printf("--------- Invoke proxy %s handler = " + objectHandler + " ------------\n", methodDescriptor.split(":")[0]);
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        int methodMRAMAddr = cm.getMethodCacheItem(className, methodDescriptor).mramAddr;
        int classMRAMAddr = cm.getClassStrutCacheLine(className).marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMRAMAddr, methodMRAMAddr, objectHandler.address);
        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMRAMAddr, methodMRAMAddr, objectHandler.address, params);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    public IDPUProxyObject getAReturnVal(){
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("return pointer = 0x%x\n", returnVal);
            if(returnVal == 0) return null;
            return UPMEM.generateProxyObjectFromHandler(DPUTreeNodeProxyAutoGen.class, new DPUObjectHandler(getDpuID(), returnVal));
        } catch (DpuException | NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public int getIReturnVal(){
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("return int = %d\n", returnVal);
            return returnVal;
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TreeNode getLeft() {
        invokeMethod("pim/algorithm/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;", new Object[]{});
        return (TreeNode) getAReturnVal();
    }

    @Override
    public TreeNode getRight() {
        invokeMethod("pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;", new Object[]{});
        return (TreeNode) getAReturnVal();
    }

    @Override
    public int getVal() {
        if(true) throw new RuntimeException();
        invokeMethod("pim/algorithm/TreeNode", "getVal:()I", new Object[]{});
        return getIReturnVal();
    }

    @Override
    public TreeNode createNode(int k, int v){
        invokeMethod("pim/algorithm/DPUTreeNode", "createNode:(II)Lpim/algorithm/TreeNode;", new Object[]{k, v});
        return (TreeNode) getAReturnVal();
    }

    @Override
    public int getKey() {
        invokeMethod("pim/algorithm/TreeNode", "getKey:()I", new Object[]{});
        int retVal = getIReturnVal();
        return retVal;
    }
}

