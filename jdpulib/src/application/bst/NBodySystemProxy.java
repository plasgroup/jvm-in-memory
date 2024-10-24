/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.bst;

import javalib.IDPUProxyObject;
import javalib.logger.Logger;
import javalib.logger.PIMLoggers;

import static javalib.dpu.ProxyHelper.*;

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
        invokeMethod(dpuID, address, NBodySystem.class, "test");
        return getIReturnValue(dpuID);
    }

    @Override
    public void advance(final float dt) {
        invokeMethod(dpuID, address, NBodySystem.class, "advance", dt);
        return;
    }

    @Override
    public float energy() {
        invokeMethod(dpuID, address, NBodySystem.class, "energy");
        return getFReturnValue(dpuID);
    }

    @Override
    public NBodySystem _new() {
        invokeMethod(dpuID, address, NBodySystem.class, "_new");
        return (NBodySystem) getAReturnValue(dpuID, NBodySystemProxy.class);
    }
}
