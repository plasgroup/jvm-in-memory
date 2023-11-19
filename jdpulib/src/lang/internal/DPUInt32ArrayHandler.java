package lang.internal;

import algorithm.DPUTreeNodeProxyAutoGen;
import pim.UPMEM;
import pim.dpu.ProxyHelper;

public class DPUInt32ArrayHandler {
    private final int dpuID;
    private final Class proxyClass;
    int address;
    int len;
    boolean referenceType = false;
    public DPUInt32ArrayHandler(int dpuID, int addr, int len) {
        this.address = addr;
        this.dpuID = dpuID;
        this.len = len;
        this.proxyClass = Integer.TYPE;
    }

    public int get(int i) {
        int elementBytes = UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.getInt32(address + 4+ i * 4);
        return elementBytes;
    }
}
