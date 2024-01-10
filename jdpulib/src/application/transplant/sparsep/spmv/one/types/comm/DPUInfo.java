package application.transplant.sparsep.spmv.one.types.comm;

public class DPUInfo {
    public int block_rows_per_dpu;
    public int start_block_row_dpu;
    public int start_block_dpu;
    public int blocks;
    public int blocks_pad;
    public int merge;
}
