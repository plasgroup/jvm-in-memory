package application.transplant.index.search.proxy;

import framework.pim.struct.IDPUProxyObject;

public class ObjectProxy extends Object implements IDPUProxyObject {
    @Override
    public int getAddr() {
        return 0;
    }

    @Override
    public int getDpuID() {
        return 0;
    }

    public ObjectProxy() {
        super();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
