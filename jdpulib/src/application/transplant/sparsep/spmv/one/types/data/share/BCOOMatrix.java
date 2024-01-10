package sparsep.spmv.one.types.data.share;

import java.util.List;

public class BCOOMatrix {
    public long col_block_size;
    public long num_block_cols;
    public int ncols;
    public long num_block_rows;
    public long row_block_size;
    public int nrows;
    public sparsep.spmv.one.types.data.share.Bind[] bind;


    // TODO, very difficult to be distributed into different spaces
    // how to distribute an array to different spaces.
    public int[] bval;
    public int nnz;
    public int num_rows_left;
    public int num_blocks;
    public int[] nnz_per_block;
}