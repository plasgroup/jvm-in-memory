package application.transplant.sparsep.spmv.one.bcooblock;

import application.transplant.index.search.ArrayListProxy;
import application.transplant.sparsep.spmv.one.computation.DPUExecutor;
import application.transplant.sparsep.spmv.one.types.comm.DPUArguments;
import application.transplant.sparsep.spmv.one.types.comm.DPUInfo;
import com.upmem.dpu.DpuException;
import framework.pim.BatchDispatcher;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import framework.pim.dpu.DPUGarbageCollector;

import java.util.ArrayList;
import java.util.List;

import static framework.pim.UPMEM.dpuInUse;

public class Main {
    private static final int INT_SIZE = 4;
    static sparsep.spmv.one.types.data.share.COOMatrix D;
    static sparsep.spmv.one.types.data.share.CSRMatrix C;
    static sparsep.spmv.one.types.data.share.BCSRMatrix B;
    static sparsep.spmv.one.types.data.share.BCOOMatrix A;
    static sparsep.spmv.one.types.comm.PartitionInfo partInfo;
    private static boolean check_correct = false;
    private static List<Integer>[] x; // in-dpu... proxy
    private static int[] y;
    private static boolean blncTakltBlock = true;
    static DPUInfo[] dpu_info;
    static void initVector(List<Integer> vec, int ncols_pad) {
        for(int i = 0; i < vec.size(); ++i) {
            vec.set(i, i % 4 + 1);
        }
    }

    final static int byte_dt = 4;

    public static class Arguments{
        public int block_rows;
        public int start_block_row;
        public int max_block_rows;
        public int row_block_size;
        public int col_block_size;
        public int tcols;
        public int max_blocks;
        public int dummy;
        public int[] start_block;
        public int[] blocks_per_tasklet;
    }

    public static class Params{
        public int fileName;
        public int row_blsize;
        public int col_blsize;
    }

    static BatchDispatcher bd = new BatchDispatcher();

    public static void partition_tsklt_by_block(sparsep.spmv.one.types.data.share.BCOOMatrix bcooMtx, sparsep.spmv.one.types.comm.PartitionInfo part_info, int dpu) {
        int block_offset = dpu * (UPMEM.perDPUThreadsInUse + 2);
        int nnz_offset = dpu * (UPMEM.perDPUThreadsInUse + 1);
        if (UPMEM.perDPUThreadsInUse == 1) {
            part_info.block_split_tasklet[block_offset + 0] = part_info.block_split[dpu];
            part_info.block_split_tasklet[block_offset + 1] = part_info.block_split[dpu + 1];
            System.out.printf("");
            int total_blocks = 0;
            for (int i = 0; i < UPMEM.perDPUThreadsInUse; i++) {
                total_blocks += (part_info.block_split_tasklet[block_offset + i + 1] - part_info.block_split_tasklet[block_offset + i]);
            }
            assert(total_blocks == (part_info.block_split[dpu+1] - part_info.block_split[dpu]));
            return;
        }

        // Compute the matrix splits.
        int block_cnt = (part_info.block_split[dpu + 1] - part_info.block_split[dpu]);
        int block_per_split = block_cnt / UPMEM.perDPUThreadsInUse;
        int rest_blocks = block_cnt % UPMEM.perDPUThreadsInUse;
        int blocks_per_tasklet, nnz_per_tasklet;
        int i,j;

        part_info.block_split_tasklet[block_offset + 0] = part_info.block_split[dpu];
        for(i = 0; i < UPMEM.perDPUThreadsInUse; i++) {
            blocks_per_tasklet = block_per_split;
            if (i < rest_blocks)
                blocks_per_tasklet++;
            part_info.block_split_tasklet[block_offset + i + 1] = part_info.block_split_tasklet[block_offset + i] + blocks_per_tasklet;
        }

        // Sanity Check
        System.out.printf("");
        int total_blocks = 0;
        for (i = 0; i < UPMEM.perDPUThreadsInUse; i++) {
            total_blocks += (part_info.block_split_tasklet[block_offset + i + 1] - part_info.block_split_tasklet[block_offset + i]);
        }
        assert(total_blocks == (part_info.block_split[dpu+1] - part_info.block_split[dpu]));
    }

