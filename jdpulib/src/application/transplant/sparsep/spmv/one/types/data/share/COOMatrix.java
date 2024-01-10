package sparsep.spmv.one.types.data.share;
public class COOMatrix {
    public int nrows;

    public int ncols;
    public int nnz;
    public int[] rowindx;
    public long[] colind;
    public int[] values;
}

