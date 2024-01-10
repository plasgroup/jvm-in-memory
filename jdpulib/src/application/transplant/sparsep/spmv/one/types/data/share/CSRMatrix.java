package sparsep.spmv.one.types.data.share;

public class CSRMatrix {
    public int nrows;
    public int ncols;
    public int nnz;
    public long[] rowptr;
    public long[] colind;
    public int[] values;
}
