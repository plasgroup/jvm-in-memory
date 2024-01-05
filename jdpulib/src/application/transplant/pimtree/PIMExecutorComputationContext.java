package application.transplant.pimtree;



import framework.pim.UPMEM;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import  application.transplant.pimtree.PIMExecutorDataContext.*;

import static application.transplant.pimtree.ApplicationConfiguration.DB_SIZE;
import static application.transplant.pimtree.PIMExecutorDataContext.*;
//import  pimtree.PIMTreeExecutor.alloc_db;

public class PIMExecutorComputationContext{
    ht_slot[] ht = new ht_slot[LX_HASHTABLE_SIZE];

    public void insertKeyValue(int key, int value){
        int htLength = ht.length;
        int addr = hash_to_addr(key, LX_HASHTABLE_SIZE);
        for(int j = addr; j < htLength; j = (j + 1) % htLength){
            if(ht[j].v == null){
                ht[j] = new ht_slot(addr, new Pnode(key, value));
                break;
            }
        }
    }


    public PIMExecutorComputationContext(){
        for(int i = 0; i < ht.length; i++){
            ht[i] = new ht_slot(0, null);
        }

    }




    /*
    * 初始化L3
    * 设置Bn, 初始化up, right
    *
    * */
    public void L3_init(pptr down){
        assert (l3bcnt == 1);
        L3Bnode bn = new L3Bnode();
        l3b_node_init(bn, 0, null, null);
        bn.size = 1;
        bn.keys[0] = Long.MIN_VALUE;
        bn.addrs[0] = down;
        l3ab_tree_root = bn;
    }

    private L3node get_new_L3(long key, byte height, int maddrIndex) {
        int size = L3_node_size(height);
        // __mram_ptr void* maddr = reserve_space_L3(size);
        //byte[] buffer = new byte[sizeof(L3node) + sizeof(pptr) * 2 * MAX_L3_HEIGHT];
        L3node nn = init_L3(key, height, null, maddrIndex);

        return nn;
    }

    public void node_count_add(int n, int a) {
        num_of_node[Math.min(n, NODE_NUM_CNT - 1)] += a;
    }


    public void b_newnode(Bnode newnode, mdbptr keys, mdbptr addrs, mdbptr caddrs, long height) {
        // IN_DPU_ASSERT(bcnt == 1, "bi! l1cnt\n");
        // IN_DPU_ASSERT((sizeof(Bnode) & 7) == 0, "bn! invlen\n");
        Bnode nn = newnode;
        nn.len = 0;
        nn.height = height;
        nn.up = nn.left = nn.right = null;
        nn.keys = keys;
        nn.addrs = addrs;
        nn.caddrs = caddrs;
        nn.padding = (mdbptr.InvalidPtr());
        data_block_init(keys);
        data_block_init(addrs);
        data_block_init(caddrs);

        nn = newnode;
        // 因为其需要从wram的nn写入到mram，所以才出现了这一句代码。
        //m_write_single(&nn, newnode, sizeof(Bnode));
    }

    public void data_block_init(mdbptr db) {
        db.la = new len_addr(0, mdbptr.InvalidPtr());
    }

    private L3node init_L3(long key, byte height, Object o, int maddrIndex) {
        L3node nn = new L3node();
        nn.key = key;
        nn.height = height;
        // 猜测  |l3node data nn | left xxxxx (height个指针) | right: (height个指针)|
        nn.left = new pptr[height]; //(mppptr)(maddr + sizeof(L3node));
        nn.right = new pptr[height]; // (mppptr)(maddr + sizeof(L3node) + sizeof(pptr) * height);

        // memset(buffer + sizeof(L3node), -1, sizeof(pptr) * height * 2); //因此这里x2
        return nn;
    }


    public Object ht_search(ht_slot[] ht, long key,
                                    BiFunction<ht_slot, Long, Integer> filter) {
        int ipos = hash_to_addr(key, LX_HASHTABLE_SIZE);
        int pos = ipos;
        while (true) {
            ht_slot hs = ht[pos];  // pull to wram
            int v = filter.apply(hs, key);
            if (v == -1) {  // empty slot
                return null;
                // continue;
            } else if (v == 0) {  // incorrect value
                pos = (pos + 1) & (LX_HASHTABLE_SIZE - 1);
            } else if (v == 1) {  // correct value;
                return hs.v;
            }
            assert (pos != ipos);
        }
    }





    private int hash_to_addr(long key, long M) {
        return hh_dpu(key, M);
    }

    private int hh_dpu(long key, long m) {
        return (int) (key & (m - 1));
    }

    int p_ht_get(ht_slot v, long key) {
        if (v.v == null) {
            return -1;
        }
        Pnode addr = v.v;
        if (addr.key == key) {
            return 1;
        }
        return 0;
    }

    public pptr p_get(int key) {
        Object htv = ht_search(ht, key, (_v, _key) -> {
            if (_v.v == null) {
                return -1;
            }
            Pnode addr = _v.v;
            if (addr.key == key) {
                return 1;
            }
            return 0;
        });
        if (htv == INVALID_DPU_ADDR || htv == null) {
            return new pptr(INVALID_DPU_ID, INVALID_DPU_ADDR);
        } else {
            return new pptr(DPU_ID, htv);
        }
    }
    public void p_newnode(long _key, long _value, long height, Pnode newnode) {
        Pnode nn = new Pnode();
        nn.key = _key;
        nn.height = height;
        nn.value = _value;
        newnode = nn;

        //m_write(&nn, newnode, sizeof(Pnode));
        // IN_DPU_ASSERT(LX_HASHTABLE_SIZE == lb(LX_HASHTABLE_SIZE),
        //               "hh_dpu! not 2^x\n");
        int ret = ht_insert(ht, new Integer[]{htcnt}, hash_to_addr(_key, LX_HASHTABLE_SIZE), newnode);

        //(void)ret;
    }

    @ImportantCheck
    // htcnt value may inequal to bst
    private int ht_insert(ht_slot[] ht, Integer[] htcnt, int pos, Pnode newnode) {
        int ipos = pos;
        ht_slot hs;
        hs = ht[pos];
        while (hs.v != null) {  // find slot
            pos = (pos + 1) & (LX_HASHTABLE_SIZE - 1);
            hs = ht[pos];
            assert (pos != ipos);
        }
        ht_slot hh = new ht_slot(ipos, newnode);
        ht[pos] = hh;

        htcnt[0] = htcnt[0] + 1;
        return pos;
    }


    public Pnode alloc_pn() {
        Pnode pnode = new Pnode();
        pbuffer[pnode_pcnt++] = pnode;
        return pnode;
        //SPACE_IN_DPU_ASSERT(pcnt < (P_BUFFER_SIZE / sizeof(Pnode)), "rsp! of\n");

    }

    @WarnLowPerformance
    public void b_search(Bnode nn, int len, List<Long> keys, List<Long> repkeys, List<pptr> repaddrs, List<Long> heights, int l, int l1, int l2, int l3) {
        Bnode bn;
        bn = nn;
        //m_read_single(nn, &bn, sizeof(Bnode));
        int nnlen = (int)bn.len;
        mdbptr keysdb = bn.keys, addrsdb = bn.addrs, caddrsdb = bn.caddrs;
        int nnht = (int)bn.height;

        mdbptr.data_block wram_keys = new mdbptr.data_block();
        mdbptr.data_block wram_addrs = new mdbptr.data_block();
        mdbptr.data_block wram_caddrs = new mdbptr.data_block();

        Object[] nnkeys = wram_keys.data;
        Object[] nnaddrs = wram_addrs.data;
        Object[] nncaddrs = wram_caddrs.data;

        for (int i = 0; i < len; i++) {
            repkeys.set(i, Long.MIN_VALUE);
        }

// #ifdef KHB_DEBUG
//     bool hasmin = false;
// #endif

        // bool exitt = false;

        for (int i = 0; i < nnlen; i += DB_SIZE) {
            int curlen = Math.min(DB_SIZE, nnlen - i);
            // IN_DPU_ASSERT(in_dbbuffer(keysdb) && in_dbbuffer(addrsdb),
            //               "bsc! inv2\n");

            wram_keys.data[0] = keysdb;
            wram_addrs.data[0] = addrsdb;
            wram_caddrs.data[0] = caddrsdb;

//            m_read_single(keysdb, &wram_keys, sizeof(data_block));
//            m_read_single(addrsdb, &wram_addrs, sizeof(data_block));
//            m_read_single(caddrsdb, &wram_caddrs, sizeof(data_block));

            for (int j = 0; j < curlen; j++) {
                long nnkey = (long) nnkeys[j];

                // IN_DPU_ASSERT(valid_pptr(nnaddrs[j], NR_DPUS), "bsc! inv
                // addr!\n");
                for (int k = 0; k < len; k++) {
                    if (nnkey <= keys.get(k) && nnkey >= repkeys.get(k)) {
                        repkeys.set(k, nnkey);
                        pptr actual_addr = (pptr) nnaddrs[j];
                        pptr local_addr = (pptr) nncaddrs[j];

                        // IN_DPU_ASSERT(equal_pptr(local_addr, null_pptr),
                        //               "bs! invlocal\n");

                        pptr nxt_step = (local_addr.addr == null)
                                ? actual_addr
                                : local_addr;
                        repaddrs.set(k, nxt_step);

                        // IN_DPU_ASSERT(
                        //     (nnht > 0 && in_bbuffer((mBptr)actual_addr.addr) &&
                        //      in_bbuffer((mBptr)nxt_step.addr)) ||
                        //         (nnht == 0 && in_pbuffer((mPptr)actual_addr.addr)
                        //         &&
                        //          in_pbuffer((mPptr)nxt_step.addr)),
                        //     "bsc! nnht\n");

                        if (heights != null) {
                            boolean leave = (nxt_step.id != DPU_ID) ||
                                    (!in_bbuffer((Bnode) nxt_step.addr));
                            boolean record = (heights.get(k) >= L2_HEIGHT) ||
                                    (heights.get(k) == 1 && nnht <= 1);
                            if (leave || record) {
                                heights.set(k, PPTR_TO_U64(actual_addr));
                                // IN_DPU_ASSERT(heights[k] > L2_HEIGHT, "bsc!
                                // ht\n");
                            } else {
                                // IN_DPU_ASSERT((heights[k] == 1) && (!leave),
                                //               "bsc! ht2\n");
                            }
                        }
                    }
                }
            }
            keysdb = wram_keys.la.nxt;
            addrsdb = wram_addrs.la.nxt;
            caddrsdb = wram_caddrs.la.nxt;
        }
    }