    public static void Main(String[] args) throws DpuException, NoSuchFieldException, InstantiationException {
        Params p = inputParams(args);
        UPMEMConfigurator configurator = new UPMEMConfigurator();
        UPMEM.initialize(configurator);
        UPMEM.beginRecordBatchDispatching(bd);

        long i;
        int NR_TASKLETS = UPMEM.perDPUThreadsInUse;

        // Initialize input data
        D = readCOOMatrix(p.fileName);
        C = coo2csr(D);
        B = csr2bcsr(C, p.row_blsize, p.col_blsize);
        D = null;
        C = null;
        sortBCSRMatrix(B);
        countNNZperBlockBCSRMatrix(B);
        A = bcsr2bcoo(B);
        B = null;

        // Initialize partition data
        partInfo = partition_init();

        // Load-balance blocks across DPUs
        partition_by_blocks(A, partInfo);

        // Initialize help data with padding if needed
        int ncols_pad = (int) (A.num_block_cols * A.col_block_size);

        if (ncols_pad % (8 / byte_dt) != 0)
            ncols_pad += ((8 / byte_dt) - (A.ncols % (8 / byte_dt)));

        long nrows_pad = A.num_block_rows * A.row_block_size;
        if (nrows_pad % (8 / byte_dt) != 0)
            nrows_pad += ((8 / byte_dt) - (A.nrows % (8 / byte_dt)));

        // Allocate input vector
        for(int dpuID = 0; dpuID < dpuInUse; dpuID++){
            x[dpuID] = (List<Integer>) UPMEM.getInstance().createObject(dpuID, ArrayList.class, ncols_pad);
        }

        // Initialize input vector with arbitrary data
        for(int dpuID = 0; dpuID < dpuInUse; dpuID++)
            initVector(x[dpuID], ncols_pad);
        // now x in all DPU, and be initialized

        // Initialize help data
        dpu_info = new DPUInfo[dpuInUse];
        DPUArguments[] input_args = new DPUArguments[dpuInUse];

        // Max limits for parallel transfers
        long max_block_rows_per_dpu = 0;
        long max_blocks_per_dpu = 0;

        int prev_block_row = 0;
        i = 0;

        // Find padding for block-rows and non-zero elements needed for CPU-DPU transfers
        for(i = 0; i < dpuInUse; i++){
            int blocks, blocks_pad;
            blocks = (partInfo.block_split[(int) (i+1)] - partInfo.block_split[(int) i]);
            if (blocks % 2 != 0) // browind, bcolind
                blocks_pad = blocks + 1;
            else
                blocks_pad = blocks;

            if (blocks_pad > max_blocks_per_dpu)
                max_blocks_per_dpu = blocks_pad;

            int block_rows_dpu = A.bind[partInfo.block_split[(int) (i+1)] - 1].getRowind() - prev_block_row + 1;
            if (block_rows_dpu > max_block_rows_per_dpu)
                max_block_rows_per_dpu = block_rows_dpu;

            // Keep information per DPU
            dpu_info[(int) i].block_rows_per_dpu = block_rows_dpu;
            dpu_info[(int) i].start_block_row_dpu = prev_block_row;
            dpu_info[(int) i].start_block_dpu = partInfo.block_split[(int) i];
            dpu_info[(int) i].blocks = blocks;
            dpu_info[(int) i].blocks_pad = blocks_pad;
            if (i == 0)
                dpu_info[(int) i].merge = 0;
            else if (A.bind[partInfo.block_split[(int) i]].getRowind() == A.bind[partInfo.block_split[(int) i]-1].getColind())
                dpu_info[(int) i].merge = 1;
            else
                dpu_info[(int) i].merge = 0;

            // Find input arguments per DPU
            input_args[(int) i].block_rows = block_rows_dpu;
            input_args[(int) i].start_block_row = prev_block_row;
            input_args[(int) i].tcols = ncols_pad;
            input_args[(int) i].row_block_size = (int) A.row_block_size;
            input_args[(int) i].col_block_size = (int) A.col_block_size;

            // Compute prev_block_row (previous block row) for the next DPU
            if (i != dpuInUse) {
                if (A.bind[partInfo.block_split[(int) (i+1)]].getRowind() == A.bind[partInfo.block_split[(int) (i+1)] - 1].getRowind())
                    prev_block_row = A.bind[partInfo.block_split[(int) (i+1)] - 1].getRowind();
                else
                    prev_block_row = A.bind[partInfo.block_split[(int) (i+1)] - 1].getRowind() + 1;
            } else {
                prev_block_row = (int) A.num_block_rows;
            }

            if(blncTakltBlock)
                // load-balance rows across tasklets
                partition_tsklt_by_block(A, partInfo, (int) i);
            else {
                // load-balance nnz across tasklets
                partition_tsklt_by_nnz(A, partInfo, (int) i);
            }

            int t;
            for (t = 0; t < NR_TASKLETS; t++) {
                // Find input arguments per DPU
                input_args[(int) i].start_block[t] = partInfo.block_split_tasklet[(int) (i * (NR_TASKLETS+2) + t)];
                input_args[(int) i].blocks_per_tasklet[t] = partInfo.block_split_tasklet[(int) (i * (NR_TASKLETS+2) + (t+1))] - partInfo.block_split_tasklet[(int) (i * (NR_TASKLETS+2) + t)];
            }
        }

        // Initializations for parallel transfers with padding needed
        if(byte_dt == 8){
            if (max_block_rows_per_dpu % 2 != 0)
                max_block_rows_per_dpu++;
        }
        if (max_blocks_per_dpu % 2 != 0)
            max_blocks_per_dpu++;

        // Re-allocations for padding needed
        // A.bind = (struct bind_t *) realloc(A.bind, (max_blocks_per_dpu * nr_of_dpus * sizeof(struct bind_t)));
        // A.bval = (val_dt *) realloc(A.bval, (max_blocks_per_dpu * A.row_block_size * A.col_block_size * nr_of_dpus * sizeof(val_dt)));
        y = new int[(int) ((long) dpuInUse * (long) max_block_rows_per_dpu * A.row_block_size)];

        // size calculation probem
        // Count total number of bytes to be transfered in MRAM of DPU
        long total_bytes;

        total_bytes = ((max_blocks_per_dpu) * sparsep.spmv.one.types.data.share.Bind.size()) + (max_blocks_per_dpu * A.row_block_size * A.col_block_size * INT_SIZE) + (ncols_pad * INT_SIZE) + (max_block_rows_per_dpu * A.row_block_size * INT_SIZE);
        assert(total_bytes <= DPUGarbageCollector.heapSpaceSize);

        // Copy input arguments to DPUs
        /** This will be done by proxy dispatching **/
        i = 0;

        DPUExecutor[] dpuExecutors = new DPUExecutor[dpuInUse];
        for(i = 0; i < dpuInUse; i++) {
            input_args[(int) i].max_block_rows = (int) max_block_rows_per_dpu;
            input_args[(int) i].max_blocks = (int) max_blocks_per_dpu;
            dpuExecutors[(int) i] = (DPUExecutor) UPMEM.getInstance()
                    .createObject((int) i, DPUExecutor.class);
            // TODO need transfer to DPU input_args[i] -> dpu#i
            // DPU_ASSERT(dpu_prepare_xfer(dpu, input_args + i));
        }

        // Copy Rowind + Colind
        /** this done by partitioning **/

        // Copy Bvalues (difficult)
        i = 0;

        List<Integer>[] distributedBValues = new List[dpuInUse];
        for(i = 0; i < dpuInUse; i++) {
            //DPU_ASSERT(dpu_prepare_xfer(dpu, A -> bval + (((uint64_t) part_info -> block_split[i]) * A -> row_block_size * A -> col_block_size)));
            int size = (int) (max_blocks_per_dpu * A.row_block_size * A.col_block_size);
            int begin = (int) (((long) partInfo.block_split[(int) i]) * A.row_block_size * A.col_block_size);
            List<Integer> list = (List<Integer>) UPMEM.getInstance().createObject((int)i, ArrayList.class, size);
            for(int index = 0; index < size; index++){
                list.set(index, A.bval[begin + index]);
            }
            distributedBValues[(int) i] = list;
            List<sparsep.spmv.one.types.data.share.Bind> dpuBinds =
                    (List<sparsep.spmv.one.types.data.share.Bind>) UPMEM.getInstance().createObject((int) i, ArrayList.class);
            for(int bindIndex = partInfo.block_split[(int) i]; bindIndex < partInfo.block_split[(int) (i + 1)]; bindIndex++){
                dpuBinds.add(A.bind[bindIndex]);
            }

            Main.Arguments arguments = (Main.Arguments) UPMEM.getInstance().createObject((int) i, Arguments.class);
            dpuExecutors[(int) i].calculate(list, x[(int) i], dpuBinds, arguments);
        }

        // Copy input vector to DPUs
        /** x already in DPU **/

        bd.dispatchAll();

        // Retrieve results for output vector from DPUs
        i = 0;
        for(i = 0; i < dpuInUse; i++) {
            // TODO: get result.
            //  DPU_ASSERT(dpu_prepare_xfer(dpu, y + (i * max_block_rows_per_dpu * A->row_block_size)));
            List<List<Integer>> resultYs = new ArrayList<>();
            for(int dpuID = 0; dpuID < dpuInUse; dpuID++){
                int address = bd.getResult(dpuID);
                resultYs.add((ArrayList<Integer>)
                        UPMEM.generateProxyObject(ArrayListProxy.class, dpuID, address));
            }
        }

        // DPU_ASSERT(dpu_push_xfer(dpu_set, DPU_XFER_FROM_DPU, DPU_MRAM_HEAP_POINTER_NAME, 0, max_block_rows_per_dpu * A->row_block_size * sizeof(val_dt), DPU_XFER_DEFAULT));


        // Merge partial results to the host CPU
        // Host Merge
        long t, n = 0;
        while (n < dpuInUse - 1) {
            long actual_block_rows = dpu_info[(int) n].block_rows_per_dpu - 1;
            if (dpu_info[(int) n].start_block_row_dpu + actual_block_rows == dpu_info[(int) (n+1)].start_block_row_dpu) {
                t = n;
                // Merge multiple y[i] elements computed in different DPUs
                while (dpu_info[(int) n].start_block_row_dpu + actual_block_rows == dpu_info[(int) (t+1)].start_block_row_dpu) {
                    for(long r = 0; r < A.row_block_size; r++) {
                        // n * max_block_rows_per_dpu * A.row_block_size 定位到第n个DPU的位置。
                        y[(int) (n * max_block_rows_per_dpu * A.row_block_size + actual_block_rows * A.row_block_size + r)] +=
                                y[(int) ((t + 1) * max_block_rows_per_dpu * A.row_block_size + r)];
                    }
                    t++;
                }
            }
            n++;
        }

        if(check_correct){
            // Check output
            int[] y_host = new int[(int) nrows_pad];
            spmv_host(y_host, A, x[0]);

            boolean status = true;
            i = 0;
            int j,r;
            for (n = 0; n < dpuInUse; n++) {
                long actual_block_rows = dpu_info[(int) n].block_rows_per_dpu;
                for (j = 0; j < actual_block_rows; j++) {
                    if(j == 0 && dpu_info[(int) n].merge == 1) {
                        continue;
                    }
                    for (r = 0; r < A.row_block_size; r++) {
                        if(y_host[(int) i] != y[(int) (n * max_block_rows_per_dpu * A.row_block_size
                                + j * A.row_block_size + r)] && i < A.nrows) {
                            status = false;
                        }
                        i++;
                    }
                }
            }
            if (status) {
                System.out.println("["  + "OK" + "] Outputs are equal\n");
            } else {
                System.out.println("[" + "ERROR" + "] Outputs differ!\n");
            }

        }

    }

