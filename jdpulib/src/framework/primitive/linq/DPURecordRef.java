package framework.primitive.linq;

import framework.lang.struct.IDPUProxyObject;

public class DPURecordRef implements IDPUProxyObject {
    int address;
    int dpuID;

    public DPURecordRef(int dpuID, int result) {

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
