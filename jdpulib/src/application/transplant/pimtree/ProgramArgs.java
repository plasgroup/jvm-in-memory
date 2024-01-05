package application.transplant.pimtree;

import java.util.ArrayList;
import java.util.List;

public class ProgramArgs {
    static ArrayList<String> file = new ArrayList<>();
    static boolean noCheck = true;
    static boolean noPrint = true;
    static boolean noDetail = true;
    static double get = 0.0;
    static double update = 0.0;
    static double predecessor = 0.0;
    static double scan = 0.0;
    static double insert = 0.0;
    static double remove = 0.0;
    static int init_batch_size = 1000000;
    static int test_batch_size = 1000000;
    static int output_batch_size = 1000000;
    static int bias = 1;
    static int top_level_threads = 1;
    static int wait_microsecond = 0;
    static double alpha = 0.0;
    static List<Integer> length;
    static List<String> output;
    static final int L2_SIZE_LOG = 4;
    static final int L2_SIZE = (1 << L2_SIZE_LOG);
    static int push_pull_limit_dynamic = L2_SIZE;
    static String generate_all_test_cases = "";
    static boolean init_state = false;

    static {
        initialize_length();
        initialize_output();
    }

    private static void initialize_output() {
        output = new ArrayList<>();
    }
//
//    public Object add_argument(String s, String s1) {
//    }

    public static void initialize_length() {
        length = new ArrayList<>(2);
        length.set(0, 40000000);
        length.set(1, 20000000);
    }

    public static ArrayList<String> getFile() {
        return file;
    }

    public static double getAlpha() {
        return alpha;
    }

    public static double getGet() {
        return get;
    }

    public static double getInsert() {
        return insert;
    }

    public static double getPredecessor() {
        return predecessor;
    }

    public static double getRemove() {
        return remove;
    }

    public static double getScan() {
        return scan;
    }

    public static double getUpdate() {
        return update;
    }

    public static int getBias() {
        return bias;
    }

    public static int getInit_batch_size() {
        return init_batch_size;
    }

    public static int getL2Size() {
        return L2_SIZE;
    }

    public static int getL2SizeLog() {
        return L2_SIZE_LOG;
    }

    public static int getOutput_batch_size() {
        return output_batch_size;
    }

    public static int getPush_pull_limit_dynamic() {
        return push_pull_limit_dynamic;
    }

    public static int getTest_batch_size() {
        return test_batch_size;
    }

    public static int getTop_level_threads() {
        return top_level_threads;
    }

    public static int getWait_microsecond() {
        return wait_microsecond;
    }

    public static List<Integer> getLength() {
        return length;
    }

    public static List<String> getOutput() {
        return output;
    }

    public static String getGenerate_all_test_cases() {
        return generate_all_test_cases;
    }

    public boolean is_used(String s) {
        return true;
    }
}
