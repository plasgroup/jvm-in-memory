package transplant.index.search.proxy;

import pim.IDPUProxyObject;
import transplant.index.search.Document;

public class DocumentProxy extends Document implements IDPUProxyObject {
    int dpuID;
    int address;

    public DocumentProxy(int id) {
        super(id);
    }

    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }
}
