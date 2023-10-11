package transplant.index.search.proxy;

import pim.IDPUProxyObject;
import transplant.index.search.Document;

public class DocumentProxy extends Document implements IDPUProxyObject {
    int dpuID;
    int address;

    private DocumentProxy(int id, String context) {
        super(id, context);
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
