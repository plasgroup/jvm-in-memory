package application.transplant.index.search;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;
import application.transplant.index.search.IndexTable;

public class IndexTableProxy extends IndexTable implements IDPUProxyObject {
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
    public int getSize() {
        System.out.println("dispatch get size");
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/IndexTable", "getSize:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public void insert(int wordID, int documentId, int location){
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/index/search/IndexTable", "insert:(III)V", wordID, documentId, location);
    }

}
