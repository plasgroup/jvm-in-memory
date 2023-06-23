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

    public DPUTreeNodeProxyAutoGen(int k, int v) {
        super(k, v);
    }

    public static DPUTreeNodeProxyAutoGen fromHandler(PIMObjectHandler handler) {
        return null;
    }

    @Override
    public TreeNode getLeft() {
        //return super.getLeft();

        System.out.println("--------- Invoke proxy getLeft() ------------");
        throw new RuntimeException("call to dpu");
    }

    @Override
    public TreeNode getRight() {
        System.out.println("--------- Invoke proxy getRight() handler = " + objectHandler +
                " ------------");
        System.out.println(" - DPUID = " + objectHandler.dpuID);
        DPUCacheManager cm = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;
        System.out.println(cm);

        int methodMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                                .classCacheManager.getMethodCacheItem("pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;").mramAddr;
        int classMramAddr = upmem.getDPUManager(objectHandler.dpuID)
                .classCacheManager.getClassStrutCacheLine("pim/algorithm/TreeNode").marmAddr;
        System.out.printf("class mram addr = 0x%x, method mram addr = 0x%x, instance addr = 0x%x\n", classMramAddr, methodMramAddr, objectHandler.address);
        try {
            upmem.getDPUManager(objectHandler.dpuID).setMethodPt(methodMramAddr);
            upmem.getDPUManager(objectHandler.dpuID).setClassPt(classMramAddr);

        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // parameters
        try {
            upmem.getDPUManager(objectHandler.dpuID).garbageCollector.pushParameters(new int[]{objectHandler.address});
            DPUCacheManager classCacheManager = upmem.getDPUManager(objectHandler.dpuID).classCacheManager;

            System.out.printf("push instance addr =  %x\n", objectHandler.address);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }


        try {
            upmem.getDPUManager(objectHandler.dpuID).dpu.exec(System.out);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("call to dpu");
    }

    @Override
    public int getVal() {
        // send this method's order in method table
        // --> set function pointer
        // for each DPU, host save
        System.out.println("--------- Invoke proxy getVal(), handler = " + objectHandler +
                " " + " DPU id = " + objectHandler.dpuID + " heap offset = " + objectHandler.address + " ------------");

        try {
            upmem.getDPUManager(objectHandler.dpuID).dpu.exec(System.out);
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("call to dpu");
    }

    @Override
    public TreeNode createNode(int k, int v){
        return null;
    }



    // Host getKey() : getKey() -> DPU dispatch

    // DPU program: return
    // dispatch

    // DPU class struct.  dpu class include methodtable, bytecodes
    public void setMethodPtr(int methodTableIndex) throws DpuException {
        //set pointer
        DPUClassFileCacheItem cacheLine =
                upmem.getDPUManager(objectHandler.dpuID).classCacheManager.getClassStrutCacheLine(TreeNode.class.getName());

        DPUClassFileManager.printDPUClassStrut(cacheLine.dpuClassStrut);
        int methodOffset = cacheLine.dpuClassStrut.methodOffset[1];
        int addr = methodOffset + cacheLine.marmAddr;

        // 32 bit int -> byte[] -> DPU
        if(method_ptr == null) method_ptr = new byte[4];
        method_ptr[0] = (byte) ((addr >> 24) & 0xFF);
        method_ptr[1] = (byte) ((addr >> 16) & 0xFF);
        method_ptr[2] = (byte) ((addr >> 8) & 0xFF);
        method_ptr[3] = (byte) (addr & 0xFF);
        upmem.getDPUManager(objectHandler.dpuID).dpu.copy("method_ptr", method_ptr);
    }
    @Override
    public int getKey() {
        System.out.println("--------- Invoke proxy getKey() ------------");

        try {
            setMethodPtr(1);

            // parameter (count == 0)
            //// write to parameter buffer
            //UPMEM.getDPUManager(objectHandler.dpuID).dpu.copy("parameter_buffer", ...);

            upmem.getDPUManager(objectHandler.dpuID).dpu.exec(System.out);

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
