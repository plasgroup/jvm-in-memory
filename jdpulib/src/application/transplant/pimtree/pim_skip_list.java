package application.transplant.pimtree;

import com.upmem.dpu.DpuException;
import framework.pim.BatchDispatcher;
import framework.pim.UPMEM;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

import java.util.*;

import static application.transplant.pimtree.PIMTreeCore.executors;
import static application.transplant.pimtree.PIMTreeCore.make_slice;
import static application.transplant.pimtree.ProgramArgs.L2_SIZE;
import static java.util.stream.Collectors.toCollection;

public class pim_skip_list {
    public static dpu_memory_regions dmr;
    public int push_pull_limit_dynamic = L2_SIZE;
    private int length;
    public int BATCH_SIZE = (2100000);
    key_value[] kv_input = new key_value[BATCH_SIZE];
    private Pair<Integer, Integer>[] keys_with_offset_sorted = new Pair[BATCH_SIZE];

    private Long[] i64_input = new Long[BATCH_SIZE];
    private Integer[] back_trace_offset = new Integer[BATCH_SIZE];
    public static final int nr_of_dpus = 4;

    private static Logger pimTreeLogger = PIMLoggers.pimTreeLogger;
    static {
        pimTreeLogger.setEnable(false);
    }
    public void init() {

    }
    public void insert_load(List<key_value> kvs) {
        int n = kvs.size();
        length = n;
        Collections.sort(kvs, new Comparator<key_value>() {
            @Override
            public int compare(key_value t1, key_value t2) {
                return (int) (t1.key - t2.key);
            }

        });
        //List<key_value> kv_input_slice = make_slice(Arrays.stream(kv_input).limit(length).collect(Collectors.toList()));
        n = 0;
        for(int i = 0; i < length; i++){
            if((i == 0) || (kvs.get(i).key != kvs.get(i - 1).key)){
                kv_input[n++] = kvs.get(i);
            }
        }
        length = n;
//        n = length = parlay::pack_into(
//                kvs,
//                parlay::make_slice(parlay::delayed_seq<bool>(
//                n,
//                [&](size_t i) {
//            return (i == 0) || (kvs[i].key != kvs[i - 1].key);
//        })),
//        kv_input_slice);
    }
    public void get_load(List<task_union.get_operation> keys) {
        int n = keys.size();
        length = n;
//
//        parlay::parallel_for(0, n, [&](size_t i) {
//            keys_with_offset_sorted[i] = make_pair(i, keys[i]);
//        });


        for(int i = 0; i < n; i++){
            keys_with_offset_sorted[i] = make_pair(i, keys.get(i).key);
        }

        List<Pair<Integer, Integer>> kwos_slice =
                make_slice(Arrays.stream(keys_with_offset_sorted).toList().subList(0, n).stream().collect(toCollection(ArrayList::new)));


//                parlay::make_slice(keys_with_offset_sorted,
//                keys_with_offset_sorted + n);

        Collections.sort(kwos_slice, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> t1, Pair<Integer, Integer> t2) {
                return (t2.right.compareTo(t1.right));
            }

        });


//       // time_nested("sort", [&]() {
//            parlay::sort_inplace(kwos_slice,
//                                 [](const auto &t1, const auto &t2) {
//                return t1.second < t2.second;
//            });
//        });

        for(int i = 0; i < n; i++){
            i64_input[i] = Long.valueOf(kwos_slice.get(i).right);
            back_trace_offset[i] = kwos_slice.get(i).left;
        }
