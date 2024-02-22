package framework.lang.struct.dist.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.lang.struct.dist.SSet;
import framework.pim.dpu.RPCHelper;

public class SSetProxy extends SSet implements IDPUProxyObject {
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
    public boolean contains(Object element) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SSet",
                "contains:(java/lang/Object;)Z", element);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public void put(Object element) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SSet",
                "put:(java/lang/Object;)V", element);
    }

    @Override
    public int size() {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SSet",
                "size:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }
}
