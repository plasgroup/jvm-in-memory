package sparsep.spmv.one.types.comm;

public class PartitionInfo {
    public int[] block_split;
    public int[] nnzs_dpu;
    public int[] block_split_tasklet;
}
