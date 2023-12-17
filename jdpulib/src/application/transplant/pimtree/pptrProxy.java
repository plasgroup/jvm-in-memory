package application.transplant.pimtree;

import framework.lang.struct.IDPUProxyObject;

public class pptrProxy extends pptr implements IDPUProxyObject {
    public Integer address = -1;
    public Integer dpuID = -1;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }
}
