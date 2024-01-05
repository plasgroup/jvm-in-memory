package framework.lang.struct;

public class DummyProxy implements IDPUProxyObject{
    Integer address = -1;
    Integer dpuID = -1;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }
}