//        parlay::parallel_for(0, n, [&](size_t i) {
//            i64_input[i] = kwos_slice[i].second;
//            back_trace_offset[i] = kwos_slice[i].first;
//        });
    }


    private Pair<Integer, Integer> make_pair(int i, long key) {
        return new Pair<Integer, Integer>(i, (int) key);
    }

    public void get() {
        Long[] keys_sorted = i64_input;
        int n = length;

//        auto task_starts = parlay::delayed_seq<bool>(n, [&](size_t i) {
//            return i == 0 || keys_sorted[i] != keys_sorted[i - 1];
//        });

        boolean[] task_starts = new boolean[n];
        for(int i = 0; i < task_starts.length; i++){
            if(i == 0 || keys_sorted[i] != keys_sorted[i - 1])
                task_starts[i] = true;
        }

        List<Integer> ll = pack_index(task_starts);
        int llen = ll.size();


        /** Dispatching **/


        BatchDispatcher bd = new BatchDispatcher();

        UPMEM.beginRecordBatchDispatching(bd);
        for(int i = 0; i < llen; i++){
            int targetDPUID = hash_to_dpu(keys_sorted[ll.get(i)], 0, nr_of_dpus);
            pimTreeLogger.logln("key = " + keys_sorted[ll.get(i)] + " dispatch to DPU " + targetDPUID);
            pptr pptr = executors[targetDPUID].p_get(Math.toIntExact(keys_sorted[ll.get(i)]));
            // dispatching to DPU

        }
        try {
            bd.dispatchAll();
        } catch (DpuException e) {
            throw new RuntimeException(e);
        }

        // TODO: should force dispatching all before batchdispatcher be disposed.
        UPMEM.endRecordBatchDispatching();


//
//        dpu_binary_switch_to(dpu_binary::query_binary);
//
//        auto io = alloc_io_manager();
//        // ASSERT(io == io_managers[0]);
//        io->init();
//
//        auto target = parlay::tabulate(llen, [&](int i) {
//            return hash_to_dpu(keys_sorted[ll[i]], 0, nr_of_dpus);
//        });
//
//        auto get_batch = io->alloc<p_get_task, p_get_reply>(direct);
//        time_nested("taskgen", [&]() {
//            get_batch->push_task_from_array_by_isort<false>(
//                    llen,
//                [&](size_t i) {
//                return (p_get_task){.key = keys_sorted[ll[i]]};
//            },
//            make_slice(target), make_slice(op_taskpos, op_taskpos + llen));
//            // filling method
//            // parfor_wrap(0, length, [&](size_t i) {
//            //     int64_t key = keys[i];
//            //     // int target = hash_to_dpu(key, 0, nr_of_dpus);
//            //     p_get_task *pgt = (p_get_task *)batch->push_task_zero_copy(
//            //         target_addr[i], -1, true, op_taskpos + i);
//            //     *pgt = (p_get_task){.key = key};
//            // });
//            io->finish_task_batch();
//        });
//
//        time_nested("exec", [&]() { ASSERT(io->exec()); });
//
//
//        time_nested("fill result", [&]() {
//            auto idx = parlay::delayed_seq<int>(
//                    n, [&](size_t i) { return back_trace_offset[i]; });
//            parlay::parallel_for(0, llen, [&](size_t i) {
//                auto reply =
//                        (p_get_reply *)get_batch->ith(target[i], op_taskpos[i]);
//                int l = ll[i];
//                int r = (i == llen - 1) ? n : ll[i + 1];
//                parlay::parallel_for(l, r, [&](size_t x) {
//                    kv_output[idx[x]] =
//                            (key_value){.key = reply->key, .value = reply->value};
//                });
//            });
//        });
//
//        io.reset();

        // assert(false);
        // return result;
    }

    public static int hash_to_dpu(Long key, int height, int M) {
        return hh(key, height, M);
    }

    private static int hh(Long key, int height, int M) {
        long v = hash64((long)key) + height;
        v = hash64(v);

        return Math.abs((int) (v % M));
    }

    private static long hash64(long u) {
        long v = u * 3935559000370003845L + 2691343689449507681L;
        v ^= v >> 21;
        v ^= v << 37;
        v ^= v >> 4;
        v *= 4768777513237032717L;
        v ^= v << 20;
        v ^= v >> 41;
        v ^= v << 5;
        return v;
    }

    private List<Integer> pack_index(boolean[] taskStarts) {
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < taskStarts.length; i++){
            if(taskStarts[i]) result.add(i);
        }
        return result;
    }

    public void insert() {

    }


    public class dpu_memory_regions {
        public int bbuffer_start;
        public int bbuffer_end;
        public int pbuffer_start;
        public int pbuffer_end;

    }
}
