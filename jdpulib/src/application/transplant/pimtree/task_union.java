package application.transplant.pimtree;

public class task_union {
    public task t;
    long key;
    long value;
    long lkey;
    long rkey;
    public get_operation g(){
        return (get_operation) t;
    }
    public update_operation u(){
        return (update_operation) t;
    }
    public predecessor_operation p(){
        return (predecessor_operation) t;
    }
    public scan_operation s(){
        return (scan_operation) t;
    }
    public insert_operation i(){
        return (insert_operation) t;
    }
    public remove_operation r(){
        return (remove_operation) t;
    }

    public static class get_operation extends task{
        long key;

        public get_operation(int key) {
            this.key = key;
        }
    }
    public static class update_operation extends task{
        long key;
        long value;
    }
    public static class predecessor_operation extends task{
        long key;
    }
    public static class scan_operation extends task{
        long lkey;
        long rkey;
    }
    public static class insert_operation extends task{
        long key;
        long value;

        public insert_operation(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    public static class remove_operation extends task{
        long key;
    }
}


