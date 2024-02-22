package framework.lang.struct.dist.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;

import java.util.Dictionary;
import java.util.Enumeration;

public class DictionaryProxy extends Dictionary implements  IDPUProxyObject {
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
    public int size() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/Collection/Dictionary", "size:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public boolean isEmpty() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/Collection/Dictionary", "isEmpty:()Z");
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public Enumeration keys() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public Enumeration elements() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public Object get(Object key) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/Collection/Dictionary", "get:(Ljava/lang/Object;)Ljava/lang/Object;", key);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public Object put(Object key, Object value) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/Collection/Dictionary", "put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", key, value);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public Object remove(Object key) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/Collection/Dictionary", "remove:(Ljava/lang/Object;)Ljava/lang/Object;", key);
        return RPCHelper.getAReturnValue(dpuID);
    }
}
