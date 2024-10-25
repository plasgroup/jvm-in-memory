/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.bst;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import framework.pim.dpu.cache.DPULookupTableManager;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

import static framework.pim.dpu.RPCHelper.*;

public class NBodySystemProxy extends NBodySystem implements IDPUProxyObject {
    public Integer dpuID;
    public Integer address;

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public int getAddr() {
        return address;
    }

    public NBodySystemProxy() {
        super();
    }

    public NBodySystemProxy(int dpuID, int mramAddress) {
        super();
        this.dpuID = dpuID;
        this.address = mramAddress;
    }

    @Override
    public int test() {
        invokeMethod(dpuID, address, "application/bst/NBodySystem", "test:()I");
        return getIReturnValue(dpuID);
    }

    @Override
    public void advance(final float dt) {
        invokeMethod(dpuID, address, "application/bst/NBodySystem", "advance:(F)V", dt);
        return;
    }

    @Override
    public float energy() {
        invokeMethod(dpuID, address, "application/bst/NBodySystem", "energy:()F");
        return getFReturnValue(dpuID);
    }

    @Override
    public NBodySystem _new() {
        invokeMethod(dpuID, address, "application/bst/NBodySystem", "_new:()Lapplication/bst/NBodySystem;");
        return (NBodySystem) getAReturnValue(dpuID, NBodySystemProxy.class);
    }
}
