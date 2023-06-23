package pim.algorithm;

import java.util.ArrayList;
import java.util.Random;

public class BSTBuilder {
    static class Pair<K, V> {
        K key;
        V val;
        public Pair(K k, V v) {
            this.key = k;
            this.val = v;
        }
    }


    static abstract class BaseKeyValuePairGenerator<K, V> {
        protected abstract Pair<K, V> genPair();

        public ArrayList<Pair<K, V>> genPairs(int pairCount) {
            ArrayList<Pair<K, V>> result = new ArrayList<>();
            for (int i = 0; i < pairCount; i++) {
                result.add(genPair());
            }
            return result;
        }
    }


    static class IntIntValuePairGenerator extends BaseKeyValuePairGenerator<Integer, Integer> {
        static Random random = new Random();
        public static IntIntValuePairGenerator instance = null;

        public static IntIntValuePairGenerator getInstance() {
            if (instance == null) instance = new IntIntValuePairGenerator();
            return instance;
        }

        @Override
        protected Pair<Integer, Integer> genPair() {
            return new Pair<>(random.nextInt(1000), random.nextInt(1000));
        }
    }

    private static ArrayList<Pair<Integer, Integer>> pairs =
            IntIntValuePairGenerator.getInstance().genPairs(1000);

    public static void build() {
        if (pairs.size() == 0) return;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            root.insert(pairs.get(i).key, pairs.get(i).val);
        }
    }
}