    private static void spmv_host(int[] y, sparsep.spmv.one.types.data.share.BCOOMatrix bcooMtx, List<Integer> x) {
        for(long n=0; n < bcooMtx.num_blocks; n++) {
            long i = bcooMtx.bind[(int) n].rowind;
            long j = bcooMtx.bind[(int) n].colind;
            for(long r=0; r < bcooMtx.row_block_size; r++){
                int acc = 0;
                for(long c=0; c < bcooMtx.col_block_size; c++) {
                    if ((i * bcooMtx.row_block_size + r < bcooMtx.nrows) && (j * bcooMtx.col_block_size + c < bcooMtx.ncols)) {
                        acc += bcooMtx.bval[(int) (n * bcooMtx.col_block_size * bcooMtx.row_block_size + r * bcooMtx.col_block_size + c)] * x.get((int) (j * bcooMtx.col_block_size + c));
                    }
                }
                y[(int) (i * bcooMtx.row_block_size + r)] += acc;
            }
        }
    }


    private static sparsep.spmv.one.types.comm.PartitionInfo partition_init() {
        sparsep.spmv.one.types.comm.PartitionInfo part_info;
        part_info = new sparsep.spmv.one.types.comm.PartitionInfo();

        part_info.block_split = new int[dpuInUse + 2];
        part_info.nnzs_dpu = new int[dpuInUse + 1];
        part_info.block_split_tasklet = new int[dpuInUse * (UPMEM.perDPUThreadsInUse + 2) ];

        return part_info;
    }

