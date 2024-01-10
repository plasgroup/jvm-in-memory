package application.transplant.pimtree;

import java.util.ArrayList;
import java.util.List;


public class frontend_by_generation implements frontend {
    int init_n, test_n;
    List<Double> pos = new ArrayList<>();
    int bias;
    batch_parallel_oracle oracle = new batch_parallel_oracle();
    int init_batch_size;
    int test_batch_size;
    final int OPERATION_NR_ITEMS = 7;

    public frontend_by_generation(int _init_n, int _test_n, List<Double> _pos,
                                  int _bias, int _init_batch_size, int _test_batch_size){
        this.init_n = _init_n;
        this.test_batch_size = _test_batch_size;
        this.init_batch_size = _init_batch_size;
        this.bias = _bias;
        this.pos = _pos;
        this.test_n = _test_n;
    }

    @Override
    public List<operation> init_tasks() {
        List<Double> init_pos = new ArrayList<>(OPERATION_NR_ITEMS);
        for(int i = 0; i < OPERATION_NR_ITEMS; i++){
            init_pos.add(0.0);
        }
        init_pos.set(operation_t.insert_t.ordinal(), 1.0);
        // test_generator tg = new test_generator(make_slice(init_pos), init_batch_size);
        test_generator tg = new test_generator(init_pos, init_batch_size);
        List<operation> ops = new ArrayList<>(init_n);
        for(int i = 0; i < init_n; i++){
            ops.add(null);
        }
        tg.fill_with_random_ops(make_slice(ops));
//        auto kvs = parlay::delayed_seq<key_value>(ops.size(), [&](size_t i) {
//            return (key_value){.key = ops[i].tsk.i.key,
//                               .value = ops[i].tsk.i.value};
//        });


        // from the generated operations push kvs to oracle
        List<key_value> kvs = new ArrayList<>();
        //
        for(int i = 0; i < ops.size(); i++)
        {
            kvs.add(new key_value(ops.get(i).tsk.key, ops.get(i).tsk.value));
        }


        oracle.init(make_slice(kvs));
        return ops; // return the ops generated
    }

    private <T> List<T> make_slice(List<T> ops) {
        return ops;
    }

    // generate test tasks
    @Override
    public List<operation> test_tasks() {
        assert(pos.size() == OPERATION_NR_ITEMS);
        test_generator tg =
                new test_generator(make_slice(this.pos), test_batch_size);
        List<operation> ops = new ArrayList<>(test_n);
        tg.fill_with_biased_ops(make_slice(ops), false, 0.0, bias, oracle, test_batch_size);
        return ops;
    }

}