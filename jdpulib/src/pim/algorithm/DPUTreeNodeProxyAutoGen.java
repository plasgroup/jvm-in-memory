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
    byte[] method_ptr = new byte[4];
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

    public static DPUTreeNodeProxyAutoGen fromHandler(PIMObjectHandler handler) {
        return null;
    }



    @Override
    public TreeNode getLeft() {

        if(true)
            throw new RuntimeException("wait for test");

        System.out.println("--------- Invoke proxy getLeft() ------------");
        throw new RuntimeException("call to dpu");
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
            return DPUTreeNodeProxyAutoGen.fromHandler(new PIMObjectHandler(getDpuID(), returnVal));
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVal() {
        if(true)
            throw new RuntimeException("wait for test");
        // send this method's order in method table
        // --> set function pointer
        // for each DPU, host save
        System.out.println("--------- Invoke proxy getVal(), handler = " + objectHandler +
                " " + " DPU id = " + objectHandler.dpuID + " heap offset = " + objectHandler.address + " ------------");

        try {
            upmem.getDPUManager(objectHandler.dpuID).dpuExec(System.out);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("call to dpu");
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
            return DPUTreeNodeProxyAutoGen.fromHandler(new PIMObjectHandler(getDpuID(), returnVal));
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

    }



    // Host getKey() : getKey() -> DPU dispatch

    // DPU program: return
    // dispatch


    @Override
    public int getKey() {

        if(true)
            throw new RuntimeException("wait for test");
        System.out.println("--------- Invoke proxy getKey() ------------");

        try {

            // parameter (count == 0)
            //// write to parameter buffer
            //UPMEM.getDPUManager(objectHandler.dpuID).dpu.copy("parameter_buffer", ...);

            upmem.getDPUManager(objectHandler.dpuID).dpuExec(System.out);

            // return
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }

    //....
}


// getkey() -> firstly, make it can be dispatched
// getval()
// setkey(k);
// setval("jvjvk") // string #1
// setleft(node);  //
// string.  intern()
// array

// Node[] arr1 = new Node[10];
// Node[] arr2 = UPMEM.arraynew(....);

// init()

/*
*  return "abc"; // return #1;
* */

// method call
// this.foo();
//  insert method.
    // new