    private static sparsep.spmv.one.types.data.share.BCOOMatrix bcsr2bcoo(sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx) {
        sparsep.spmv.one.types.data.share.BCOOMatrix bcooMtx = new sparsep.spmv.one.types.data.share.BCOOMatrix();

        bcooMtx.nrows = bcsrMtx.nrows;
        bcooMtx.ncols = bcsrMtx.ncols;
        bcooMtx.nnz = bcsrMtx.nnz;
        bcooMtx.num_block_rows = bcsrMtx.num_block_rows;
        bcooMtx.num_block_cols = bcsrMtx.num_block_cols;
        bcooMtx.num_blocks = bcsrMtx.num_blocks;
        bcooMtx.num_rows_left = bcsrMtx.num_rows_left;
        bcooMtx.row_block_size = bcsrMtx.row_block_size;
        bcooMtx.col_block_size = bcsrMtx.col_block_size;

        bcooMtx.bind = new sparsep.spmv.one.types.data.share.Bind[bcooMtx.num_blocks];
        bcooMtx.bval = new int[(int) ((bcooMtx.num_blocks + 1) * bcooMtx.row_block_size * bcooMtx.col_block_size)];
        bcooMtx.nnz_per_block = new int[bcooMtx.num_blocks];

        for(long n = 0; n < bcsrMtx.num_block_rows; n++) {
            for(long i = bcsrMtx.browptr[(int) n]; i < bcsrMtx.browptr[(int) (n + 1)]; i++){
                // !important
                bcooMtx.bind[(int) i] = new sparsep.spmv.one.types.data.share.Bind((int) n, bcsrMtx.bcolind[(int) i]);
            }
        }

        // memory copy
        for(int i = 0; i < (bcooMtx.num_blocks + 1) * bcooMtx.row_block_size * bcooMtx.col_block_size; i++) {
            bcooMtx.bval[i] = bcsrMtx.bval[i];
        }

        for(int i = 0; i < bcooMtx.num_blocks;i++)
        {
            bcooMtx.nnz_per_block[i] = bcsrMtx.nnz_per_block[i];
        }
        return bcooMtx;
    }

