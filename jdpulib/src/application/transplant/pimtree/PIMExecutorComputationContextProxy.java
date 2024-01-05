package application.transplant.pimtree;

import framework.lang.struct.DummyProxy;
import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;

import java.util.List;
import java.util.function.BiFunction;

public class PIMExecutorComputationContextProxy extends PIMExecutorComputationContext implements IDPUProxyObject {
    public Integer address = -1;
    public Integer dpuID = -1;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    @Override
    public void L3_init(pptr down) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/pimtree/PIMExecutorComputationContext","L3_init:(Lapplication/transplant/pimtree/pptr;)V", down);
    }

    @Override
    public void node_count_add(int n, int a) {
        RPCHelper.invokeMethod(dpuID, address, "application/transplant/pimtree/PIMExecutorComputationContext","node_count_add:(II)V", n, a);
    }

    @Override
    public void b_newnode(Bnode newnode, mdbptr keys, mdbptr addrs, mdbptr caddrs, long height) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "b_newnode:(Lapplication/transplant/pimtree/Bnode;Lapplication/transplant/pimtree/mdbptr;Lapplication/transplant/pimtree/mdbptr;Lapplication/transplant/pimtree/mdbptr;J)V"
        , newnode, keys, addrs, caddrs, height);
    }

    @Override
    public void data_block_init(mdbptr db) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "data_block_init:(Lapplication/transplant/pimtree/mdbptr;)V", db);
    }

    @Override
    public Object ht_search(ht_slot[] ht, long key, BiFunction<ht_slot, Long, Integer> filter) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "ht_search:([Lapplication/transplant/pimtree/ht_slot;JLjava/util/function/BiFunction;)java/lang/Object", ht, key, filter);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public int p_ht_get(ht_slot v, long key) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "p_ht_get:(Lapplication/transplant/pimtree/ht_slot;J)I", v, key);
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public pptr p_get(int key) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "p_get:(I)Lapplication/transplant/pimtree/pptr;", key);
        return (pptr) RPCHelper.getAReturnValue(dpuID, pptrProxy.class);
    }

    @Override
    public void p_newnode(long _key, long _value, long height, Pnode newnode) {
        super.p_newnode(_key, _value, height, newnode);
    }

    @Override
    public void insertKeyValue(int key, int value) {
        RPCHelper.invokeMethod(dpuID, address,
                "application/transplant/pimtree/PIMExecutorComputationContext",
                "insertKeyValue:(II)V", key, value);
    }

    @Override
    public Pnode alloc_pn() {
        return super.alloc_pn();
    }

    @Override
    public void b_search(Bnode nn, int len, List<Long> keys, List<Long> repkeys, List<pptr> repaddrs, List<Long> heights, int l, int l1, int l2, int l3) {
        super.b_search(nn, len, keys, repkeys, repaddrs, heights, l, l1, l2, l3);
    }

    @Override
    public boolean in_bbuffer(Bnode addr) {
        return super.in_bbuffer(addr);
    }

    @Override
    public long l3b_search(long key, L3Bnode addr, pptr value) {
        return super.l3b_search(key, addr, value);
    }

    @Override
    public int L3_node_size(byte height) {
        return super.L3_node_size(height);
    }

    @Override
    public void L3_insert_parallel(int length, int l, long[] keys, byte[] heights, pptr[] down, int[] newnode_size, byte[] maxHeightShared, Object[] right_predecessor_shared, Object[] right_newnode_shared) {
        super.L3_insert_parallel(length, l, keys, heights, down, newnode_size, maxHeightShared, right_predecessor_shared, right_newnode_shared);
    }

    @Override
    public int b_remove(Bnode nn, int len, List<Long> keys) {
        return super.b_remove(nn, len, keys);
    }

    @Override
    public long b_scan(long bb, long ee, Bnode nn, Long[] keys, Long[] addrs, int keyOffset, int addrOffset) {
        return super.b_scan(bb, ee, nn, keys, addrs, keyOffset, addrOffset);
    }

    @Override
    public Object L3_search(long key, int i, byte record_height, L3node[] rightmost, int i1) {
        return super.L3_search(key, i, record_height, rightmost, i1);
    }

    @Override
    public int nested_search(int len, List<Long> keys, List<Long> repkeys, List<pptr> addrs, List<Long> heights, Object[] paths, int siz, int offset_keys, int offset_repkeys, int offset_address, int offset_height, int offset_path) {
        return super.nested_search(len, keys, repkeys, addrs, heights, paths, siz, offset_keys, offset_repkeys, offset_address, offset_height, offset_path);
    }

    @Override
    public long PPTR_TO_I64(pptr addr) {
        return super.PPTR_TO_I64(addr);
    }

    @Override
    public Long p_get_height(Long key) {
        return super.p_get_height(key);
    }

    @Override
    boolean ht_no_greater_than(int a, int b) {
        return super.ht_no_greater_than(a, b);
    }

    @Override
    public void l3b_insert_parallel(int n, int l, int r) {
        super.l3b_insert_parallel(n, l, r);
    }

    @Override
    public int b_search_with_path_task_siz(int len) {
        return super.b_search_with_path_task_siz(len);
    }

    @Override
    public void L3_remove_parallel(int length, L3node[] nodes, byte[] maxHeightShared, Object[] leftNodeShared) {
        super.L3_remove_parallel(length, nodes, maxHeightShared, leftNodeShared);
    }

    @Override
    public void l3b_remove_parallel(int n, int l, int r) {
        super.l3b_remove_parallel(n, l, r);
    }

    @Override
    public int L3_scan_search(long begin, long end, Object addrbuf) {
        return super.L3_scan_search(begin, end, addrbuf);
    }

    @Override
    public mdbptr data_block_from_mram(mdbptr db, Object[] bufkeys, int len, int len2) {
        return super.data_block_from_mram(db, bufkeys, len, len2);
    }

    @Override
    public mdbptr data_block_allocate() {
        return super.data_block_allocate();
    }

    @Override
    public int b_filter_cache_mram(Bnode nn, long l, long r, Object[] keys, Object[] addrs, Object[] caddrs, int offset_addrs, int offset_caddrs) {
        return super.b_filter_cache_mram(nn, l, r, keys, addrs, caddrs, offset_addrs, offset_caddrs);
    }

    @Override
    public int b_filter_cache(Bnode nn, long l, long r, long[] keys, long[] addrs, long[] caddrs) {
        return super.b_filter_cache(nn, l, r, keys, addrs, caddrs);
    }

    @Override
    public Object push_variable_reply_head(int taskletId) {
        return super.push_variable_reply_head(taskletId);
    }

    @Override
    public void l3b_scan(long lkey, long rkey, varlen_buffer_dpu addr_buf, varlen_buffer_dpu up_buf, varlen_buffer_dpu down_buf) {
        super.l3b_scan(lkey, rkey, addr_buf, up_buf, down_buf);
    }

    @Override
    public void remove_data_blocks(mdbptr db) {
        super.remove_data_blocks(db);
    }

    @Override
    boolean isInValid(mdbptr mdbptr) {
        return super.isInValid(mdbptr);
    }

    @Override
    public boolean b_length_check(Bnode nn) {
        return super.b_length_check(nn);
    }
}
