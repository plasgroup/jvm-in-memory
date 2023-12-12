package application.transplant.index.search;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;
import application.transplant.index.search.IndexTable;

public class IndexTableProxy extends IndexTable implements IDPUProxyObject {
    Integer address;
    Integer dpuID;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getSize() {
        RPCHelper.invokeMethod(dpuID, address, "IndexTable", "getSize():I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public void insert(int wordID, int documentId, int location){
        RPCHelper.invokeMethod(dpuID, address, "IndexTable", "insert(III):V");
    }

}
