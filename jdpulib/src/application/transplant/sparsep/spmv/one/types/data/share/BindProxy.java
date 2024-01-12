package application.transplant.sparsep.spmv.one.types.data.share;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;

public class BindProxy extends sparsep.spmv.one.types.data.share.Bind implements IDPUProxyObject {
    public Integer dpuID = -1;
    public Integer address = -1;
    public BindProxy(int rowind, int colind) {
        super(rowind, colind);
    }

    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getColind() {
        RPCHelper.invokeMethod(dpuID, address, "sparsep/spmv/one/types/data/share/Bind", "getColind:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int getRowind() {
        RPCHelper.invokeMethod(dpuID, address, "sparsep/spmv/one/types/data/share/Bind", "getRowind:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }
}
