package application.transplant.pimtree;

public class L3node {
    public long key;
    public int height;
    public pptr down;
    public pptr[] left;
    public pptr[] right;

    public L3node(long key, byte height) {
        this.key = key;
        this.height = height;
    }

    public L3node() {

    }
}
