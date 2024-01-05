package application.transplant.pimtree;

public class Bnode {
    long height;
    long len;
    pptr up, left, right;
    mdbptr keys, addrs;
    mdbptr caddrs, padding;
}
