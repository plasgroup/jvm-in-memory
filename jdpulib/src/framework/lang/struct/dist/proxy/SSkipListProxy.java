package framework.lang.struct.dist.proxy;

import application.transplant.index.search.ArrayListProxy;
import framework.lang.struct.IDPUProxyObject;
import framework.lang.struct.dist.SSkipList;
import framework.lang.struct.dist.SkipNode;
import framework.pim.UPMEM;
import framework.pim.dpu.RPCHelper;

import java.util.ArrayList;
import java.util.List;

public class SSkipListProxy<K, V> extends SSkipList implements IDPUProxyObject {
    public Integer address;
    public Integer dpuID;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public boolean put(Comparable key, Object value) {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "put:(Ljava/lang/Comparable;Ljava/lang/Object;)Z", key, value
        );
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public Object search(Comparable key) {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "search:(Ljava/lang/Comparable;)Ljava/lang/Object;", key
        );
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public void delete(Comparable key) {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "delete:(Ljava/lang/Comparable;)V", key
        );
    }

    @Override
    public List<SkipNode> rangeSearch(Comparable beginKey, Comparable endKey) {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "rangeSearch:(Ljava/lang/Comparable;Ljava/lang/Comparable;)Ljava/util/Collection/List;",
                beginKey, endKey
        );
        return (ArrayListProxy)RPCHelper.getAReturnValue(dpuID, ArrayListProxy.class);
    }

    @Override
    public boolean contains(Object element) {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "contains:(Ljava/lang/Object)Z", element
        );
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public int size() {
        RPCHelper.invokeMethod(dpuID, address,
                "framework/lang/struct/dist/SSkipList",
                "size:()I"
        );
        return RPCHelper.getIReturnValue(dpuID);
    }
}
