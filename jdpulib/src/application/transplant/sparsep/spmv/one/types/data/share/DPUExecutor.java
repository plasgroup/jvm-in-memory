package application.transplant.sparsep.spmv.one.computation;

import application.transplant.sparsep.spmv.one.bcooblock.Main;
import sparsep.spmv.one.types.data.share.Bind;

import java.util.ArrayList;
import java.util.List;

@sparsep.spmv.one.computation.DPUDispatchable
public class DPUExecutor {


    public List<Integer> calculate(List<Integer> bval, List<Integer> x, List<Bind> binds, Main.Arguments params){
        int tasklet_id = 0;
        // Load parameters
        int block_rows = params.block_rows;
        int global_start_block_row = params.start_block_row;
        int max_block_rows = params.max_block_rows;
        int row_block_size = params.row_block_size;
        int col_block_size = params.col_block_size;
        int tcols = params.tcols;
        int max_blocks = params.max_blocks;
        int global_start_block = params.start_block[0];
        int start_block = params.start_block[tasklet_id];
        int blocks_per_tasklet = params.blocks_per_tasklet[tasklet_id];

        int row_size = row_block_size * 4;
        int col_size = col_block_size * 4;
        int block_size = row_block_size * col_block_size * 4;

        // Initialize help cache to temporarily store results
        int[] cache_acc = new int[row_size];

        // Initialize input vector cache
        int[] cache_x = new int[col_size];

        // Initialize output vector cache
        int[] cache_y = new int[row_size];

        // Help variables
        int acc;
        int i, diff;

        List<Integer> y = new ArrayList<>(max_block_rows * row_block_size);
        int mram_temp_addr_y = 0;
        // Use cache_acc cache to initialize the output vector elements in MRAM with zeros
        if(tasklet_id == 0) {
            for(i=0; i < row_block_size; i++) {
                cache_acc[i] = 0;
            }

            int iter = block_rows;
            for(i=0; i < iter; i++) { // 迭代行大小次
                // 似乎在写入行大小长度的数据
                // size(cache_acc) = row size
                for(int j = 0; j < row_block_size; j++) {
                    y.set(mram_temp_addr_y / 4 + j, cache_acc[j]);
                    mram_temp_addr_y += row_size;
                }
            }
        }

        // If there is no work, return
        if (blocks_per_tasklet == 0) return y;

        // Find offsets per tasklet for browptr, bcolind (indexes)
        Bind current_bind = binds.get(0);
        int prev_block_row = current_bind.rowind;

        // Initialize cache for bvalues
        int mram_base_addr_val = 0; // (int) (mram_base_addr_bind + (max_blocks * sizeof(struct bind_t)));

        // start_block stores the start block of i-th tasklet

        mram_base_addr_val += ((start_block - global_start_block) * block_size) / 4;
        int[] cache_val = new int[block_size / 4];

        for(int p = 0; p > block_size / 4; p++) {
            cache_val[p] = bval.get(p);
        }

        //mram_read((__mram_ptr void const *) (mram_base_addr_val), cache_val, block_size);
        //mram_base_addr_val += block_size;

        // Initialize cache_acc
        int r, c;
        for(r = 0; r < row_block_size; r++) {
            cache_acc[r] = 0;
        }

        int bind_index = 0;

        // SpMV
        // Iterate over block rows
        for (i=0; i < blocks_per_tasklet; i++) {
                // If all non-zero blocks of the same block row have been traversed, write the final values for the output vector elements in MRAM
                if (current_bind.rowind != prev_block_row) {
                    diff = prev_block_row - global_start_block_row;
                    mram_temp_addr_y = y.get(diff * row_size); // (mram_base_addr_y + (diff * row_size));

                    // Store the final values for the output vector element in MRAM  (block-row granularity)

                    // read.   mram_temp_addr_y -> cache_y
                    // mram_read((__mram_ptr void const *) (mram_temp_addr_y), cache_y, row_size);
                    for(int p = 0; p < row_size / 4; p++) {
                        cache_y[p] = y.get(p);
                    }
                    for(r = 0; r < row_block_size; r++) {
                        if (cache_acc[r] != 0)
                            cache_y[r] += cache_acc[r];
                        cache_acc[r] = 0;
                    }
                    for(int p = 0; p < row_size / 4; p++) {
                        y.set(p, cache_y[p]);
                    }

                    prev_block_row = current_bind.rowind;
                }

                // For each non-zero block: 1. get input vector value, 2. multiply and add
                for(int p = 0; p < col_size / 4; p++) {
                    cache_x[p] = x.get(current_bind.colind * col_size / 4);
                }
                for(r = 0; r < row_block_size; r++) {
                    acc = 0;
                    for(c = 0; c < col_block_size; c++) {
                        if (cache_val[r * col_block_size + c]  != 0)
                            cache_acc[r] += cache_val[r * col_block_size + c] * cache_x[c];
                    }
                }

                // Get the next non-zero block
                current_bind = binds.get(++bind_index);//seqread_get(current_bind, sizeof(*current_bind), &sr_bind);
                for(int p = 0; p < block_size / 4; p++){
                    cache_val[mram_base_addr_val] = bval.get(mram_base_addr_val / 4 + p);
                }
                mram_base_addr_val += block_size;
            }

            // Store the final values for the output vector element in MRAM  (block-row granularity)
            diff = prev_block_row - global_start_block_row;
            mram_temp_addr_y = ((diff * row_size));

            for (int p = 0; p < row_size / 4; p++){
                cache_y[p] = y.get(mram_temp_addr_y / 4 + p);
            }

            for(r = 0; r < row_block_size; r++) {
                if (cache_acc[r] != 0)
                    cache_y[r] += cache_acc[r];
            }

            for(int p = 0 ; p < row_size / 4; p++){
                y.set(mram_temp_addr_y / 4 + p, cache_y[p]);
            }
            return y;
        }
}
