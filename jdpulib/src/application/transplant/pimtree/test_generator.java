package application.transplant.pimtree;

import java.util.List;
import java.util.Random;

public class test_generator {
    Random r = new Random();
    public test_generator(Object o, int initBatchSize) {
    }

    // TODO: support more oeprations
    public void fill_with_random_ops(List<operation> o) {
        for(int i = 0; i < o.size(); i++){
            operation op = getRandomOperation();
            o.set(i, op);
        }
    }

    private operation getRandomOperation() {
        int ordinal = r.nextInt(7);
        operation op = null;
        switch (ordinal){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                op = buildGetOperation(r.nextInt());
                return op;

        }
        return null;
    }

    private operation buildGetOperation(int key) {
        operation op = new operation();
        op.type = operation_t.get_t;
        op.tsk = new task_union();
        op.tsk.t = new task_union.get_operation(key);

        op.tsk.key = key;
        return op;
    }

    private operation buildInsertOperation(int key, int value) {
        operation op = new operation();
        op.type = operation_t.insert_t;
        op.tsk = new task_union();
        op.tsk.t = new task_union.insert_operation(key, value);

        op.tsk.key = key;
        op.tsk.value = value;
        return op;
    }


    public void fill_with_biased_ops(Object o, boolean b, double v, int bias, batch_parallel_oracle oracle, int testBatchSize) {
    }
}
