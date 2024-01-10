package application.transplant.sparsep.spmv.one.types.comm;

import framework.pim.UPMEM;

public class DPUArguments {
    public int block_rows;
    public int start_block_row;
    public int max_block_rows;
    public int row_block_size;
    public int col_block_size;
    public int tcols;
    public int max_blocks;
    int dummy;
    public int[] start_block;
    public int[] blocks_per_tasklet;
    public DPUArguments(){
        start_block = new int[UPMEM.perDPUThreadsInUse];
        blocks_per_tasklet = new int[UPMEM.perDPUThreadsInUse];
    }
}
