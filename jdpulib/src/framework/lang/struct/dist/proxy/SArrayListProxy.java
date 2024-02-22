package framework.lang.struct.dist.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.lang.struct.dist.SArrayList;
import framework.pim.UPMEM;
import framework.pim.dpu.RPCHelper;

public class SArrayListProxy extends SArrayList implements IDPUProxyObject {
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
    public Object get(int loc) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SList", "get:(I)Ljava/lang/Object;", loc);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public void set(int loc, Object value) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SList", "set:(ILjava/lang/Object;)Ljava/lang/Object;", loc, value);
    }

    @Override
    public void append(Object value) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SList", "append:(Ljava/lang/Object;)V", value);
    }

    @Override
    public int size() {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SList", "size:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }
}
