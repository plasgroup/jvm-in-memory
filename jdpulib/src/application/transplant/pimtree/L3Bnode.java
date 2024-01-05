package application.transplant.pimtree;

public class L3Bnode {
    public int height;
    public int size;
    public pptr up;
    public pptr right;
    public pptr[] addrs;
    long[] keys = new long[ApplicationConfiguration.DB_SIZE];

}
