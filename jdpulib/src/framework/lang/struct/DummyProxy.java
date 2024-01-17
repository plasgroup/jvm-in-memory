package framework.lang.struct;

public class DummyProxy implements IDPUProxyObject{
    public Integer address = -1;
    public Integer dpuID = -1;
    public boolean isInCPU = false;
    public Object instance;
    public DummyProxy(Object instance) {
        this.instance = instance;
        isInCPU = true;
    }

    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }
    public DummyProxy(){

    }
    public DummyProxy(int dpuID, int address){
        this.dpuID = dpuID;
        this.address = address;
    }
}