    private static void countNNZperBlockBCSRMatrix(sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx) {
        double occupancy = 0;
        for(int n=0; n < bcsrMtx.num_block_rows; n++) {
            for(int i = (int) bcsrMtx.browptr[n]; i < bcsrMtx.browptr[n+1]; i++){
                int j = bcsrMtx.bcolind[i];
                for(int r = 0; r < bcsrMtx.row_block_size; r++){
                    for(int c=0; c < bcsrMtx.col_block_size; c++) {
                        if (bcsrMtx.bval[i * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c] != 0)
                            bcsrMtx.nnz_per_block[i]++;
                    }
                }
                occupancy += ((double) bcsrMtx.nnz_per_block[i] / (double) (bcsrMtx.row_block_size * bcsrMtx.col_block_size));
            }
        }
    }

    private static void sortBCSRMatrix(sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx) {
        for(int n = 0; n < bcsrMtx.num_block_rows; n++) {
            int low = (int) bcsrMtx.browptr[n];
            int high = (int) (bcsrMtx.browptr[n+1] - 1);

            quickSortBCSRMatrix(bcsrMtx, low, high);
        }
    }
    static void quickSortBCSRMatrix(sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx, int low, int high) {

        if (low < high) {
            int mid = partitionBCSRMatrix(bcsrMtx, low, high);

            quickSortBCSRMatrix(bcsrMtx, low, mid - 1);
            quickSortBCSRMatrix(bcsrMtx, mid + 1, high);
        }
    }


    static int partitionBCSRMatrix(sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx, int low, int high) {

        int pivot = bcsrMtx.bcolind[high];
        int i = low - 1;
        int temp_ind;
        int temp_val;

        for(long j = low; j <= high - 1; j++) {
            if(bcsrMtx.bcolind[(int) j] < pivot) {
                i++;
                // swap(i, j)
                temp_ind = bcsrMtx.bcolind[i];
                bcsrMtx.bcolind[i] = bcsrMtx.bcolind[(int) j];
                bcsrMtx.bcolind[(int) j] = temp_ind;
                for(int r = 0; r < bcsrMtx.row_block_size; r++) {
                    for(int c = 0; c < bcsrMtx.col_block_size; c++) {
                        temp_val = bcsrMtx.bval[i * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c];
                        bcsrMtx.bval[i * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c] = bcsrMtx.bval[(int) (j * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c)];
                        bcsrMtx.bval[(int) (j * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c)] = temp_val;
                    }
                }
            }
        }

        // swap(i+1, high)
        temp_ind = bcsrMtx.bcolind[i+1];
        bcsrMtx.bcolind[i+1] = bcsrMtx.bcolind[high];
        bcsrMtx.bcolind[high] = temp_ind;
        for(int r=0; r < bcsrMtx.row_block_size; r++) {
            for(int c=0; c < bcsrMtx.col_block_size; c++) {
                temp_val = bcsrMtx.bval[(i+1) * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c];
                bcsrMtx.bval[(i+1) * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c] = bcsrMtx.bval[high * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c];
                bcsrMtx.bval[high * bcsrMtx.row_block_size * bcsrMtx.col_block_size + r * bcsrMtx.col_block_size + c] = temp_val;
            }
        }
        return (i+1);
    }

