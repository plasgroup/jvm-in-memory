package transplant.index.search.proxy;

import pim.IDPUProxyObject;
import pim.UPMEM;
import pim.dpu.ProxyHelper;
import transplant.index.search.IndexTable;

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