    private long PPTR_TO_U64(pptr addr) {
        int[] data = new int[8];
        // id
        data[0] = (byte) ((addr.id >> 8) & 0xFF);
        data[1] = (byte) ((addr.id) & 0xFF);
        data[2] = (byte) ((addr.offset >> 8) & 0xFF);
        data[3] = (byte) ((addr.offset) & 0xFF);
        data[4] = (byte) ((addr.hashCode() >> 24) & 0xFF);
        data[5] = (byte) ((addr.hashCode() >> 16) & 0xFF);
        data[6] = (byte) ((addr.hashCode() >> 8) & 0xFF);
        data[7] = (byte) ((addr.hashCode()) & 0xFF);
        int s = 0;
        int base = 1;
        for(int i = 0; i < 8; i++){
            s += data[7 - i] * base;
            base <<= 1;
        }
        return s;
    }
    Bnode bbuffer_start, bbuffer_end;
    Bnode[] bbuffer = new Bnode[B_BUFFER_SIZE / 24];

    public  boolean in_bbuffer(Bnode addr) {
        return true;
        //return addr >= bbuffer_start && addr < bbuffer_end;
    }

    //    public  mdbptr alloc_db() {
//        pptr recycle = // 从GC的自由列表中获取。alloc_node(&free_list_data_block, 1);
//        mdbptr ret;
//        if (recycle.id == 0) {
//            ret = dbbuffer[dbcnt];
//            dbcnt ++;
//        } else {
//            ret = (mdbptr)recycle.addr;
//        }
//        data_block_init(ret);
//        assert (dbcnt < (DB_BUFFER_SIZE / sizeof(data_block)));
//        return ret;
//    }
//
//    public  Bnode alloc_bn() {
//        pptr recycle = alloc_node(&free_list_bnode, 1);
//        Bnode ret;
//        if (recycle.id == 0) {
//            ret = bbuffer[bcnt];
//            bcnt ++;
//        } else {
//            ret = (mBptr)recycle.addr;
//        }
//        assert (bcnt < (B_BUFFER_SIZE / sizeof(Bnode)));
//        return ret;
//    }
    public  long l3b_search(long key, L3Bnode addr, pptr value) {
        L3Bnode tmp = l3b_skip_list_root;
        L3Bnode bn = new L3Bnode();
        while (true) {
            // mram_read(tmp, &bn, sizeof(L3Bnode));

            //m_read(tmp, &bn, sizeof(L3Bnode));
            long pred = Long.MIN_VALUE;
            pptr nxt_addr = null;
            for (int i = 0; i < bn.size; i++) {
                if (bn.keys[i] <= key) {
                    pred = bn.keys[i];
                    nxt_addr = bn.addrs[i];
                }
            }
            // IN_DPU_ASSERT((valid_pptr(nxt_addr) || bn.height == 0), "bs! inv\n");
            if (bn.height > 0) {
                tmp = pptr_to_ml3bptr(nxt_addr);
            } else {
            //*addr = tmp;
            addr.keys = tmp.keys;
            addr.addrs = tmp.addrs;
            addr.height = tmp.height;
            addr.right = tmp.right;
            addr.up = tmp.up;
            addr.size = tmp.size;

            //*value = nxt_addr;
            value.addr = nxt_addr.addr;
            value.id = nxt_addr.id;
                return pred;
            }
        }
    }

    private  L3Bnode pptr_to_ml3bptr(pptr x) {
        return (L3Bnode) x.addr;
    }

    private  void l3b_node_init(L3Bnode bn, int ht, pptr up, pptr right) {
        bn.height = ht;
        bn.up = up;
        bn.right = right;
        bn.size = 0;
        for (int i = 0; i < DB_SIZE; i++) {
            bn.keys[i] = Long.MIN_VALUE;
            bn.addrs[i] = null;
        }
    }


    public  int L3_node_size(byte height) {
        // TODO mapping to index
        return 40 + 8 * height * 2;
    }

    private  Object reserve_space_L3(int size) {
        // TODO: check the size?
        Object ret = l3buffer[l3cnt];
        l3cnt += size;
        // SPACE_IN_DPU_ASSERT(l3cnt < L3_BUFFER_SIZE, "rs3! of\n");
        return ret;
    }


    public  void L3_insert_parallel(int length, int l, long[] keys, byte[] heights, pptr[] down, int[] newnode_size, byte[] maxHeightShared, Object[] right_predecessor_shared, Object[] right_newnode_shared) {
        int OLD_NODES_DPU_ID = Integer.MAX_VALUE - 1;
        int tasklet_id = 0;
        byte max_height = 0;
        int NR_TASKLETS = 1;
        L3node[] newnode = new L3node[length];

        if (tasklet_id == 0) {
            for (int i = 0; i < NR_TASKLETS; i++) {
                newnode_size[i] = (int)reserve_space_L3(newnode_size[i]);
            }
        }

        //Object[] maddr = newnode_size[tasklet_id];
        int maddrIndex = tasklet_id;
        for (int i = 0; i < length; i++) {
            newnode[i] = get_new_L3(keys[i], heights[i], maddrIndex);
            newnode[i].down = down[i];
            maddrIndex += L3_node_size(heights[i]);
            if (heights[i] > max_height) {
                max_height = heights[i];
            }
        }

        // mutex_lock(L3_lock);
        L3node[] predecessor = new L3node[max_height];
        L3node[] left_predecessor = new L3node[max_height];
        L3node[] left_newnode = new L3node[max_height];
        // mutex_unlock(L3_lock);

        L3node right_predecessor = (L3node) right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT];
        L3node right_newnode = (L3node) right_newnode_shared[tasklet_id * MAX_L3_HEIGHT];

        max_height_shared[tasklet_id] = max_height;

        assert (max_height <= l3b_skip_list_root.height);

