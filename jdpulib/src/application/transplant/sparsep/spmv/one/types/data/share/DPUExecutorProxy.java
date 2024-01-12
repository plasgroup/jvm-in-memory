package application.transplant.sparsep.spmv.one.types.data.share;

import application.transplant.sparsep.spmv.one.bcooblock.Main;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;
import sparsep.spmv.one.types.data.share.Bind;

import java.util.List;

public class DPUExecutorProxy extends DPUExecutor implements IDPUProxyObject {
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
    public List<Integer> calculate(List<Integer> bval, List<Integer> x, List<Bind> binds, Main.Arguments params) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/sparsep/spmv/one/types/data/share/DPUExecutor",
                "calculate:(Ljava/util/List;Ljava/util/List;Ljava/util/List;application/transplant/sparsep/spmv/one/bcooblock/Main/Arguments;)"
        );
        return (List<Integer>) RPCHelper.getAReturnValue(dpuID);
    }
}
