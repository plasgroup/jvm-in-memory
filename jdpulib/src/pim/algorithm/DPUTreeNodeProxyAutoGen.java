package pim.algorithm;

import com.upmem.dpu.DpuException;
import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUClassFileCacheItem;
import pim.dpu.DPUClassFileManager;
import pim.dpu.PIMObjectHandler;


public class DPUTreeNodeProxyAutoGen extends DPUTreeNode implements IDPUProxyObject {
    public PIMObjectHandler objectHandler = null;
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




    @Override
    public TreeNode getLeft() {

        System.out.println("--------- Invoke proxy getLeft() handler = " + objectHandler + " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getMethodCacheItem("pim/algorithm/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/TreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);

        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMramAddr, methodMramAddr, objectHandler.address, new Object[]{});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // get return val
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("right treenode pointer = 0x%d\n", returnVal);
            return (TreeNode) UPMEM.createProxyObjectFromHandler(DPUTreeNodeProxyAutoGen.class, new PIMObjectHandler(getDpuID(), returnVal));
        } catch (DpuException | NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TreeNode getRight() {

        System.out.println("--------- Invoke proxy getRight() handler = " + objectHandler + " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                                .classCacheManager.getMethodCacheItem("pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/TreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);

        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMramAddr, methodMramAddr, objectHandler.address, new Object[]{});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // get return val
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("right treenode pointer = 0x%d\n", returnVal);
            return (TreeNode) UPMEM.createProxyObjectFromHandler(DPUTreeNodeProxyAutoGen.class, new PIMObjectHandler(getDpuID(), returnVal));
        } catch (DpuException | NoSuchFieldException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVal() {
        System.out.println("--------- Invoke proxy getVal() handler = " + objectHandler + " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getMethodCacheItem("pim/algorithm/TreeNode", "getVal:()I").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/TreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);

        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMramAddr, methodMramAddr, objectHandler.address, new Object[]{});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // get return val
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("val = 0x%d\n", returnVal);
            return returnVal;
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TreeNode createNode(int k, int v){

        System.out.println("--------- Invoke proxy createNode() handler = " + objectHandler + " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getMethodCacheItem("pim/algorithm/DPUTreeNode", "createNode:(II)Lpim/algorithm/TreeNode;").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/DPUTreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);

        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMramAddr, methodMramAddr, objectHandler.address, new Object[]{k, v});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // get return val
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("right treenode pointer = 0x%d\n", returnVal);
            return (TreeNode) UPMEM.createProxyObjectFromHandler(DPUTreeNodeProxyAutoGen.class, new PIMObjectHandler(getDpuID(), returnVal));
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public int getKey() {
        System.out.println("--------- Invoke proxy getVal() handler = " + objectHandler + " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getMethodCacheItem("pim/algorithm/TreeNode", "getKey:()I").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/TreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);

        try {
            upmem.getDPUManager(getDpuID()).callNonstaticMethod(classMramAddr, methodMramAddr, objectHandler.address, new Object[]{});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // get return val
        try {
            int returnVal = upmem.getDPUManager(getDpuID()).garbageCollector.getReturnVal();
            System.out.printf("key = 0x%d\n", returnVal);
            return returnVal;
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }
}

