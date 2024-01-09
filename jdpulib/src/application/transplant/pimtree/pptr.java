package application.transplant.pimtree;

public class pptr {
    public int offset;
    int id;
    Object addr;

    public pptr(int dpuId, Object addr) {
        this.id = dpuId;
        this.addr = addr;
    }

    public pptr() {

    }
}
