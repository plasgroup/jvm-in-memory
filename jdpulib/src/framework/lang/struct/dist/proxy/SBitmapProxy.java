package framework.lang.struct.dist.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.lang.struct.dist.SBitmap;
import framework.pim.dpu.RPCHelper;

public class SBitmapProxy extends SBitmap implements IDPUProxyObject {
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
    public int getiThBit(int i) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SBitmap", "getiThBit:(I)I", i);
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public void setiThBit(int i) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SBitmap", "setiThBit:(I)V", i);
    }

    @Override
    public void cleariThBit(int i) {
        RPCHelper.invokeMethod(dpuID, address, "framework/lang/struct/dist/SBitmap", "cleariThBit:(I)V", i);
    }
}