        if (length > 0) {
            int i = 0;
            L3_search(keys[i], 0, heights[i], predecessor, 0);
            for (int ht = 0; ht < heights[i]; ht++) {
                left_predecessor[ht] = (L3node) (right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht] = predecessor[ht]);
                left_newnode[ht] = (L3node) (right_newnode_shared[tasklet_id * MAX_L3_HEIGHT + ht] = newnode[i]);
            }
            max_height = heights[i];
            // print_nodes(heights[0], predecessor, true);
        }

        for (int i = 1; i < length; i++) {
            L3_search(keys[i], 0, heights[i], predecessor, 0);
            int minheight = (max_height < heights[i]) ? max_height : heights[i];
            for (int ht = 0; ht < minheight; ht++) {
                if (right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht] == predecessor[ht]) {
                    ((L3node)right_newnode_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht] = new pptr(DPU_ID, newnode[i]);
                    newnode[i].left[ht] = new pptr(DPU_ID, (L3node) right_newnode_shared[tasklet_id * MAX_L3_HEIGHT + ht]);
                } else {
                    ((L3node)right_newnode_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht] = new pptr(OLD_NODES_DPU_ID, (L3node) ((L3node) right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht].addr);
                    assert (((L3node) right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht].id != INVALID_DPU_ID);

                    newnode[i].left[ht] = new pptr(OLD_NODES_DPU_ID, predecessor[ht]);
                }
            }
            for (int ht = 0; ht < heights[i]; ht++) {
                right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht] = predecessor[ht];
                right_newnode_shared[tasklet_id * MAX_L3_HEIGHT + ht] = newnode[i];
            }
            if (heights[i] > max_height) {
                for (int ht = max_height; ht < heights[i]; ht++) {
                    left_predecessor[ht] = predecessor[ht];
                    left_newnode[ht] = newnode[i];
                }
                max_height = heights[i];
            }
        }


        int max_height_r = 0;
        for (int r = tasklet_id + 1; r < NR_TASKLETS; r++) {
            max_height_r = (max_height_shared[r] > max_height_r)
                    ? max_height_shared[r]
                    : max_height_r;
        }
        for (int ht = max_height_r; ht < max_height; ht++) {
            // right_newnode[ht]->right[ht] = right_predecessor[ht]->right[ht];
            if (((L3node)right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht].id != INVALID_DPU_ID) {
                ((L3node)right_newnode_shared[ht]).right[ht] = new pptr(OLD_NODES_DPU_ID, (L3node) ((L3node) right_predecessor_shared[tasklet_id * MAX_L3_HEIGHT + ht]).right[ht].addr);
            } else {
                ((L3node)right_newnode_shared[ht]).right[ht] = null;
            }
        }

        for (int ll = (int)tasklet_id - 1, ht = 0; ht < max_height; ht++) {
            while (ll >= 0 && ht >= max_height_shared[ll]) {
                ll--;
                assert (ll >= -1 && ll <= NR_TASKLETS);
            }
            if (ll >= 0 && ht < max_height_shared[ll]) {
                if (right_predecessor_shared[ll * MAX_L3_HEIGHT + ht] == left_predecessor[ht]) {
                    ((L3node)right_newnode_shared[ll * MAX_L3_HEIGHT + ht]).right[ht] = new pptr(DPU_ID, left_newnode[ht]);
                    left_newnode[ht].left[ht] = new pptr(DPU_ID, ((L3node)right_newnode_shared[ll * MAX_L3_HEIGHT + ht]));
                } else {
                    assert (((L3node)right_predecessor_shared[ll * MAX_L3_HEIGHT + ht]).right[ht].id != INVALID_DPU_ID);
                    ((L3node) right_newnode_shared[ll * MAX_L3_HEIGHT + ht]).right[ht] = new pptr(OLD_NODES_DPU_ID, (L3node) ((L3node) right_predecessor_shared[ll * MAX_L3_HEIGHT + ht]).right[ht].addr);

                    left_newnode[ht].left[ht] = new pptr(OLD_NODES_DPU_ID, left_predecessor[ht]);
                }
            }
            if (ll < 0) {
                left_newnode[ht].left[ht] = new pptr(OLD_NODES_DPU_ID, left_predecessor[ht]);

            }
        }


        for (int i = 0; i < length; i++) {
            for (int ht = 0; ht < heights[i]; ht++) {
                if (newnode[i].left[ht].id == OLD_NODES_DPU_ID) {
                    L3node ln = (L3node) newnode[i].left[ht].addr;
                    newnode[i].left[ht] = new pptr(DPU_ID,ln);
                    ln.right[ht] = new pptr(DPU_ID, newnode[i]);
                }
                if (newnode[i].right[ht].id == OLD_NODES_DPU_ID) {
                    L3node rn = (L3node) newnode[i].right[ht].addr;
                    newnode[i].right[ht] = new pptr(DPU_ID, rn);
                    rn.left[ht] = new pptr(DPU_ID, newnode[i]);
                }
            }
        }
    }


    public  int b_remove(Bnode nn, int len, List<Long> keys) {
        int tid = 0;

        Bnode bn;
        bn = nn;
        // m_read_single(nn, &bn, sizeof(Bnode));
        int nnlen = (int)bn.len;

        Object[] tmpbuf = (Object[]) push_variable_reply_head(tid);
        Object[] mpnnkeys = tmpbuf;
        // mppptr is a pptr*, size pptr = size long
        mppptr mpnnaddrs = (mppptr)(tmpbuf[nnlen]);
        mppptr mpnncaddrs = (mppptr)(tmpbuf[nnlen * 2]);

//
//        int l1 = data_block_to_mram(bn.keys, mpnnkeys);
//        int l2 = data_block_to_mram(bn.addrs, mpnnaddrs);
//        int l3 = data_block_to_mram(bn.caddrs, mpnncaddrs);

        for (int i = 0; i < nnlen; i ++) {
            long nnkey = (long) mpnnkeys[i];
            for (int k = 0; k < len; k ++) {
                if (keys.get(k) > nnkey) {
                    break;
                }
                if (keys.get(k) == nnkey) {
                    mpnnaddrs.pptrs[i] = (null);
                }
            }
        }

        int mpnnaddrsBegin = nnlen;
        int mpnncaddrsBegin = nnlen * 2;
        for (int i = 0; i < nnlen; i ++) {
            while (i < nnlen && !valid_pptr(tmpbuf[mpnnaddrsBegin + i])) {
                nnlen --;
                mpnnkeys[i] = mpnnkeys[nnlen];
                tmpbuf[mpnnaddrsBegin + i] = tmpbuf[mpnnaddrsBegin + nnlen];
                tmpbuf[mpnncaddrsBegin + i] = tmpbuf[mpnncaddrsBegin + nnlen];
            }
        }
        
        data_block_from_mram(bn.keys, tmpbuf, nnlen, 0);
        data_block_from_mram(bn.addrs, tmpbuf, nnlen, nnlen);
        data_block_from_mram(bn.caddrs, tmpbuf, nnlen, nnlen * 2);
        nn.len = nnlen;
        return 0;
    }



    private  boolean valid_pptr(Object o) {
        if(o instanceof pptr){
            if(((pptr) o).id < UPMEM.dpuInUse && ((pptr) o).addr != null){
                return true;
            }
        }
        return false;
    }

    @ModifiedSignature
    public  long b_scan(long bb, long ee, Bnode nn, Long[] keys, Long[] addrs, int keyOffset, int addrOffset) {
        int len = 0;
        long lkeys = Long.MIN_VALUE;
        Bnode bn;
        bn = nn;

        // m_read_single(nn, &bn, sizeof(Bnode));

        int nnlen = (int)bn.len;
        mdbptr keysdb = bn.keys, addrsdb = bn.addrs;
        mdbptr.data_block wram_keys = new mdbptr.data_block();
        mdbptr.data_block wram_addrs = new mdbptr.data_block();
        Object[] nnkeys = wram_keys.data;
        Object[] nnaddrs = wram_addrs.data;

        for (int i = 0; i < nnlen; i += DB_SIZE) {
            int curlen = Math.min(DB_SIZE, nnlen - i);
            keysdb.la = wram_keys.la;
            keysdb.data_blocks = wram_keys.data;
            addrsdb.la = wram_addrs.la;
            addrsdb.data_blocks = wram_addrs.data;

            for (int j = 0; j < curlen; j ++) {
                long nnkey = (long) nnkeys[j];
                if (nnkey >= bb && nnkey <= ee) {
                    keys[keyOffset + len] = nnkey;
                    addrs[addrOffset + len] = (Long) nnaddrs[j];
                    len++;
                } else if(nnkey < bb && nnkey >= lkeys){
                    keys[keyOffset + nnlen - 1] = nnkey;
                    addrs[addrOffset + nnlen - 1] = (Long) nnaddrs[j];
                    lkeys = nnkey;
                }
            }

            keysdb = wram_keys.la.nxt;
            addrsdb = wram_addrs.la.nxt;
        }

        if(lkeys != Long.MIN_VALUE) {
            lkeys = addrs[nnlen - 1];
            addrs[addrOffset + len] = addrs[0];
            addrs[addrOffset + 0] = lkeys;
            len++;
        }
        return len;
    }

    @ModifiedSignature
    public  Object L3_search(long key, int i, byte record_height, L3node[] rightmost, @Append int i1) {
        L3node tmp = l3_skip_list_root;
        long ht = l3_skip_list_root.height - 1;
        while (ht >= 0) {

            pptr r = tmp.right[(int) ht];
            if (r.id != INVALID_DPU_ID && ((L3node)r.addr).key <= key) {
                tmp = (L3node) r.addr;  // go right
                continue;
            }
            if (rightmost != null && ht < record_height) {
                rightmost[(int) ht] = tmp;
            }
            ht--;
        }
        // L3_IN_DPU_ASSERT(rightmost != NULL, "L3 search: rightmost error");
        // 这里出现了既获push reply又返回key的情况。(why?
        if (rightmost == null) {  // pure search task
            // TODO: reply
            return new L3_search_reply(tmp.down);
            //L3_search_reply tsr = (L3_search_reply){.addr = tmp->down};
            // push_fixed_reply(i, &tsr);
        }
        return tmp.key;
    }

    @ImportantCheck
    public  int nested_search(int len, List<Long> keys, List<Long> repkeys, List<pptr> addrs, List<Long> heights, Object[] paths, int siz, int offset_keys, int offset_repkeys, int offset_address, int offset_height, int offset_path) {
        int pathlen = 0;
        boolean complete = false;

        int L2_HEIGHT = 3;
        while (!complete) {
            complete = true;
            int l, r;
            for (l = 0; l < len; l = r) {
                for (r = l + 1; r < len; r++) {
                    if (PPTR_TO_I64(addrs.get(offset_address + r)) != PPTR_TO_I64(addrs.get(offset_address + l))) {
                        break;
                    }
                }
                pptr addr = addrs.get(offset_address + l);
                if (addr.id == DPU_ID && in_bbuffer((Bnode) addr.addr)) {
                    complete = false;
                    Bnode nn = (Bnode) addr.addr;
                    if (heights != null) {
                        b_search(nn, r - l, keys, repkeys, addrs,
                                heights, l, l, l, l);
                        for (int j = l; j < r; j++) {
                            if (heights.get(offset_height + j) > L2_HEIGHT) {  // RECORDING ADDRESSES
                                pptr ad = I64_TO_PPTR(heights.get(offset_height + j));
                                paths[pathlen++] =
                                        new offset_pptr(ad.addr, ad.id, j);
//                                        (offset_pptr){
//                                        .addr = ad.addr, .id = ad.id, .offset = j};
                            }
                            assert (
                                    pathlen <= siz &&
                                            (heights.get(offset_height + j) > L2_HEIGHT || heights.get(offset_height + j) == 1));
                            // IN_DPU_ASSERT_EXEC(
                            //     pathlen <= siz &&
                            //         (heights[j] > L2_HEIGHT || heights[j] == 1),
                            //     {
                            //         printf("ns! p=%d s=%d h=%lu\n", pathlen, siz,
                            //                heights[j]);
                            //         for (int x = 0; x < pathlen; x++) {
                            //             printf("p[%d]=%llx\n", x,
                            //                    PPTR_TO_I64(paths[x]));
                            //         }
                            //     });
                        }
                    } else {
                        b_search(nn, r - l, keys, repkeys, addrs, null, l, l, l, l);
                    }
                }
            }
        }
        return pathlen;
    }


    private  pptr I64_TO_PPTR(Long aLong) {
        return null;
    }

    public  long PPTR_TO_I64(pptr addr) {

        return 0;
    }


    public  Long p_get_height(Long key) {
        Object htv = ht_search(ht, key, (_v, _key)->{
            if (_v.v == null) {
                return -1;
            }
            Pnode addr = _v.v;
            if (addr.key == key) {
                return 1;
            }
            return 0;
        });
        if (htv == null) {
            return -1l;
        } else {
            ht_delete(ht, new int[]{ htcnt}, hash_to_addr(key, LX_HASHTABLE_SIZE), htv);
            //free_node(&free_list_pnode, (mpvoid)htv);
            Pnode nn = (Pnode) htv;
            return nn.height;
        }
    }

    private  void ht_delete(ht_slot[] ht, int[] cnt, int pos, Object val) {
        int ipos = pos;  // initial position
        ht_slot hs = ht[pos];
        while (hs.v != val) {  // find slot
            pos = (pos + 1) & (LX_HASHTABLE_SIZE - 1);
            hs = ht[pos];
            assert (pos != ipos);
        }
        ipos = pos;  // position to delete
        pos = (pos + 1) & (LX_HASHTABLE_SIZE - 1);

        while (true) {
            hs = ht[pos];
            if (hs.v == null) {
                ht[ipos] = null;
                break;
            } else if (ht_no_greater_than(hs.pos, ipos)) {
                ht[ipos] = hs;
                ipos = pos;
            } else {
            }
            pos = (pos + 1) & (LX_HASHTABLE_SIZE - 1);
            assert (pos != ipos);
        }
        //cnt[0]--;
        htcnt--;
    }

     boolean ht_no_greater_than(int a, int b) {  // a <= b with wrapping
        int delta = b - a;
        if (delta < 0) {
            delta += LX_HASHTABLE_SIZE;
        }
        return delta < (LX_HASHTABLE_SIZE >> 1);
    }

    public  void l3b_insert_parallel(int n, int l, int r) {
        int tid = 0;

        int NR_TASKLETS = 1;

        // bottom up
        for (int i = l; i < r; i++) {
            long key = mod_keys[i];
            pptr value = new pptr();

            L3Bnode nn = new L3Bnode();
            l3b_search(key, nn, value);
            mod_addrs[i] = ml3bptr_to_pptr(nn);

            // if (i > 0) {
            //     int64_t keyl = mod_keys[i - 1];
            //     // IN_DPU_ASSERT_EXEC(keyl < key, {
            //     //     printf("bip! eq %d %d %d %d %lld %lld\n", i, tid, l, r, key,
            //     //            keyl);
            //     //     for (int i = 0; i < n; i++) {
            //     //         printf("key[%d]=%lld\n", i, mod_keys[i]);
            //     //     }
            //     // });
            //     IN_DPU_ASSERT_EXEC(keyl < key, {});
            // }
        }

        l3ab_L3_n = n;
        int SERIAL_HEIGHT = 2;

        for (int ht = 0; ht <= l3ab_tree_root.height + 1; ht++) {
            if (ht < SERIAL_HEIGHT) {
                // distribute work
                n = l3ab_L3_n;
                // if (tid == 0) {
                //     printf("PARALLEL:%d\n", n);
                // }
                int lft = n * tid / NR_TASKLETS;
                int rt = n * (tid + 1) / NR_TASKLETS;
                if (rt > lft) {
                    if (lft != 0) {
                        lft = get_r(mod_addrs, n, lft - 1);
                    }
                    // IN_DPU_ASSERT(rt > 0, "bi! rt\n");
                    rt = get_r(mod_addrs, n, rt - 1);
                }

                L3_lfts[tid] = lft;
                L3_rts[tid] = rt;
                // execute
                l3b_insert_onelevel(n, tid, ht);

                // distribute work
                if (tid == 0) {
                    n = 0;
                    for (int i = 0; i < NR_TASKLETS; i++) {
                        for (int j = L3_lfts[i]; j < L3_rts[i]; j++) {
                            mod_keys[n] = mod_keys2[j];
                            mod_values[n] = mod_values2[j];
                            mod_addrs[n] = mod_addrs2[j];
                            // IN_DPU_ASSERT(mod_keys[n] != INT64_MIN, "bi! min\n");
                            // if (n > 0) {
                            //     int64_t key = mod_keys[n];
                            //     int64_t keyl = mod_keys[n - 1];
                            //     // IN_DPU_ASSERT_EXEC(key > keyl, {
                            //     //     printf("bip! rev %d %d %d %d %lld %lld\n", i, j, tid, n, key,
                            //     //         keyl);
                            //     //     for (int i = n - 10; i < n + 10; i++) {
                            //     //         printf("key[%d]=%lld\n", i, mod_keys[i]);
                            //     //     }
                            //     // });
                            //     // IN_DPU_ASSERT_EXEC(key > keyl, {});
                            //     IN_DPU_ASSERT(mod_keys[n] > mod_keys[n - 1],
                            //                   "bip! rev\n");
                            // }
                            n++;
                        }
                    }
                    l3ab_L3_n = n;
                    // printf("n=%d\n", n);
                }
            } else {
                if (tid == 0 && n > 0) {
                    // printf("SOLO:%d\n", n);
                    L3_lfts[0] = 0;
                    L3_rts[0] = n;
                    l3b_insert_onelevel(n, tid, ht);
                    n = L3_rts[0];
                    for (int i = 0; i < n; i++) {
                        mod_keys[i] = mod_keys2[i];
                        mod_values[i] = mod_values2[i];
                        mod_addrs[i] = mod_addrs2[i];
                    }
                } else {
                    break;
                }
            }
        }
    }

    private  int get_r(pptr[] addrs, int n, int l) {
        int r;
        pptr p1 = addrs[l];
        for (r = l; r < n; r++) {
            pptr p2 = addrs[r];
            if ((p1.addr == p2.addr)) {
                continue;
            } else {
                break;
            }
        }
        return r;
    }

    private  void l3b_insert_onelevel(int n, int tid, int ht) {
        L3Bnode bn = new L3Bnode();
        long[] nnkeys = new long[DB_SIZE];
        pptr[] nnvalues = new pptr[DB_SIZE];
        int lft = L3_lfts[tid];
        int rt = L3_rts[tid];
        int nxtlft = lft;
        int nxtrt = nxtlft;
        int siz = 0;

        int l, r;  // catch all inserts to the same node
        for (l = lft; l < rt; l = r) {
            r = get_r(mod_addrs, n, l);
            pptr addr = mod_addrs[l];
            L3Bnode nn = new L3Bnode();
            pptr up = new pptr(), right = new pptr();
            int nnsize;
            L3Bnode nn0 = new L3Bnode();
            if (valid_pptr(addr)) {
                nn0 = nn = pptr_to_ml3bptr(addr);
                nn.keys = bn.keys;
                nn.size = bn.size;
                nn.height = bn.height;
                nn.addrs = bn.addrs;
                nn.right = bn.right;
                nn.up = bn.up;

                up = bn.up;
                right = bn.right;
                nnsize = bn.size;
                for (int i = 0; i < nnsize; i++) {
                    nnkeys[i] = bn.keys[i];
                    nnvalues[i] = bn.addrs[i];
                }
            } else {
                nn = new L3Bnode(); //alloc_l3bn();
                up = null;
                right = null;
                nnsize = 1;
                nnkeys[0] = Long.MIN_VALUE;
                nnvalues[0] = ml3bptr_to_pptr(l3b_skip_list_root);
                if (ht > 0) {
                    l3b_skip_list_root.up = ml3bptr_to_pptr(nn);
                    for (int i = l; i < r; i++) {
                        pptr child_addr = (pptr) mod_values[i];
                        L3Bnode ch = pptr_to_ml3bptr(child_addr);
                        ch.up = ml3bptr_to_pptr(nn);
                    }
                }
                l3b_skip_list_root = nn;
            }
            l3b_node_init(bn, ht, up, right);
            int totsize = 0;

            {
                int nnl = 0;
                int i = l;
                while (i < r || nnl < nnsize) {
                    if (i < r && nnl < nnsize) {
                        if (mod_keys[i] == nnkeys[nnl]) {
                            i++;
                            totsize--;  // replace
                        } else if (mod_keys[i] < nnkeys[nnl]) {
                            i++;
                        } else {
                            nnl++;
                        }
                    } else if (i == r) {
                        nnl++;
                    } else {
                        i++;
                    }
                    totsize++;
                }
            }

            int nnl = 0;
            bn.size = 0;

            int l0 = l;
            for (int i = 0; nnl < nnsize || l < r; i++) {
                if (nnl < nnsize && (l == r || nnkeys[nnl] < mod_keys[l])) {
                    bn.keys[bn.size] = nnkeys[nnl];
                    bn.addrs[bn.size] = nnvalues[nnl];
                    nnl++;
                } else {
                    bn.keys[bn.size] = mod_keys[l];
                    bn.addrs[bn.size] = (pptr) mod_values[l];
                    if (nnl < nnsize && nnkeys[nnl] == mod_keys[l]) {  // replace
                        nnl++;
                    }
                    l++;
                }
                bn.size++;
                if (bn.size == 1 && (i > 0)) {  // newnode
                    mod_keys2[nxtrt] = bn.keys[0];
                    mod_values2[nxtrt] = ml3bptr_to_pptr(nn);
                    mod_addrs2[nxtrt] = bn.up;
                    nxtrt++;
                    // IN_DPU_ASSERT_EXEC(bn.keys[0] != INT64_MIN, {
                    //     printf("mod_keys2 = INT64_MIN\n");
                    //     // printf("l3bbuffer=%x l3bcnt=%d\n", l3bbuffer, l3bcnt);
                    //     // printf("ht=%d addr=%x\n", ht, nn0);
                    //     // printf("l=%d\tr=%d\tnnl=%d\n", l0, r, nnl);
                    //     // printf("i=%d\ttotsize=%d\n", i, totsize);
                    //     // for (int x = 0; x < nnsize; x++) {
                    //     //     printf("nn[%d]=%lld\n", x, nnkeys[x]);
                    //     // }
                    //     // // mram_read(nn0, &bn, sizeof(L3Bnode));
                    //     // m_read(nn0, &bn, sizeof(L3Bnode));
                    //     // printf("nn0size=%lld nnsize=%d\n", bn.size, nnsize);
                    //     // for (int x = 0; x < bn.size; x++) {
                    //     //     printf("nn0[%d]=%lld\n", x, bn.keys[x]);
                    //     // }
                    //     // for (int x = l0; x < r; x++) {
                    //     //     printf("mod[%d]=%lld\n", x, mod_keys[x]);
                    //     // }
                    //     // printf("i=%d\ttotsize=%d\n", i, totsize);
                    // });
                }
                if (bn.size == DB_SIZE ||
                        (i + HF_DB_SIZE + 1 == totsize && bn.size > HF_DB_SIZE)) {
                    for (int ii = 0; ii < bn.size; ii++) {
                        if (bn.height > 0) {
                            assert (valid_pptr(bn.addrs[ii]));
                            L3Bnode ch = pptr_to_ml3bptr(bn.addrs[ii]);
                            ch.up = ml3bptr_to_pptr(nn);
                        }
                    }
                    if (nnl == nnsize && l == r) {
                        nn.up = bn.up;
                        nn.right = bn.right;
                        nn.keys = bn.keys;
                        nn.size = bn.size;
                        nn.height = bn.height;
                        nn.addrs = bn.addrs;
                        //m_write(&bn, nn, sizeof(L3Bnode));
                    } else {
                        up = bn.up;
                        right = bn.right;
                        ht = bn.height;

                        L3Bnode nxt_nn = new L3Bnode();//alloc_l3bn();
                        bn.right = ml3bptr_to_pptr(nxt_nn);
                        nn.up = bn.up;
                        nn.right = bn.right;
                        nn.keys = bn.keys;
                        nn.size = bn.size;
                        nn.height = bn.height;
                        nn.addrs = bn.addrs;
                        //m_write(&bn, nn, sizeof(L3Bnode));

                        l3b_node_init(bn, ht, up, right);
                        nn = nxt_nn;
                    }
                }
                // if (nnl == nnsize && l == r) {
                //     IN_DPU_ASSERT_EXEC(i + 1 == totsize, {
                //         printf("i+1 != totsize\n");
                //         // printf("l3bbuffer=%x l3bcnt=%d\n", l3bbuffer, l3bcnt);
                //         // printf("ht=%d addr=%x\n", ht, nn0);
                //         // printf("l=%d\tr=%d\tnnl=%d\n", l0, r, nnl);
                //         // printf("i=%d\ttotsize=%d\n", i, totsize);
                //         // for (int x = 0; x < nnsize; x++) {
                //         //     printf("nn[%d]=%lld\n", x, nnkeys[x]);
                //         // }
                //         // // mram_read(nn0, &bn, sizeof(L3Bnode));
                //         // m_read(nn0, &bn, sizeof(L3Bnode));
                //         // printf("nn0size=%lld nnsize=%d\n", bn.size, nnsize);
                //         // for (int x = 0; x < bn.size; x++) {
                //         //     printf("nn0[%d]=%lld\n", x, bn.keys[x]);
                //         // }
                //         // for (int x = l0; x < r; x++) {
                //         //     printf("mod[%d]=%lld\n", x, mod_keys[x]);
                //         // }
                //         // printf("i=%d\ttotsize=%d\n", i, totsize);
                //     });
                // }
            }
            if (bn.size != 0) {
                for (int i = 0; i < bn.size; i++) {
                    if (bn.height > 0) {
                        // IN_DPU_ASSERT(valid_pptr(bn.addrs[i]), "bio! inv2\n");
                        L3Bnode ch = pptr_to_ml3bptr(bn.addrs[i]);
                        ch.up = ml3bptr_to_pptr(nn);
                    }
                }
                nn.up = bn.up;
                nn.right = bn.right;
                nn.keys = bn.keys;
                nn.size = bn.size;
                nn.height = bn.height;
                nn.addrs = bn.addrs;
                //m_write(&bn, nn, sizeof(L3Bnode));
            }
            // IN_DPU_ASSERT_EXEC(l == r && nnl == nnsize, {
            //     printf("l=%d\tr=%d\tnnl=%d\tnnsize=%d\n", l, r, nnl, nnsize);
            // });
        }
        L3_lfts[tid] = nxtlft;
        L3_rts[tid] = nxtrt;
    }


    public  int b_search_with_path_task_siz(int len) {
        return ((len + 1) << 1);
    }

    public  void L3_remove_parallel(int length, L3node[] nodes, byte[] maxHeightShared, Object[] leftNodeShared) {
        int tasklet_id = 0;
        // mL3ptr *nodes = mem_alloc(sizeof(mL3ptr) * length);
        byte[] heights = new byte[length];
        byte max_height = 0;
        for (int i = 0; i < length; i ++) {
            heights[i] = (byte) nodes[i].height;
        }
        int left_node_offset = tasklet_id * MAX_L3_HEIGHT;

        max_height = 0;
        for (int i = 0; i < length; i++) {
            int min_height = (heights[i] < max_height) ? heights[i] : max_height;
            for (int ht = 0; ht < min_height; ht++) {
                L3node ln = (L3node)(nodes[i].left[ht].addr);
                ln.right[ht] = nodes[i].right[ht];
                if (nodes[i].right[ht].id != INVALID_DPU_ID) {
                    L3node rn = (L3node)(nodes[i].right[ht].addr);
                    rn.left[ht] = nodes[i].left[ht];
                }
                nodes[i].left[ht] = nodes[i].right[ht] = null;
            }
            if (heights[i] > max_height) {
                for (int ht = max_height; ht < heights[i]; ht++) {
                    leftNodeShared[left_node_offset + ht] = nodes[i];
                }
                max_height = heights[i];
            }
        }

        max_height_shared[tasklet_id] = max_height;

        int NR_TASKLETS = 1;

        for (int l = (int)tasklet_id - 1, ht = 0; ht < max_height; ht++) {
            while (l >= 0 && ht >= max_height_shared[l]) {
                l--;
            }
            int left_node_l_offset = l * MAX_L3_HEIGHT;
            if (l < 0 || (((L3node)leftNodeShared[left_node_offset + ht]).left[ht].addr !=
                    leftNodeShared[left_node_l_offset + ht])) {  // left most node in the level
                int r = tasklet_id + 1;
                L3node rn = (L3node) leftNodeShared[left_node_offset + ht];
                for (; r < NR_TASKLETS; r++) {
                    if (max_height_shared[r] <= ht) {
                        continue;
                    }
                    //L3node *left_node_r = left_node_shared + r * MAX_L3_HEIGHT;
                    int left_node_r_offset =  r * MAX_L3_HEIGHT;
                    if (rn.right[ht].id == INVALID_DPU_ID ||
                            (L3node)rn.right[ht].addr != leftNodeShared[left_node_r_offset + ht]) {
                        break;
                    }
                    rn = (L3node) leftNodeShared[left_node_r_offset + ht];
                }
                assert(((L3node)leftNodeShared[left_node_offset + ht]).left[ht].id == DPU_ID);
                L3node ln = ((L3node)((L3node)(leftNodeShared[left_node_offset + ht])).left[ht].addr);
                ln.right[ht] = rn.right[ht];
                if (rn.right[ht].id != INVALID_DPU_ID) {
                    rn = (L3node) rn.right[ht].addr;
                    rn.left[ht] = ((L3node)leftNodeShared[left_node_offset + ht]).left[ht];
                }
            } else {  // not the left most node
                assert (
                        (((L3node)leftNodeShared[left_node_l_offset + ht]).right[ht].addr == leftNodeShared[left_node_offset + ht]));
                // do nothing
            }
        }
    }

    public  void l3b_remove_parallel(int n, int l, int r) {
        int tid = 0;

        // bottom up
        for (int i = l; i < r; i++) {
            long key = mod_keys[i];
            pptr value = new pptr();
            L3Bnode nn = new L3Bnode();
            l3b_search(key, nn, value);
            mod_addrs[i] = ml3bptr_to_pptr(nn);
            mod_type[i] = remove_type;
        }

        l3ab_L3_n = n;
        int NR_TASKLETS = 1;
        int SERIAL_HEIGHT = 2;

        for (int ht = 0; ht <= l3_skip_list_root.height; ht++) {
            if (ht < SERIAL_HEIGHT) {
                // distribute work
                n = l3ab_L3_n;
                int lft = n * tid / NR_TASKLETS;
                int rt = n * (tid + 1) / NR_TASKLETS;
                if (rt > lft) {
                    if (lft != 0) {
                        lft = get_r(mod_addrs, n, lft - 1);
                    }
                    // IN_DPU_ASSERT(rt > 0, "br! rt\n");
                    rt = get_r(mod_addrs, n, rt - 1);
                }

                L3_lfts[tid] = lft;
                L3_rts[tid] = rt;

                // execute
                l3b_remove_onelevel(n, tid, ht);

                // distribute work
                if (tid == 0) {
                    n = 0;
                    for (int i = 0; i < NR_TASKLETS; i++) {
                        for (int j = L3_lfts[i]; j < L3_rts[i]; j++) {
                            mod_keys[n] = mod_keys2[j];
                            mod_values[n] = mod_values2[j];
                            mod_addrs[n] = mod_addrs2[j];
                            mod_type[n] = mod_type2[j];
                            n++;
                        }
                    }
                    l3b_remove_serial_compact(n, ht);
                    n = L3_rts[0];
                    for (int i = 0; i < n; i++) {
                        mod_keys[i] = mod_keys2[i];
                        mod_values[i] = mod_values2[i];
                        mod_addrs[i] = mod_addrs2[i];
                        mod_type[i] = mod_type2[i];
                    }
                    l3ab_L3_n = n;
                }
            } else {
                if (tid == 0 && n > 0) {
                    // printf("SOLO:%d\n", n);
                    L3_lfts[0] = 0;
                    L3_rts[0] = n;
                    l3b_remove_onelevel(n, tid, ht);
                    n = L3_rts[0];
                    for (int i = 0; i < n; i++) {
                        mod_keys[i] = mod_keys2[i];
                        mod_values[i] = mod_values2[i];
                        mod_addrs[i] = mod_addrs2[i];
                        mod_type[i] = mod_type2[i];
                    }
                    l3b_remove_serial_compact(n, ht);
                    n = L3_rts[0];
                    for (int i = 0; i < n; i++) {
                        mod_keys[i] = mod_keys2[i];
                        mod_values[i] = mod_values2[i];
                        mod_addrs[i] = mod_addrs2[i];
                        mod_type[i] = mod_type2[i];
                    }
                } else {
                    break;
                }
            }
        }
    }

    private  void l3b_remove_onelevel(int n, int tid, int ht) {
        L3Bnode bn = new L3Bnode();
        long[] nnkeys = new long[DB_SIZE];
        pptr[] nnvalues = new pptr[DB_SIZE];
        int lft = L3_lfts[tid];
        int rt = L3_rts[tid];
        int nxtlft = lft;
        int nxtrt = nxtlft;
        int siz = 0;

        int l, r;  // catch all inserts to the same node
        for (l = lft; l < rt; l = r) {
            r = get_r(mod_addrs, n, l);

            pptr addr = mod_addrs[l];
            // IN_DPU_ASSERT(valid_pptr(addr), "bro! inva\n");

            L3Bnode nn = pptr_to_ml3bptr(addr);
            bn.size = nn.size;
            bn.addrs = nn.addrs;
            bn.keys = nn.keys;
            bn.height = nn.height;
            bn.right = nn.right;
            bn.up = nn.up;
            pptr up = bn.up, right = bn.right;
            int nnsize = bn.size;
            for (int i = 0; i < nnsize; i++) {
                nnkeys[i] = bn.keys[i];
                nnvalues[i] = bn.addrs[i];
            }

            l3b_node_init(bn, ht, up, right);
            int totsize = 0;

            {
                int nnl = 0;
                int i = l;
                while (nnl < nnsize) {
                    if (i == r || nnkeys[nnl] < mod_keys[i]) {
                        bn.keys[bn.size] = nnkeys[nnl];
                        bn.addrs[bn.size] = nnvalues[nnl];
                        bn.size++;
                        nnl++;
                    } else if (nnkeys[nnl] > mod_keys[i]) {
                        i++;
                    } else {  // equal
                        if (mod_type[i] == change_key_type) {
                            bn.keys[bn.size] = pptr_to_int64((pptr) mod_values[i]);
                            bn.addrs[bn.size] = nnvalues[nnl];
                            bn.size++;
                        } else if (mod_type[i] == remove_type) {
                            L3Bnode removed_child = (L3Bnode) nnvalues[nnl].addr;
//                            if (in_l3bbuffer(removed_child)) {
//                                /.free_node(free_list_l3bnode, (mpvoid)removed_child);
//                            }
                        }
                        nnl++;
                    }
                }
            }
            nn.size = bn.size;
            nn.addrs = bn.addrs;
            nn.keys = bn.keys;
            nn.height = bn.height;
            nn.right = bn.right;
            nn.up = bn.up;
            //m_write(&bn, nn, sizeof(L3Bnode));

            int future_modif = 0;
                if (bn.size < HF_DB_SIZE) {
                future_modif |= underflow_type; // underflow, requires merge / rotate
            }
            if (bn.size == 0 || nnkeys[0] != bn.keys[0]) {
                future_modif |= change_key_type; // pivot key changed
            }
            if (nn == l3b_skip_list_root) {
                future_modif = 0; // shouldn't remove root
            }
            if (future_modif > 0) {
                mod_keys2[nxtrt] = nnkeys[0];
                mod_addrs2[nxtrt] = addr;
                mod_values2[nxtrt] = int64_to_pptr(bn.keys[0]);
                mod_type2[nxtrt] = future_modif;
                nxtrt++;
            }
        }
        L3_lfts[tid] = nxtlft;
        L3_rts[tid] = nxtrt;
    }

    private  void l3b_remove_serial_compact(int n, int ht) {
        L3Bnode bn = new L3Bnode();
        long[] nnkeys = scnnkeys;
        pptr[] nnaddrs = scnnaddrs;

        int nxt_n = 0;
        for (int l = 0; l < n;) {
            int mt = (int) mod_type[l];
            if (mt == change_key_type) {
                mod_keys2[nxt_n] = mod_keys[l];
                mod_values2[nxt_n] = (pptr) mod_values[l];
                L3Bnode nn = pptr_to_ml3bptr(mod_addrs[l]);
                mod_addrs2[nxt_n] = nn.up;
                mod_type2[nxt_n] = change_key_type;
                nxt_n++;
                l++;
            } else if ((mt & underflow_type) != 0) {
                int r = l;
                pptr addr = mod_addrs[l];
                int nnl = 0;
                while (nnl < HF_DB_SIZE) {
                    L3Bnode nn = pptr_to_ml3bptr(addr);
                    bn.up = nn.up;
                    bn.size = nn.size;
                    bn.addrs = nn.addrs;
                    bn.keys = nn.keys;
                    bn.right = nn.right;
                    bn.height = nn.height;

                    for (int i = 0; i < bn.size; i++) {
                        nnkeys[nnl] = bn.keys[i];
                        nnaddrs[nnl] = bn.addrs[i];
                        nnl++;
                    }
                    if (r < n && (addr.addr == mod_addrs[r].addr)) {
                        r++;
                    } else {
                        // IN_DPU_ASSERT(bn.size >= HF_DB_SIZE ||
                        //        equal_pptr(bn.right, null_pptr), "brsc! mr\n");
                    }
                    if (nnl < HF_DB_SIZE && !(bn.right.addr == null)) {
                        addr = bn.right;
                    } else {
                        break;
                    }
                }

                L3Bnode left_nn = pptr_to_ml3bptr(mod_addrs[l]);
                pptr right_out = bn.right;

                // prepare for upper level
                if (nnl > 0) {
                    if (nnkeys[0] != mod_keys[l]) {  // change key
                        mod_keys2[nxt_n] = mod_keys[l];
                        mod_values2[nxt_n] = int64_to_pptr(nnkeys[0]);
                        mod_addrs2[nxt_n] = left_nn.up;
                        mod_type2[nxt_n] = change_key_type;
                        nxt_n++;
                    }
                    left_nn.right = right_out;
                } else {
                    mod_keys2[nxt_n] = mod_keys[l];
                    mod_values2[nxt_n] = null;
                    mod_addrs2[nxt_n] = left_nn.up;
                    mod_type2[nxt_n] = remove_type;
                    nxt_n++;
                }

                int mid = nnl >> 1;
                long mid_key = nnkeys[mid];

                if (nnl > 0 && nnl <= DB_SIZE) {
                    L3Bnode nn = pptr_to_ml3bptr(mod_addrs[l]);
                    l3b_node_fill(nn, bn, nnl, nnkeys, nnaddrs, 0, 0);
                    boolean addr_covered = (addr.addr == mod_addrs[l].addr);
                    for (int i = l + 1; i < r; i++) {
                        if ((addr.addr == mod_addrs[i].addr)) {
                            addr_covered = true;
                        }
                        nn = pptr_to_ml3bptr(mod_addrs[i]);
                        mod_keys2[nxt_n] = mod_keys[i];
                        mod_values2[nxt_n] = null;
                        mod_addrs2[nxt_n] = nn.up;
                        mod_type2[nxt_n] = remove_type;
                        nxt_n++;
                    }
                    if (addr_covered == false) {
                        nn = pptr_to_ml3bptr(addr);
                        bn.height = nn.height;
                        bn.keys = nn.keys;
                        bn.size = nn.size;
                        bn.right = nn.right;
                        bn.up = nn.up;
                        bn.addrs = nn.addrs;
                        //m_read(nn, &bn, sizeof(L3Bnode));
                        // IN_DPU_ASSERT((bn.size >= HF_DB_SIZE ||
                        //        equal_pptr(bn.right, null_pptr)), "brsc! mr\n");
                        mod_keys2[nxt_n] = bn.keys[0];
                        mod_values2[nxt_n] = null;
                        mod_addrs2[nxt_n] = bn.up;
                        mod_type2[nxt_n] = remove_type;
                        nxt_n++;
                    }
                } else if (nnl > DB_SIZE) {
                    boolean addr_covered = false;
                    L3Bnode nn = pptr_to_ml3bptr(mod_addrs[l]);
                    l3b_node_fill(nn, bn, mid, nnkeys, nnaddrs, 0, 0);
                    int filpos = l;
                    {
                        for (int i = l; i < r; i++) {
                            if ((addr.addr == mod_addrs[i].addr)) {
                                addr_covered = true;
                            }
                            if (mod_keys[i] <= mid_key) {
                                filpos = i;
                            }
                        }
                        if (!addr_covered) {
                            nn = pptr_to_ml3bptr(addr);
                            // IN_DPU_ASSERT(nn->size >= HF_DB_SIZE ||
                            //    equal_pptr(nn->right, null_pptr), "brsc! mr\n");
                            if (nn.keys[0] <= mid_key) {
                                filpos = -1;
                            }
                        }
                    }
                    // IN_DPU_ASSERT(filpos != l, "fil=l\n");

                    for (int i = l + 1; i < r; i++) {
                        nn = pptr_to_ml3bptr(mod_addrs[i]);
                        if (i != filpos) {
                            mod_keys2[nxt_n] = mod_keys[i];
                            mod_addrs2[nxt_n] = nn.up;
                            mod_type2[nxt_n] = remove_type;
                            nxt_n++;
                        } else {
                            int offset_mid = mid;
                            l3b_node_fill(nn, bn, nnl - mid, nnkeys,
                                    nnaddrs, offset_mid, offset_mid);
                            left_nn.right = mod_addrs[i];
                            nn.right = right_out;

                            mod_keys2[nxt_n] = mod_keys[i];
                            mod_values2[nxt_n] = int64_to_pptr(mid_key);
                            mod_addrs2[nxt_n] = nn.up;
                            mod_type2[nxt_n] = change_key_type;
                            nxt_n++;
                        }
                    }
                    if (!addr_covered) {
                        nn = pptr_to_ml3bptr(addr);
                        // IN_DPU_ASSERT(nn->size >= HF_DB_SIZE ||
                        //        equal_pptr(nn->right, null_pptr), "brsc! mr\n");
                        if (filpos != -1) {
                            mod_keys2[nxt_n] = nn.keys[0];
                            mod_addrs2[nxt_n] = nn.up;
                            mod_type2[nxt_n] = remove_type;
                            nxt_n++;
                        } else {
                            left_nn.right = addr;
                            nn.right = right_out;

                            mod_keys2[nxt_n] = nn.keys[0];
                            mod_values2[nxt_n] = int64_to_pptr(mid_key);
                            mod_addrs2[nxt_n] = nn.up;
                            mod_type2[nxt_n] = change_key_type;
                            nxt_n++;
                            l3b_node_fill(nn, bn, nnl - mid, nnkeys,
                                    nnaddrs, mid, mid);
                        }
                    }
                } else {
                    // pass
                }

                l = r;
            } else {
                // IN_DPU_ASSERT(false, "mt\n");
            }
        }
        L3_lfts[0] = 0;
        L3_rts[0] = nxt_n;

    }

    private  void l3b_node_fill(L3Bnode nn, L3Bnode bn, int size, long[] keys, pptr[] addrs, int offsetkeys, int offsetaddrs) {
        //m_read(nn, bn, sizeof(L3Bnode));
        bn.addrs = nn.addrs;
        bn.size = nn.size;
        bn.keys = nn.keys;
        bn.up = nn.up;
        bn.height = nn.height;
        bn.right = nn.right;

        for (int i = 0; i < size; i++) {
            bn.keys[offsetkeys + i] = keys[i];
            bn.addrs[offsetaddrs + i] = addrs[i];
        }
        for (int i = size; i < DB_SIZE; i++) {
            bn.keys[offsetkeys + i] = Integer.MIN_VALUE;
            bn.addrs[offsetaddrs + i] = null;
        }
        bn.right = nn.right;
        bn.addrs = nn.addrs;
        bn.up = nn.up;
        bn.height = nn.height;
        bn.size = nn.size;
        bn.keys = nn.keys;

        //m_write(bn, nn, sizeof(L3Bnode));
        if (bn.height > 0) {
            for (int i = 0; i < size; i ++) {
                L3Bnode ch = pptr_to_ml3bptr(bn.addrs[i]);
                ch.up = ml3bptr_to_pptr(nn);
            }
        }
    }


    public  int L3_scan_search(long begin, long end, Object addrbuf) {
        L3node tmp = l3_skip_list_root;
        long ht = l3_skip_list_root.height - 1;
        while (ht >= 0) {
            pptr r = tmp.right[(int) ht];
            if (r.id != INVALID_DPU_ID && ((L3node)r.addr).key <= begin) {
                tmp = (L3node) r.addr;  // go right
                continue;
            }
            ht--;
        }

        varlen_buffer_reset_dpu((varlen_buffer_dpu) addrbuf);
        pptr l3_down = tmp.down;
        L3node tmp2 = tmp;
        int num = 1;
        ht = 1;
        varlen_buffer_push_dpu((varlen_buffer_dpu) addrbuf, PPTR_TO_I64(l3_down));

        while(ht == 1) {
            pptr r = tmp2.right[0];
            if(r.id != INVALID_DPU_ID && ((L3node)r.addr).key <= end
            // && addrbuf->len <= 1024
        ) {
                tmp2 = (L3node) r.addr;  // go right
                num++;
                l3_down = tmp2.down;
                varlen_buffer_push_dpu((varlen_buffer_dpu) addrbuf, PPTR_TO_I64(l3_down));
                continue;
            }
            ht = 0;
        }
        return num;
    }


    public  mdbptr data_block_from_mram(mdbptr db, Object[] bufkeys, int len, int len2) {
        // IN_DPU_ASSERT(in_dbbuffer(db), "dbfb! inv\n");
        mdbptr ret = db;
        mdbptr.data_block tmp = new mdbptr.data_block();
        db.la = new len_addr(0, mdbptr.InvalidPtr());
        for (int inslen = 0; inslen <= len; inslen += DB_SIZE) {
            int curlen = Math.min(DB_SIZE, len - inslen);
            if (curlen > 0) {
                for(int pos = 0; pos < curlen; pos ++){
                    tmp.data[pos] = bufkeys[inslen + pos];
                }
                // tmp.data = bufkeys[inslen]
                // m_read_single(bufkeys + inslen, tmp.data, S64(curlen));
            }
            tmp.la = db.la;
            tmp.la.len = len - inslen;
            if (tmp.la.len >= DB_SIZE && tmp.la.nxt == mdbptr.InvalidPtr()) {
                tmp.la.nxt = data_block_allocate();
            }
            tmp.data = db.data_blocks;
            tmp.la = db.la;
            //m_write_single(&tmp, db, sizeof(data_block));
            db = tmp.la.nxt;
        }
        remove_data_blocks(tmp.la.nxt);
        return ret;
    }

    public  mdbptr data_block_allocate() {
        return alloc_db();
    }

    private  mdbptr alloc_db() {
        return null;
    }

    public  int b_filter_cache_mram(Bnode nn, long l, long r, Object[] keys, Object[] addrs, Object[] caddrs, int offset_addrs, int offset_caddrs) {
        Bnode bn = nn;
        //m_read_single(nn, &bn, sizeof(Bnode));
        int nnlen = (int)bn.len;
        mdbptr keysdb = bn.keys, addrsdb = bn.addrs, caddrsdb = bn.caddrs;

        mdbptr.data_block wram_keys = new mdbptr.data_block();
        mdbptr.data_block wram_addrs = new mdbptr.data_block();
        mdbptr.data_block wram_caddrs = new mdbptr.data_block();
        Object[] nnkeys = wram_keys.data;
        Object[] nnaddrs = wram_addrs.data;
        Object[] nncaddrs = wram_caddrs.data;


        int len = 0;
        for (int i = 0; i < nnlen; i += DB_SIZE) {
            int curlen = Math.min(DB_SIZE, nnlen - i);



            // TODO:
            wram_keys.la = keysdb.la;
            wram_keys.data = keysdb.data_blocks;
            wram_addrs.la = addrsdb.la;
            wram_addrs.data = addrsdb.data_blocks;
            wram_caddrs.la = caddrsdb.la;
            wram_caddrs.data = caddrsdb.data_blocks;

//            m_read_single(keysdb, &wram_keys, sizeof(data_block));
//            m_read_single(addrsdb, &wram_addrs, sizeof(data_block));
//            m_read_single(caddrsdb, &wram_caddrs, sizeof(data_block));

            for (int j = 0; j < curlen; j ++) {
                long nnkey = (long) nnkeys[j];
                // IN_DPU_ASSERT(valid_pptr(I64_TO_PPTR(nnaddrs[j]), NR_DPUS),
                //               "bf! inv addr!\n");
                if (nnkey >= l && nnkey <= r) {
                    keys[len] = nnkey;
                    addrs[len] = nnaddrs[j];
                    caddrs[len] = nncaddrs[j];
                    len++;
                } else {
                }
            }

            keysdb = wram_keys.la.nxt;
            addrsdb = wram_addrs.la.nxt;
            caddrsdb = wram_caddrs.la.nxt;
        }

        return len;
    }

    public  int b_filter_cache(Bnode nn, long l, long r, long[] keys,
                                     long[] addrs,
                                     long[] caddrs) {
        // l <= x <= r
        // IN_DPU_ASSERT(b_length_check(nn), "bf! len\n");

        Bnode bn = nn;
        //m_read_single(nn, &bn, sizeof(Bnode));
        int nnlen = (int)bn.len;
        mdbptr keysdb = bn.keys, addrsdb = bn.addrs, caddrsdb = bn.caddrs;

        mdbptr.data_block wram_keys = new mdbptr.data_block(), wram_addrs = new mdbptr.data_block(), wram_caddrs = new mdbptr.data_block();
        Object[] nnkeys = wram_keys.data;
        Object[] nnaddrs = wram_addrs.data;
        Object[] nncaddrs = wram_caddrs.data;

        int len = 0;
        for (int i = 0; i < nnlen; i += DB_SIZE) {
            int curlen = Math.min(DB_SIZE, nnlen - i);
            // IN_DPU_ASSERT(in_dbbuffer(keysdb) && in_dbbuffer(addrsdb) &&
            //                   in_dbbuffer(caddrsdb),
            //               "bf! inv2\n");

            // IN_DPU_ASSERT_EXEC(
            //     in_dbbuffer(keysdb) && in_dbbuffer(addrsdb) &&
            //         in_dbbuffer(caddrsdb),
            //     {
            //         printf("i=%d nnlen=%d keys=%x addrs=%x caddrs=%x\n", i,
            //         nnlen,
            //                (uint32_t)keysdb, (uint32_t)addrsdb,
            //                (uint32_t)caddrsdb);
            //         return false;
            //     });

            wram_keys.la = keysdb.la;
            wram_keys.data = keysdb.data_blocks;
            wram_addrs.la = addrsdb.la;
            wram_addrs.data = addrsdb.data_blocks;
            wram_addrs.la = caddrsdb.la;
            wram_addrs.data = caddrsdb.data_blocks;

            for (int j = 0; j < curlen; j++) {
                long nnkey = (long) nnkeys[j];
                // IN_DPU_ASSERT(valid_pptr(I64_TO_PPTR(nnaddrs[j]), NR_DPUS),
                //               "bf! inv addr!\n");
                if (nnkey >= l && nnkey <= r) {
                    keys[len] = nnkey;
                    addrs[len] = (long) nnaddrs[j];
                    caddrs[len] = (long) nncaddrs[j];
                    len++;
                }
            }
            keysdb = wram_keys.la.nxt;
            addrsdb = wram_addrs.la.nxt;
            caddrsdb = wram_caddrs.la.nxt;
        }
        return len;
    }


    @ImportantCheck
    public  Object push_variable_reply_head(int taskletId) {
//        int tasklet_id = 0;
//        return send_varlen_buffer[tasklet_id] + send_varlen_task_size[tasklet_id];

        return null;
    }
    @ImportantCheck @ReplyRelative
    public  void l3b_scan(long lkey, long rkey, varlen_buffer_dpu addr_buf, varlen_buffer_dpu up_buf, varlen_buffer_dpu down_buf) {
        L3Bnode tmp;
        L3Bnode bn;

        varlen_buffer_dpu tmp_buf = new varlen_buffer_dpu();
        varlen_buffer_reset_dpu(addr_buf);
        varlen_buffer_reset_dpu(up_buf);
        varlen_buffer_reset_dpu(down_buf);
        pptr cur_addr = ml3bptr_to_pptr(l3b_skip_list_root);
        varlen_buffer_push_dpu(up_buf, pptr_to_int64(cur_addr));
        boolean flag;
        long tmp_max_value, vee = Long.MIN_VALUE, tmp_key;
        int tmp_max_idx = 0, jbb = -1, jee = -1;
        while (up_buf.len > 0) {
            for(long j = 0; j < up_buf.len; j++) {
                tmp_key = (long) varlen_buffer_element_dpu(up_buf, (int) j);
                cur_addr = int64_to_pptr(tmp_key);
                tmp = pptr_to_ml3bptr(cur_addr);
                bn = tmp;
                //m_read(tmp, &bn, sizeof(L3Bnode));
                flag = false;
                tmp_max_value = Long.MAX_VALUE;
                for (int i = 0; i < bn.size; i++) {
                    if(bn.height > 0) {
                        if(bn.keys[i] <= lkey) {
                            flag = true;
                            if(bn.keys[i] >= tmp_max_value) {
                                tmp_max_value = bn.keys[i];
                                tmp_max_idx = i;
                            }
                        }
                        else if(bn.keys[i] <= rkey) {
                            tmp_key = pptr_to_int64(bn.addrs[i]);
                            varlen_buffer_push_dpu(down_buf, tmp_key);
                        }

                    }
                    else {
                        if(bn.keys[i] <= lkey) {
                            flag = true;
                            if(bn.keys[i] >= tmp_max_value) {
                                tmp_max_value = bn.keys[i];
                                tmp_max_idx = i;
                            }
                        }
                        else if (bn.keys[i] <= rkey) {
                            if(bn.keys[i] >= vee) {
                                vee = bn.keys[i];
                                jee = addr_buf.len;
                            }
                            tmp_key = pptr_to_int64(bn.addrs[i]);
                            varlen_buffer_push_dpu(addr_buf, tmp_key);
                        }
                    }
                }
                if(flag) {
                    if(bn.height > 0){
                        tmp_key = pptr_to_int64(bn.addrs[tmp_max_idx]);
                        varlen_buffer_push_dpu(down_buf, tmp_key);
                    }
                    else {
                        jbb = addr_buf.len;
                        tmp_key = pptr_to_int64(bn.addrs[tmp_max_idx]);
                        varlen_buffer_push_dpu(addr_buf, tmp_key);
                    }
                }
            }
            tmp_buf = up_buf;
            up_buf = down_buf;
            down_buf = tmp_buf;
            varlen_buffer_reset_dpu(down_buf);
        }
        if(jbb >= 0 && jbb < addr_buf.len) {
            tmp_max_value = (long) varlen_buffer_element_dpu(addr_buf, 0);
            vee = (long) varlen_buffer_element_dpu(addr_buf, jbb);
            varlen_buffer_set_element_dpu(addr_buf, 0, vee);
            varlen_buffer_set_element_dpu(addr_buf, jbb, tmp_max_value);
        }
        if(jee >= 0 && jee < addr_buf.len) {
            tmp_max_value = (long) varlen_buffer_element_dpu(addr_buf, addr_buf.len - 1);
            vee = (long) varlen_buffer_element_dpu(addr_buf, jee);
            varlen_buffer_set_element_dpu(addr_buf, addr_buf.len - 1, vee);
            varlen_buffer_set_element_dpu(addr_buf, jee, tmp_max_value);
        }
    }

    private  pptr ml3bptr_to_pptr(L3Bnode addr) {
        return new pptr (DPU_ID, addr); //pptr){.id = DPU_ID, .addr = (uint32_t)addr};
    }

    private  pptr int64_to_pptr(long tmpKey) {

        return null;
    }

    private  long pptr_to_int64(pptr addr) {
        /*
         *     int64_t *i64p = (int64_t *)(&x);
         *     return *i64p;
         *
         *
         * */
        byte[] data = new byte[8];
        // id
        data[0] = (byte) ((addr.id >> 8) & 0xFF);
        data[1] = (byte) ((addr.id) & 0xFF);
        data[2] = (byte) ((addr.offset >> 8) & 0xFF);
        data[3] = (byte) ((addr.offset) & 0xFF);
        data[4] = (byte) ((addr.hashCode() >> 24) & 0xFF);
        data[5] = (byte) ((addr.hashCode() >> 16) & 0xFF);
        data[6] = (byte) ((addr.hashCode() >> 8) & 0xFF);
        data[7] = (byte) ((addr.hashCode()) & 0xFF);
        int s = 0;
        int base = 1;
        for(int i = 0; i < 8; i++){
            s += data[7 - i] * base;
            base <<= 1;
        }
        return s;
    }

    private  void varlen_buffer_reset_dpu(varlen_buffer_dpu buf) {
        buf.len = 0;
        buf.llen = 0;
    }

    private  Object varlen_buffer_element_dpu(varlen_buffer_dpu buf, int idx) {
        if(idx >= buf.len - buf.llen) {
            return buf.ptr[(int) (idx - buf.len + buf.llen)];
        }
        else {
            Object res;
            res = buf.ptr_mram[0];
            //m_read(buf.ptr_mram + idx, &res, S64(1));
            return res;
        }
    }

    private  void varlen_buffer_set_element_dpu(varlen_buffer_dpu buf, int idx, long value) {
            if(idx >= buf.len - buf.llen) {
                buf.ptr[(int) (idx - buf.len + buf.llen)] = value;
            }
            else {
                buf.ptr_mram[idx] = value;
                //m_write(&value, buf->ptr_mram + idx, S64(1));
            }
    }

    private  void varlen_buffer_push_dpu(varlen_buffer_dpu buf, long v) {
        if(buf.llen == buf.capacity) {
            for(int pos = 0; pos < buf.capacity; pos ++){
                buf.ptr_mram[(int) (buf.len - buf.capacity + pos)] = buf.ptr[pos];
            }
            //m_write(buf.ptr, (buf.ptr_mram + buf.len - buf.capacity), S64(buf.capacity));
            buf.llen = 1;
            buf.ptr[0] = v;
        }
        else{
            buf.ptr[(int) buf.llen] = v;
            buf.llen++;
        }
        buf.len++;
    }


    public  void remove_data_blocks(mdbptr db) {
        mdbptr cur = db;
        while (!isInValid(cur)) {
            mdbptr nxt = cur.la.nxt;
            //free_node(&free_list_data_block, (mpvoid)cur);
            cur = nxt;
        }
    }



     boolean isInValid(mdbptr mdbptr){
        return mdbptr.la == null && mdbptr.data_blocks == null;
    }
    public  boolean b_length_check(Bnode nn) {
//        if (!(in_bbuffer(nn))) {
//            return false;
//        }
        if(nn == null) return false;
        Bnode bn = nn;
        int nnlen = (int)bn.len;
        mdbptr keysdb = bn.keys, addrsdb = bn.addrs, caddrsdb = bn.caddrs;
        len_addr keysla = keysdb.la, addrsla = addrsdb.la,
                caddrsla = caddrsdb.la;


        if (!(in_dbbuffer(keysdb) && in_dbbuffer(addrsdb) &&
                in_dbbuffer(caddrsdb))) {
            return false;
        }
        if ((keysla.len != nnlen) || (addrsla.len != nnlen) ||
                (caddrsla.len != nnlen)) {

            return false;
        }
        return true;
    }

    private  boolean in_dbbuffer(mdbptr addr) {
        // dbbuffer_start define in data_block.h
        // dbbuffer_start be initialized as dbbuffer + 1;
        // dbbuffer_end = dbbuffer_start + (DB_BUFFER_SIZE / sizeof(data_block)) - 1;
        // return addr >= dbbuffer_start && addr < dbbuffer_end;
        return false;
    }


}
