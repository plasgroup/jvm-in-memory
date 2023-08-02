package pim.algorithm;

import com.upmem.dpu.DpuException;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUJVMMemSpaceKind;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import static pim.algorithm.TreeWriter.*;

public class BSTBuilder {
    static Logger bstBuildingLogger = PIMLoggers.bstBuildingLogger;
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
        protected abstract Pair<K, V> generatePair();
        public ArrayList<Pair<K, V>> generatePairs(int pairCount) {
            HashSet<K> keys = new HashSet<>();
            ArrayList<Pair<K, V>> result = new ArrayList<>();
            while(result.size() < pairCount){
                Pair<K, V> kvPair = generatePair();
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



    public static TreeNode buildLargePIMTree(ArrayList<Pair<Integer, Integer>> pairs, int totalNodeCount, int nodeCountInCPU){
        try {
            for(int i = 0; i < 128; i++){
                UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
            }
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, 24);

        return root;
    }


    public static TreeNode buildPIMTree(ArrayList<Pair<Integer, Integer>> pairs) {
        if (pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        bstBuildingLogger.logf("(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", 1, pairs.get(0).key, pairs.get(0).val);

        for (int i = 1; i < pairs.size(); i++) {
            bstBuildingLogger.logf( "(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", i + 1, pairs.get(i).key, pairs.get(i).val);
            root.insert(pairs.get(i).key, pairs.get(i).val);
        }
        return root;
    }
}
