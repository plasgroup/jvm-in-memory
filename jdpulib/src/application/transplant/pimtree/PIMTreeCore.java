package application.transplant.pimtree;

import framework.pim.UPMEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class PIMTreeCore {
    public static int OPERATION_NR_ITEMS = 7;



    public static int push_pull_limit_dynamic;
    public static int num_wait_microsecond;
    public static pim_skip_list[] pim_skip_list_drivers;

    static int num_top_level_threads = 1;
    static final int _block_size = 1000;

    private static final int MAX_BATCH_SIZE = (int) 5e3;
    static task_union.get_operation[] get_ops = new task_union.get_operation[MAX_BATCH_SIZE];
    static task_union.update_operation[] update_ops = new task_union.update_operation[MAX_BATCH_SIZE];
    static task_union.predecessor_operation[] predecessor_ops = new task_union.predecessor_operation[MAX_BATCH_SIZE];
    static task_union.scan_operation[] scan_ops = new task_union.scan_operation[MAX_BATCH_SIZE];
    static task_union.insert_operation[] insert_ops = new task_union.insert_operation[MAX_BATCH_SIZE];
    static task_union.remove_operation[] remove_ops = new task_union.remove_operation[MAX_BATCH_SIZE];

    static batch_parallel_oracle oracle = new batch_parallel_oracle();
    static int[] cnts = new int[OPERATION_NR_ITEMS];

    static AtomicInteger batch_number = new AtomicInteger(0);
    static long[] op_count = new long[OPERATION_NR_ITEMS];
    private static int T, n, rounds;
    private static int l;
    public static final int nr_of_dpus = 4;
    public static PIMTreeExecutor[] executors = new PIMTreeExecutor[UPMEM.dpuInUse];

    public static void execute(List<operation> ops, int load_batch_size,
                               int execute_batch_size, int threads)  {
        System.out.printf("execute n=%d batchsize=%d,%d\n",
                ops.size(),
                load_batch_size,
                execute_batch_size);
        assert (threads <= num_top_level_threads);
        for(int i = 0; i < op_count.length; i++){
            op_count[i] = 0;
        }

        //memset(op_count, 0, sizeof(op_count));
        init(ops, load_batch_size, execute_batch_size);
        // atomic<int> num_finished_threads = 0;
        int num_finished_threads = 0;

        for(int tid = 0; tid < threads; tid ++){
//            cpu_coverage_timer->start();
//            pim_coverage_timer->start();
//            pim_coverage_timer->end();
//            time_nested("global_exec", [&]() {

                System.out.printf("%d / %d *****!!! start\n", tid, threads);
                while (true) {
                    {
                        ReentrantLock lock = new ReentrantLock();
                        lock.lock();
                        // unique_lock<mutex> lock(load_batch_mutex);
                        // time_start("load batch");
                        operation_t op_type = operation_t.empty_t;
                        while (true) {
                            op_type = batch_ready(execute_batch_size);
                            if (op_type != operation_t.empty_t) break;
                            boolean next_batch =
                                    load_one_batch(ops, load_batch_size);
                            if (!next_batch) {
                                op_type = batch_ready(1); // finish remaining tasks
                                break;
                            }
                        }
                        // time_end("load batch");

                        if (op_type == operation_t.empty_t) {
                            break; // !next_batch
                        }
                        run_batch(op_type, lock, tid);  // may unlock here
                    }
                }

                System.out.println(tid + "*****!!! finished"); // << endl;
                num_finished_threads++;
                if (tid == 0) {
                    while (num_finished_threads < threads) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        //this_thread::sleep_for(chrono::microseconds(100));
                    }
                }
            //});
//            cpu_coverage_timer->end();
//            pim_coverage_timer->start();
//            pim_coverage_timer->end();
            if (tid == 0) {
                assert(num_finished_threads == threads);
            }
        }

//        parlay::parallel_for(
//                0, threads,
//        [&](size_t tid) {
//
//        },
//        1);

        System.out.printf("execute finish!\n");
        // fflush(stdout);
        // TODO: oracle
        //System.out.println(oracle.inserted.size());
    }
    public static <T> List<T> make_slice(List<T> ops) {
        return ops;
    }

    static int scan_start;
    private static void run_batch(operation_t op_type, Lock mut, int tid) {
        int count = (int) op_count[op_type.ordinal()];
        if(op_type != operation_t.scan_t)
            op_count[op_type.ordinal()] = 0;

        switch (op_type) {
            case empty_t:
                break;
            case get_t: {
                PIMTreeCore.get(make_slice(Arrays.stream(get_ops).toList().subList(0, count)), mut, tid);
                break;
            }
            case update_t: {
                assert(false);
                break;
            }
            case predecessor_t: {
                PIMTreeCore.predecessor(
                        make_slice(Arrays.stream(predecessor_ops).toList().subList(0, count)),
                mut, tid);
                break;
            }
            case scan_t: {
                int scan_batch = 10000;
                if (count - scan_start >= scan_batch) {
                    PIMTreeCore.scan(make_slice(Arrays.stream(scan_ops).toList().subList(scan_start, scan_start + scan_batch)), mut, tid);
                    scan_start += scan_batch;
                } else if(count - scan_start > 1) {

                    for(int loop_i = 0; loop_i < count - scan_start; loop_i++){
                        scan_ops[loop_i] = scan_ops[loop_i + scan_start];
                    }

                    op_count[op_type.ordinal()] = count - scan_start;
                    scan_start = 0;
                    PIMTreeCore.scan(make_slice(
                            Arrays.stream(scan_ops).toList().subList(0, (int) op_count[op_type.ordinal()]))
                           , mut, tid);
                    op_count[op_type.ordinal()] = 0;
                }
                else {
                    op_count[op_type.ordinal()] = 0;
                    scan_start = 0;
                    mut.unlock();
                }
                break;
            }
            case insert_t: {
                PIMTreeCore.insert(make_slice(
                        Arrays.stream(insert_ops).toList().subList(0, count)), mut, tid);
                break;
            }
            case remove_t: {
                PIMTreeCore.remove(make_slice(
                        Arrays.stream(remove_ops).toList().subList(0, count)),
                        mut, tid);
                break;
            }
            default: {
                assert(false);
                break;
            }
        }
    }

    private static void remove(List<task_union.remove_operation> removeOperations, Lock mut, int tid) {
    }

    private static void insert(List<task_union.insert_operation> insertOperations, Lock mut, int tid) {
    }

    private static void scan(List<task_union.scan_operation> scanOperations, Lock mut, int tid) {
    }

    private static void predecessor(List<task_union.predecessor_operation> predecessorOperations, Lock mut, int tid) {
    }

    private static <T> void get(List<task_union.get_operation> ops, Lock mut, int tid) {
        List<Long> ops_sequence = new ArrayList<>();
        pim_skip_list ds = pim_skip_list_drivers[tid];

        int ds_offset = 0;
        int n = ops.size();
        {
//            if (check_result) {
//                ops_sequence =
//                        parlay::tabulate(n, [&](size_t i) { return ops[i].key; });
//            }
            List<task_union.get_operation> ops2 = make_slice(ops); // make_slice((int64_t*)ops.begin(), (int64_t*)ops.end());
            //time_nested("get load", [&]() { d
            System.out.println("load");
            ds.get_load(ops2);

            //});
            mut.unlock();
        }

        {
            Lock rLock = new ReentrantLock();
            rLock.lock();
            System.out.printf("(%d) func void get(List<task_union.get_operation> ops, Lock mut, int tid) tid = %d\n",
                    batch_number.getAndIncrement(), tid
            );
            //cout << (batch_number++) << " " << __FUNCTION__ << " " << tid << endl;
            System.out.println("get");
            //  time_nested("get", [&]() {
            ds.get(); // execute
            rLock.unlock();
        //});

//
//            if (check_result) {
//                auto v1 = parlay::make_slice(ds->kv_output, ds->kv_output + n);
//                auto pred = oracle.predecessor_batch(make_slice(ops_sequence));
//                auto v2 = parlay::tabulate(pred.size(), [&](size_t i) {
//                    if (pred[i].key != ops_sequence[i]) {
//                        return (key_value){.key = INT64_MIN, .value = INT64_MIN};
//                    } else {
//                        return pred[i];
//                    }
//                });
//                if (!parlay::equal(v1, v2)) {
//                    int n = v1.size();
//                    for (int i = 0; i < n; i++) {
//                        if (v1[i] != v2[i]) {
//                            printf("[%8d]\t", i);
//                            cout << "k=" << ops[i].key << "\tv1=" << v1[i]
//                                    << "\tv2=" << v2[i] << endl;
//                        }
//                    }
//                }
//            }
        }
    }

    static List<Integer>[] sums = new List[OPERATION_NR_ITEMS];

    private static boolean load_one_batch(List<operation> ops, int load_batch_size) {
        if (T >= rounds) {
            return false;
        }


        // block position and length
        int l = T * load_batch_size;
        int r = Math.min((T + 1) * load_batch_size, n);
        int len = r - l;


        // get a block\
        List<operation> mixed_op_batch = ops.subList(l, r);


        int[] c = new int[OPERATION_NR_ITEMS];

        for(int i = 0; i < len; i+= _block_size){
            int s = i;
            int e = Math.min(len, i + _block_size);
            for(int j = s; j < e; j++){
                int t = mixed_op_batch.get(j).type.ordinal();
                assert(j < len);
                assert(t >= 0 && t < OPERATION_NR_ITEMS);
                c[t]++; // t-th operation's count ++
            }
            for (int j = 0; j < OPERATION_NR_ITEMS; j++) {
                sums[j].set(i, c[j]); // j-th's operation's i block has c[j] operations
                System.out.printf("%d-th's operation's %d-th block has %d operations\n", j, i, c[j]);
            }
        }


        for (int j = 0; j < OPERATION_NR_ITEMS; j++) {
            int s = 0; // 幺元
            for(int k = 0; k < sums[j].size(); k++){
                s += sums[j].get(k);
            }
            cnts[j] = s;
//                    parlay::scan_inplace(parlay::make_slice(sums[j]),
//                    parlay::addm<size_t>());
        }

        c = new int[OPERATION_NR_ITEMS];
        for(int i = 0; i < len; i += _block_size){
            int s = i;
            int e = Math.min(len, s + _block_size);
            for (int j = 0; j < OPERATION_NR_ITEMS; j++) {
                // TODO: some problem here.
                c[j] = 0;//(int) (sums[j].get(i) + op_count[j]);
            }
            for (int j = s; j < e; j++) {

                operation t = mixed_op_batch.get(j);

                operation_t operation_type = t.type;
                int x = operation_type.ordinal();

                switch (operation_type) {
                    case get_t -> get_ops[c[x]++] = t.tsk.g();
                    case update_t -> update_ops[c[x]++] = t.tsk.u();
                    case predecessor_t -> predecessor_ops[c[x]++] = t.tsk.p();
                    case scan_t -> scan_ops[c[x]++] = t.tsk.s();
                    case insert_t -> insert_ops[c[x]++] = t.tsk.i();
                    case remove_t -> remove_ops[c[x]++] = t.tsk.r();
                    default -> {
                        assert (false);
                    }
                }
            }
        }


        for (int j = 0; j < OPERATION_NR_ITEMS; j++) {
            op_count[j] += cnts[j];
        }
        T++;
        return true;
    }

    private static operation_t batch_ready(int execute_batch_size) {
        int scan_execute_batch_size = execute_batch_size / 100;
        for (int j = 1; j < OPERATION_NR_ITEMS; j++) {
            if (op_count[j] >= execute_batch_size && op_count[j] > 0) {
                return operation_t.values()[j];
            }
        }
        if (op_count[operation_t.scan_t.ordinal()] >= scan_execute_batch_size && op_count[operation_t.scan_t.ordinal()] > 0) {
            return operation_t.scan_t;
        }
        return operation_t.empty_t;
    }

    private static void init(List<operation> ops, int load_batch_size, int execute_batch_size) {
        Arrays.fill(op_count, 0);

        l = num_blocks(load_batch_size, _block_size);
        sums = new List[OPERATION_NR_ITEMS];
        for (int i = 0; i < OPERATION_NR_ITEMS; i++) {
            sums[i] = new ArrayList<>(l); //parlay::sequence<size_t>(l);
            for(int j = 0; j < l; j++){
                sums[i].add(0);
            }
            cnts[i] = 0;
        }
        n = ops.size();
        rounds = num_blocks(n, load_batch_size);
        T = 0;
    }

    private static int num_blocks(int loadBatchSize, int blockSize) {
        return (int) Math.ceil((double) loadBatchSize / blockSize);
    }


}
