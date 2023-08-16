package pim.algorithm;

import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUObjectHandler;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import static pim.dpu.ProxyHelper.*;

public class DPUTreeNodeProxyAutoGen extends DPUTreeNode implements IDPUProxyObject {
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
    int dpuID;
    int address;

    static Logger pimProxy = PIMLoggers.pimProxy;

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getAddr() {
        return address;
    }

    public DPUTreeNodeProxyAutoGen(int k, int v) {
        super(k, v);
    }
    public DPUTreeNodeProxyAutoGen(int k, int v, int dpuID, int mramAddress) {
        super(k, v);
        this.dpuID = dpuID;
        this.address = mramAddress;
    }
    @Override
    public TreeNode getLeft() {
        getLeftDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(dpuID);
    }

    @Override
    public TreeNode getRight() {
        getRightDispatchCount++;
        invokeMethod(dpuID, address, "pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(dpuID);
    }

    @Override
    public void setRight(TreeNode right) {
        setRightDispatchCount++;
        invokeMethod(dpuID, address, "pim/algorithm/TreeNode", "setRight:(Lpim/algorithm/TreeNode;)V", right);
    }

    @Override
    public void setLeft(TreeNode left) {
        setLeftDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "setLeft:(Lpim/algorithm/TreeNode;)V", left);
    }
    @Override
    public void setKey(int key) {
        setKeyDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "setKey:(I)V", key);
    }

    @Override
    public void setVal(int key) {
        setValDispatchCount++;
        invokeMethod(dpuID, address, "pim/algorithm/TreeNode", "setVal:(I)V", key);
    }

    @Override
    public int getVal() {
        getValDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "getVal:()I");
        return getIReturnValue(dpuID);
    }

    @Override
    public TreeNode createNode(int k, int v){
        createNodeDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/DPUTreeNode", "createNode:(II)Lpim/algorithm/TreeNode;", k, v);
        return (TreeNode) getAReturnValue(dpuID);
    }

    @Override
    public int getKey() {
        getKeyDispatchCount++;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "getKey:()I");
        int retVal = getIReturnValue(dpuID);
        return retVal;
    }

    @Override
    public void insert(int k, int v) {
        insertDispatchCount++;
        DPUCacheManager classCacheManager1 = UPMEM.getInstance().getDPUManager(dpuID).classCacheManager;
        invokeMethod(dpuID, address,"pim/algorithm/TreeNode", "insert:(II)V", k, v);
    }

    @Override
    public int search(int k) {
        searchDispatchCount++;
        invokeMethod(dpuID, address, "pim/algorithm/TreeNode", "search:(I)I", k);
        int retVal = getIReturnValue(dpuID);
        return retVal;
    }
}

