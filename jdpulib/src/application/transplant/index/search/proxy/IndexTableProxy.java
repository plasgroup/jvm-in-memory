package application.transplant.index.search.proxy;

import framework.pim.struct.IDPUProxyObject;
import framework.pim.dpu.ProxyHelper;
import application.transplant.index.search.IndexTable;

public class IndexTableProxy extends IndexTable implements IDPUProxyObject {
    int address;
    int dpuID;
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
        ProxyHelper.invokeMethod(dpuID, address, "IndexTable", "getSize():I");
        return ProxyHelper.getIReturnValue(dpuID);
    }

    @Override
    public void insert(int wordID, int documentId, int location){
        ProxyHelper.invokeMethod(dpuID, address, "IndexTable", "insert(III):V");
    }

}
