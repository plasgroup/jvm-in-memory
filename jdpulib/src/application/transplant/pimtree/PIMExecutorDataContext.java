package application.transplant.pimtree;


import static application.transplant.pimtree.ApplicationConfiguration.DB_SIZE;

public class PIMExecutorDataContext {
    public static final long L2_HEIGHT = 3;
    public static byte[] l3buffer;
    public static int l3cnt = 8;
    public static int recv_block_task_cnt;
    public static int l3ab_L3_n;
    public static int l3bcnt = 1;
    public static L3Bnode l3b_skip_list_root;
    public static L3node l3_skip_list_root;
    public static L3Bnode l3ab_tree_root;
    public static int[] newnode_size;
    public static final int NODE_NUM_CNT = 100;
    public static int[] num_of_node = new int[NODE_NUM_CNT];
    public static Object[] mod_values;
    public static pptr[] mod_values2;
    public static int[] L3_lfts;
    public static int[] L3_rts;




    public static final int MAX_L3_HEIGHT = 20;
    public static byte[] max_height_shared;
    public static InValidObject INVALID_DPU_ADDR = new InValidObject();
    public static int INVALID_DPU_ID = Integer.MAX_VALUE;


    public static int pnode_pcnt = 1;
    public static int htcnt;
    public static Object[] bufferA_shared;
    public static Object[] bufferB_shared;


    public static int VARLEN_BUFFER_SIZE = 64;
    public static int NR_TASKLETS = 1;

    public static int LX_HASHTABLE_SIZE = ((4 << 20) >> 3);
    public static int CACHE_HEIGHT = (2);
    public static int L3_BUFFER_SIZE = (6 << 20); // 6 MB
    public static int  B_BUFFER_SIZE = (3 << 19); // 1.5 MB
    public static int  DB_BUFFER_SIZE = (17 << 20); // 17 MB
    public static int  P_BUFFER_SIZE = (15 << 19); // 7.5MB
    public static int M_BUFFER_SIZE = (L3_BUFFER_SIZE >> 10);
    public static Pnode[] pbuffer = new Pnode[P_BUFFER_SIZE / 24];
    public static long remove_type = 1;
    public static int HF_DB_SIZE = DB_SIZE >> 1;
    public static long[] scnnkeys = new long[DB_SIZE + HF_DB_SIZE + 3];
    public static pptr[] scnnaddrs = new pptr[DB_SIZE + HF_DB_SIZE + 3];
    public static Object[] mrambuffer = new Object[M_BUFFER_SIZE];

    public static long change_key_type = 2;
    public static long underflow_type = 4;
    public static pptr[] mod_addrs;
    public static pptr[] mod_addrs2;
    public static long[] mod_type;
    public static long[] mod_type2;
    public static long[] mod_keys;
    public static long[] mod_keys2;


    public static int DPU_ID;
}
