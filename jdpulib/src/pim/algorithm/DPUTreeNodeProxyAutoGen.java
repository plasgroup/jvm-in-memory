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
        super(k, v);
    }


    @Override
    public TreeNode getLeft() {
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getLeft:()Lpim/algorithm/TreeNode;", new Object[]{});
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public TreeNode getRight() {
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "getRight:()Lpim/algorithm/TreeNode;", new Object[]{});
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public void setRight(TreeNode right) {
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "setRight:(Lpim/algorithm/TreeNode;)V", new Object[]{right});
    }

    @Override
    public void setLeft(TreeNode left) {
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "setLeft:(Lpim/algorithm/TreeNode;)V", new Object[]{left});
    }

    @Override
    public void setKey(int key) {
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "setKey:(I)V", new Object[]{key});
    }

    @Override
    public void setVal(int key) {
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "setVal:(I)V", new Object[]{key});
    }

    @Override
    public int getVal() {
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getVal:()I", new Object[]{});
        return getIReturnValue(getDpuID());
    }

    @Override
    public TreeNode createNode(int k, int v){

        invokeMethod(objectHandler,"pim/algorithm/DPUTreeNode", "createNode:(II)Lpim/algorithm/TreeNode;", new Object[]{k, v});
        return (TreeNode) getAReturnValue(getDpuID());
    }

    @Override
    public int getKey() {
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "getKey:()I", new Object[]{});
        int retVal = getIReturnValue(getDpuID());
        return retVal;
    }

    @Override
    public void insert(int k, int v) {
        pimProxy.log( "insert dispatch");
        DPUCacheManager classCacheManager1 = UPMEM.getInstance().getDPUManager(getDpuID()).classCacheManager;
        invokeMethod(objectHandler,"pim/algorithm/TreeNode", "insert:(II)V", new Object[]{k, v});
    }


    @Override
    public int search(int k) {
        invokeMethod(objectHandler, "pim/algorithm/TreeNode", "search:(I)I", new Object[]{k});
        int retVal = getIReturnValue(getDpuID());
        return retVal;
    }
}

