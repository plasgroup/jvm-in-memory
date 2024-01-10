package sparsep.spmv.one.types.data.share;

public class BCSRMatrix {
    public int nrows;
    public int ncols;
    public int nnz;
    public int num_block_rows;
    public int num_block_cols;
    public int num_blocks;
    public int col_block_size;
    public int row_block_size;
    public int num_rows_left;
    public int[] browptr;
    public int[] bcolind;
    public int[] bval;
    public int[] nnz_per_block;
}