    public static void partition_by_blocks(sparsep.spmv.one.types.data.share.BCOOMatrix bcooMtx, sparsep.spmv.one.types.comm.PartitionInfo partInfo){
        if (dpuInUse == 1) {
            partInfo.block_split[0] = 0;
            partInfo.block_split[1] = bcooMtx.num_blocks;
            partInfo.nnzs_dpu[0] = bcooMtx.nnz;
            return;
        }

        // Compute the matrix splits.
        int block_cnt = bcooMtx.num_blocks;
        int block_per_split = block_cnt / dpuInUse;
        int rest_blocks = block_cnt % dpuInUse;
        int blocks_per_dpu;
        int i,j;

        partInfo.block_split[0] = 0;
        for(i = 0; i < dpuInUse; i++) {
            blocks_per_dpu = block_per_split;
            if (i < rest_blocks)
                blocks_per_dpu++;

            /** migrate to DPU **/

            partInfo.block_split[i+1] = partInfo.block_split[i] + blocks_per_dpu;
            for(j = partInfo.block_split[i]; j < partInfo.block_split[i+1]; j++) {
                A.bind[j] = (sparsep.spmv.one.types.data.share.Bind)
                        UPMEM.getInstance().createObject(i, sparsep.spmv.one.types.data.share.Bind.class, A.bind[j].rowind, A.bind[j].colind);
                partInfo.nnzs_dpu[i] += bcooMtx.nnz_per_block[j];
            }
        }

        // Sanity Check
        int total_blocks = 0;
        int total_nnzs = 0;
        for (i = 0; i < dpuInUse; i++) {
            total_blocks += (partInfo.block_split[i+1] - partInfo.block_split[i]);
            total_nnzs += partInfo.nnzs_dpu[i];
        }
        assert(total_blocks == bcooMtx.num_blocks);
        assert(total_nnzs == bcooMtx.nnz);
    }
    private static sparsep.spmv.one.types.data.share.BCSRMatrix csr2bcsr(sparsep.spmv.one.types.data.share.CSRMatrix csrMtx, int row_block_size, int col_block_size) {
        sparsep.spmv.one.types.data.share.BCSRMatrix bcsrMtx;
        bcsrMtx = new sparsep.spmv.one.types.data.share.BCSRMatrix();

        int num_block_rows = (csrMtx.nrows + row_block_size - 1) / row_block_size;
        int num_block_cols = (csrMtx.ncols + col_block_size - 1) / col_block_size;
        int num_rows_left = csrMtx.nrows % row_block_size;

        bcsrMtx.nrows = csrMtx.nrows;
        bcsrMtx.ncols = csrMtx.ncols;
        bcsrMtx.nnz = csrMtx.nnz;
        bcsrMtx.row_block_size = row_block_size;
        bcsrMtx.col_block_size = col_block_size;
        bcsrMtx.num_block_rows = num_block_rows;
        bcsrMtx.num_block_cols = num_block_cols;
        bcsrMtx.num_rows_left = num_rows_left;

        int[] bAp;
        int[] bAj;
        int[] bAx;

        //tmp variables
        int num_blocks = 0;
        int[] block_count;

        int[] bAp_next;
        long I, J;
        long i, j, k, j0, di;
        int a_ij;

        bAp = new int[num_block_rows + 2];
        bAp_next = new int[(num_block_rows+2)];
        block_count = new int[num_block_cols];

        //Phase I: Count the exact number of new blocks to create.
        bAp[0] = 0;
        if(num_rows_left == 0) {
            for(I=0; I<num_block_rows; I++) {
                for(i=I * row_block_size; i < (I+1) * row_block_size; i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j/col_block_size;
                        if(block_count[(int) J] == 0) {
                            num_blocks++;
                            block_count[(int) J]++;
                        }
                    }
                }
                bAp[(int) (I+1)] = num_blocks;
                for(i = 0; i < num_block_cols; i++)
                    block_count[(int) i] = 0;
            }
        } else {
            for(I=0; I<num_block_rows-1; I++) {
                for(i=I * row_block_size; i < (I+1) * row_block_size; i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j/col_block_size;
                        if(block_count[(int) J] == 0) {
                            num_blocks++;
                            block_count[(int) J]++;
                        }
                    }
                }
                bAp[(int) (I+1)] = num_blocks;
                for(i = 0; i < num_block_cols; i++)
                    block_count[(int) i] = 0;
            }
            for(i = (num_block_rows-1) * row_block_size; i < ((num_block_rows-1) * row_block_size + num_rows_left); i++) {
                for (k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                    j = csrMtx.colind[(int) k];
                    J = j/col_block_size;
                    if (block_count[(int) J] == 0) {
                        num_blocks++;
                        block_count[(int) J]++;
                    }
                }
            }
            bAp[num_block_rows] = num_blocks;
            for(i = 0; i < num_block_cols; i++)
                block_count[(int) i] = 0;
        }

        bcsrMtx.num_blocks = num_blocks;
        bAj = new int[num_blocks + 1];
        bAx = new int[(num_blocks + 1) * row_block_size * col_block_size];
        int[] blocks = new int[row_block_size * col_block_size * num_block_cols];

        for(int p = 0; p < num_block_rows + 1; p++){
            bAp_next[p] = bAp[p];
        }
        bcsrMtx.nnz_per_block = new int[num_blocks];

        //Phase II: Copy all blocks.
        if(num_rows_left == 0) {
            for(I=0; I < num_block_rows; I++) {
                for(i = I * row_block_size, di=0; di<row_block_size; di++, i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j / col_block_size;
                        j0 = J * col_block_size;
                        a_ij = csrMtx.values[(int) k];
                        blocks[(int) (J * row_block_size * col_block_size + di * col_block_size + j - j0)] = a_ij;
                        block_count[(int) J]++;
                    }
                }
                for(i = I*row_block_size, di=0; di<row_block_size; di++, i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j / col_block_size;
                        j0 = J * col_block_size;

                        if(block_count[(int) J] > 0) {
                            long k_next = bAp_next[(int) I];
                            bAj[(int) k_next] = (int) J;
                            for(int p = 0; p < row_block_size * col_block_size; i++){
                                bAx[(int) (k_next * row_block_size * col_block_size + p)] = blocks[(int) (J * row_block_size * col_block_size + p)];
                            }
                            bAp_next[(int) I]++;
                            assert(bAp_next[(int) I] <= bAp[(int) (I+1)]);
                            block_count[(int) J] = 0;
                            for(int p = 0; p < row_block_size * col_block_size; p++){
                                blocks[(int) (J * col_block_size * row_block_size + p)] = 0;
                            }
                        }
                    }
                }
            }
        } else {
            for(I = 0; I < num_block_rows-1; I++) {
                for(i = I*row_block_size, di=0; di<row_block_size; di++, i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j / col_block_size;
                        j0 = J * col_block_size;
                        a_ij = csrMtx.values[(int) k];
                        blocks[(int) (J * row_block_size * col_block_size + di * col_block_size + j - j0)] = a_ij;
                        block_count[(int) J]++;
                    }
                }

                for(i = I*row_block_size, di=0; di < row_block_size; di++, i++) {
                    for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                        j = csrMtx.colind[(int) k];
                        J = j / col_block_size;
                        j0 = J * col_block_size;

                        if (block_count[(int) J] > 0) {
                            long k_next = bAp_next[(int) I];
                            bAj[(int) k_next] = (int) J;
                            for(int p = 0; p < row_block_size * col_block_size; p++){
                                bAx[(int) (k_next * row_block_size * col_block_size + p)] = blocks[(int) (J * row_block_size * col_block_size)];
                            }
                            bAp_next[(int) I]++;
                            assert(bAp_next[(int) I] <= bAp[(int) (I+1)]);
                            block_count[(int) J] = 0;
                            for(int p = 0; p < row_block_size * col_block_size; p++){
                                blocks[(int) (J * col_block_size * row_block_size + p)] = 0;
                            }
                        }
                    }
                }

            }

            for(i = (num_block_rows-1)*row_block_size, di=0; di < num_rows_left; di++, i++) {
                for(k = csrMtx.rowptr[(int) i]; k < csrMtx.rowptr[(int) (i+1)]; k++) {
                    j = csrMtx.colind[(int) k];
                    J = j / col_block_size;
                    j0 = J * col_block_size;
                    a_ij = csrMtx.values[(int) k];
                    blocks[(int) (J * row_block_size * col_block_size + di * col_block_size + j - j0)] = a_ij;
                    block_count[(int) J]++;
                }
            }

            for(i = (num_block_rows-1)*row_block_size, di=0; di<num_rows_left; di++, i++) {
                for(k = csrMtx.rowptr[(int) i]; k< csrMtx.rowptr[(int) (i+1)]; k++) {
                    j = csrMtx.colind[(int) k];
                    J = j / col_block_size;
                    j0 = J * col_block_size;

                    if(block_count[(int) J] > 0) {
                        long k_next = bAp_next[num_block_rows-1];
                        bAj[(int) k_next] = (int) J;
                        for(int p = 0; p < row_block_size * col_block_size; p++){
                            bAx[(int) (k_next * row_block_size * col_block_size + p)] = blocks[(int) (J * row_block_size * col_block_size + p)];
                        }
                        bAp_next[num_block_rows-1]++;
                        assert(bAp_next[num_block_rows-1] <= bAp[num_block_rows]);
                        block_count[(int) J] = 0;
                        for(int p = 0; p < row_block_size * col_block_size ; p++){
                            blocks[(int) (J * col_block_size * row_block_size + p)] = 0;
                        }
                    }
                }
            }

        }

        bcsrMtx.browptr = bAp;
        bcsrMtx.bcolind = bAj;
        bcsrMtx.bval = bAx;

        return bcsrMtx;


    }

    private static sparsep.spmv.one.types.data.share.CSRMatrix coo2csr(sparsep.spmv.one.types.data.share.COOMatrix cooMtx) {

        sparsep.spmv.one.types.data.share.CSRMatrix csrMtx;
        csrMtx =  new sparsep.spmv.one.types.data.share.CSRMatrix();

        csrMtx.nrows = cooMtx.nrows;
        csrMtx.ncols = cooMtx.ncols;
        csrMtx.nnz = cooMtx.nnz;
        csrMtx.rowptr = new long[(csrMtx.nrows + 1)];
        csrMtx.colind = new long[(csrMtx.nnz + 1)];
        csrMtx.values = new int[csrMtx.nnz + 8];

        for(int i = 0; i < cooMtx.nnz; ++i) {
            int rowIndx = cooMtx.rowindx[i];
            csrMtx.rowptr[rowIndx]++;
        }

        int sumBeforeNextRow = 0;
        for(int rowIndx = 0; rowIndx < csrMtx.nrows; ++rowIndx) {
            int sumBeforeRow = sumBeforeNextRow;
            sumBeforeNextRow += csrMtx.rowptr[rowIndx];
            csrMtx.rowptr[rowIndx] = sumBeforeRow;
        }
        csrMtx.rowptr[csrMtx.nrows] = sumBeforeNextRow;

        for(int i = 0; i < cooMtx.nnz; ++i) {
            int rowIndx = cooMtx.rowindx[i];
            int nnzIndx = (int) csrMtx.rowptr[rowIndx]++;
            csrMtx.colind[nnzIndx] = cooMtx.colind[i];
            csrMtx.values[nnzIndx] = cooMtx.values[i];
        }

        for(int rowIndx = csrMtx.nrows - 1; rowIndx > 0; --rowIndx) {
            csrMtx.rowptr[rowIndx] = csrMtx.rowptr[rowIndx - 1];
        }
        csrMtx.rowptr[0] = 0;

        return csrMtx;
    }

    private static sparsep.spmv.one.types.data.share.COOMatrix readCOOMatrix(Object fileName) {
        return null;
    }

    private static Params inputParams(String[] args) {
        return new Params();
    }

    public static void partition_tsklt_by_nnz(sparsep.spmv.one.types.data.share.BCOOMatrix bcooMtx, sparsep.spmv.one.types.comm.PartitionInfo part_info, int dpu) {

        int block_offset = dpu * (UPMEM.perDPUThreadsInUse + 2);
        int nnz_offset = dpu * (UPMEM.perDPUThreadsInUse + 1);
        if (UPMEM.perDPUThreadsInUse == 1) {
            part_info.block_split_tasklet[block_offset + 0] = part_info.block_split[dpu];
            part_info.block_split_tasklet[block_offset + 1] = part_info.block_split[dpu+1];
            System.out.println("");
            int total_blocks = 0;
            for (int i = 0; i < UPMEM.perDPUThreadsInUse; i++) {
                total_blocks += (part_info.block_split_tasklet[block_offset + i+1] - part_info.block_split_tasklet[block_offset + i]);
            }
            assert(total_blocks == (part_info.block_split[dpu+1] - part_info.block_split[dpu]));
            return;
        }

        // Compute the matrix splits.
        int nnz_cnt = part_info.nnzs_dpu[dpu];
        int nnz_per_split = nnz_cnt / UPMEM.perDPUThreadsInUse;
        int curr_nnz = 0;
        int block_start = part_info.block_split[dpu];
        int split_cnt = 0;
        int i;

        part_info.block_split_tasklet[block_offset + 0] = block_start;
        for (i = part_info.block_split[dpu]; i < part_info.block_split[dpu+1]; i++) {
            curr_nnz += bcooMtx.nnz_per_block[i];
            if (curr_nnz >= nnz_per_split) {
                ++split_cnt;
                block_start = i + 1;
                if (split_cnt <= UPMEM.perDPUThreadsInUse) {
                    part_info.block_split_tasklet[block_offset + split_cnt] = block_start;
                    curr_nnz = 0;
                }
            }
        }

        // Fill the last split with remaining elements
        if (curr_nnz < nnz_per_split && split_cnt <= UPMEM.perDPUThreadsInUse) {
            split_cnt++;
            part_info.block_split_tasklet[block_offset + split_cnt] = part_info.block_split[dpu+1];
        }

        // If there are any remaining blocks merge them in last partition
        if (split_cnt > UPMEM.perDPUThreadsInUse) {
            part_info.block_split_tasklet[block_offset + UPMEM.perDPUThreadsInUse] = part_info.block_split[dpu + 1];
        }

        // If there are remaining threads create empty partitions
        for (i = split_cnt + 1; i <= UPMEM.perDPUThreadsInUse; i++) {
            part_info.block_split_tasklet[block_offset + i] = part_info.block_split[dpu+1];
        }

        // Sanity Check
        System.out.println("");
        int total_blocks = 0;
        for (i = 0; i < UPMEM.perDPUThreadsInUse; i++) {
            total_blocks += (part_info.block_split_tasklet[block_offset + i+1] - part_info.block_split_tasklet[block_offset + i]);
        }
        assert(total_blocks == (part_info.block_split[dpu+1] - part_info.block_split[dpu]));
    }
}
