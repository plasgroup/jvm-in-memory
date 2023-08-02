package pim.algorithm;

import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUObjectHandler;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import static pim.dpu.ProxyHelper.*;

public class DPUTreeNodeProxyAutoGen extends DPUTreeNode implements IDPUProxyObject {
    public DPUObjectHandler objectHandler = null;
    static Logger pimProxy = PIMLoggers.pimProxy;

    @Override
    public int getDpuID() {
        return objectHandler.dpuID;
    }

    @Override
    public int getAddr() {
        return objectHandler.address;
    }

    public DPUTreeNodeProxyAutoGen(int k, int v) {
        super(k, v);objectHandler =new DPUObjectHandler(0,0);
    }

    public static int getLeftDispatchCount = 0;
    public static int getRightDispatchCount = 0;
    public static int setRightDispatchCount = 0;
    public static int setLeftDispatchCount = 0;
    public static int setKeyDispatchCount = 0;
    public static int setValDispatchCount = 0;
    public static int getValDispatchCount = 0;
    public static int searchDispatchCount = 0;
    public static int getKeyDispatchCount = 0;
    @Override
    public TreeNode getLeft() {
        getLeftDispatchCount++;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public TreeNode getRight() {
        getRightDispatchCount++;
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;");
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public void setRight(TreeNode right) {
        setRightDispatchCount++;
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "setRight:(Lpim/algorithm/TreeNode;)V", right);
    }

    @Override
    public void setLeft(TreeNode left) {
        setLeftDispatchCount++;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "setLeft:(Lpim/algorithm/TreeNode;)V", left);
    }

    @Override
    public void setKey(int key) {
        setKeyDispatchCount++;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "setKey:(I)V", key);
    }

    @Override
    public void setVal(int key) {
        setValDispatchCount++;
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "setVal:(I)V", key);
    }

    @Override
    public int getVal() {
        getValDispatchCount++;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getVal:()I");
        return getIReturnValue(getDpuID());
    }

    @Override
    public TreeNode createNode(int k, int v){
        invokeMethod(objectHandler,"pim/algorithm/DPUTreeNode", "createNode:(II)Lpim/algorithm/TreeNode;", k, v);
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public int getKey() {
        getKeyDispatchCount++;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getKey:()I");
        int retVal = getIReturnValue(getDpuID());
        return retVal;
    }

    @Override
    public void insert(int k, int v) {
        pimProxy.log( "insert dispatch");
        DPUCacheManager classCacheManager1 = UPMEM.getInstance().getDPUManager(getDpuID()).classCacheManager;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "insert:(II)V", k, v);
    }

    @Override
    public int search(int k) {
        searchDispatchCount++;
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "search:(I)I", k);
        int retVal = getIReturnValue(getDpuID());
        return retVal;
    }
}

