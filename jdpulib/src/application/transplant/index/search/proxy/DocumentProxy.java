package application.transplant.index.search.proxy;

import framework.pim.struct.IDPUProxyObject;
import application.transplant.index.search.Document;

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
