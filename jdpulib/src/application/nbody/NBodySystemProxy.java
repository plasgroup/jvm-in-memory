/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.nbody;

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
    public void advance(final float dt) {
        invokeMethod(dpuID, address, "application/nbody/NBodySystem", "advance:(F)V", dt);
        return;
    }

    @Override
    public float energy() {
        invokeMethod(dpuID, address, "application/nbody/NBodySystem", "energy:()F");
        return getFReturnValue(dpuID);
    }

    @Override
    public NBodySystem _new() {
        invokeMethod(dpuID, address, "application/nbody/NBodySystem", "_new:()Lapplication/nbody/NBodySystem;");
        return (NBodySystem) getAReturnValue(dpuID, NBodySystemProxy.class);
    }
}
