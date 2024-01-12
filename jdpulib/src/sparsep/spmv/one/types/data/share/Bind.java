package sparsep.spmv.one.types.data.share;

import java.io.Serializable;

public class Bind implements Serializable {
    public int rowind;
    public int colind;

    public Bind(int rowind, int colind){
        this.rowind = rowind;
        this.colind = colind;
    }
    public static long size() {
        return 8;
    }

    public int getColind() {
        return colind;
    }
    public int getRowind(){
        return rowind;
    }
}
