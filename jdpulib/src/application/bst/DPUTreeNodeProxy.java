package application.bst;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

import static framework.pim.dpu.RPCHelper.*;

public class DPUTreeNodeProxy extends DPUTreeNode implements IDPUProxyObject {
    public static int getLeftDispatchCount = 0;
    public static int getRightDispatchCount = 0;
    public static int setRightDispatchCount = 0;
    public static int setLeftDispatchCount = 0;
    public static int setKeyDispatchCount = 0;
    public static int setValDispatchCount = 0;
    public static int getValDispatchCount = 0;
    public static int searchDispatchCount = 0;
    public static int getKeyDispatchCount = 0;
    public static int insertDispatchCount = 0;
    public static int createNodeDispatchCount = 0;
    public Integer dpuID;
    public Integer address;

    static Logger pimProxy = PIMLoggers.pimProxy;

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getAddr() {
        return address;
    }

    public DPUTreeNodeProxy(int k, int v) {
        super(k, v);
    }

    public DPUTreeNodeProxy(int k, int v, int dpuID, int mramAddress) {
        super(k, v);
        this.dpuID = dpuID;
        this.address = mramAddress;
    }

    @Override
    public void setTestArray(int index, int value) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "setTestArray:(II)V", index, value);
    }

    @Override
    public int getTestArray(int index) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "getTestArray:(I)I", index);
        return getIReturnValue(dpuID);
    }

    @Override
    public TreeNode getLeft() {
        getLeftDispatchCount++;
        invokeMethod(dpuID, address, "application/bst/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(dpuID, DPUTreeNodeProxy.class);
    }

    @Override
    public TreeNode getRight() {
        getRightDispatchCount++;
        invokeMethod(dpuID, address, "application/bst/TreeNode", "getRight:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(dpuID, DPUTreeNodeProxy.class);
    }

    @Override
    public void setRight(TreeNode right) {
        setRightDispatchCount++;
        invokeMethod(dpuID, address, "application/bst/TreeNode", "setRight:(Lpim/algorithm/TreeNode;)V", right);
    }

    @Override
    public void setLeft(TreeNode left) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "setLeft:(Lpim/algorithm/TreeNode;)V", left);
    }

    @Override
    public void setKey(int key) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "setKey:(I)V", key);
    }

    @Override
    public void setVal(int key) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "setVal:(I)V", key);
    }

    @Override
    public int getVal() {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "getVal:()I");
        return getIReturnValue(dpuID);
    }

    @Override
    public TreeNode createNode(int k, int v) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "createNode:(II)Lpim/algorithm/TreeNode;", k, v);
        return (TreeNode) getAReturnValue(dpuID, DPUTreeNodeProxy.class);
    }

    @Override
    public int getKey() {

        invokeMethod(dpuID, address, "application/bst/TreeNode", "getKey:()I");
        int retVal = getIReturnValue(dpuID);
        return retVal;
    }

    @Override
    public void insert(int k, int v) {
        long s = System.nanoTime();
        DPULookupTableManager classCacheManager1 = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
        invokeMethod(dpuID, address, "application/bst/TreeNode", "insert:(II)V", k, v);
        BSTTester.prepareTimeTotal += System.nanoTime() - s;
    }

    @Override
    public int search(int k) {
        invokeMethod(dpuID, address, "application/bst/TreeNode", "search:(I)I", k);
        int retVal = getIReturnValue(dpuID);
        return retVal;
    }
}
