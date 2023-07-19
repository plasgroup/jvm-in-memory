package pim.algorithm;

import com.upmem.dpu.DpuException;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static pim.algorithm.TreeWriter.convertCPUTreeToPIMTree;

public class BSTBuilder {
    public static class Pair<K, V> {
        K key;
        V val;

        public Pair(K k, V v) {
            this.key = k;
            this.val = v;
        }

        public K getKey() {
            return key;
        }

        public V getVal() {
            return val;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public void setVal(V val) {
            this.val = val;
        }
    }


    static abstract class BaseKeyValuePairGenerator<K, V> {
        protected abstract Pair<K, V> genPair();

        public ArrayList<Pair<K, V>> genPairs(int pairCount) {
            HashSet<K> keys = new HashSet<>();
            ArrayList<Pair<K, V>> result = new ArrayList<>();
            while(result.size() < pairCount){
                Pair<K, V> kvPair = genPair();
                if(keys.contains(kvPair.key))
                    continue;
                keys.add(kvPair.key);
                result.add(kvPair);
            }
            return result;
        }
    }



    public static TreeNode buildCPUTree(ArrayList<Pair<Integer, Integer>> pairs){
        if(pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            ((CPUTreeNode)root).insertNewCPUNode(pairs.get(i).key, pairs.get(i).val);
        }
        return root;
    }

    public static TreeNode buildLargePIMTree(){
        try {
            UPMEM.getInstance().getDPUManager(0).createObject(DPUTreeNode.class, new Object[]{0, 0});
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DPUCacheManager classCacheManager = UPMEM.getInstance().getDPUManager(0).classCacheManager;
        int dpuClassAddress = classCacheManager.getClassStrutCacheLine("pim/algorithm/DPUTreeNode").marmAddr;

        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(100000)
                .genPairs(2000);
        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, 2000, 10, dpuClassAddress);
        return root;
    }



    public static TreeNode buildPIMTree(ArrayList<Pair<Integer, Integer>> pairs) {
        if (pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        Logger.logf("bst:building","(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", 1, pairs.get(0).key, pairs.get(0).val);

        for (int i = 1; i < pairs.size(); i++) {
            Logger.logf("bst:building", "(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", i + 1, pairs.get(i).key, pairs.get(i).val);
            root.insert(pairs.get(i).key, pairs.get(i).val);
        }
        return root;
    }
}
