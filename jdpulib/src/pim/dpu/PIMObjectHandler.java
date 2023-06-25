package pim.dpu;

import pim.IDPUProxyObject;
public class PIMObjectHandler {
    public int dpuID;
    public int address;
    public PIMObjectHandler(int dpuID, int address){
        this.dpuID = dpuID;
        this.address = address;
    }
}
