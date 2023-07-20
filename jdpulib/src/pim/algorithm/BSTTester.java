package pim.algorithm;

import pim.logger.Logger;

import java.util.ArrayList;
import java.util.Random;

import static pim.algorithm.BSTBuilder.buildLargePIMTree;

public class BSTTester {
    static Random random = new Random();

    public static void testBSTCPU(int totalNodeCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(Integer.MAX_VALUE).genPairs(totalNodeCount);

        Logger bstTestLogger = Logger.getLogger("bst:testing");
        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        int correct = 0;

        for(int i = 0; i < pairs.size(); i++){
            int key = pairs.get(i).getKey();
            int val = pairs.get(i).getVal();
            int retrievedValue = root.search(key);
            Logger.logf("bst:testing", "==> (Test) search key = %d. correct value = %d, retrieved value = %d\n", key, val, retrievedValue);
            if(val != retrievedValue){
                bstTestLogger.log("test case fail at index " + i + "\n");
            }else{
                correct++;
            }
        }
        bstTestLogger.logf(" == Test finished. Correct %d/%d == \n", correct, pairs.size());
    }

    public static int evaluateCPU(int totalNodeCount, int queriesCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(Integer.MAX_VALUE).genPairs(totalNodeCount);
        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        int i = 0;
        int s = 0;
        while(i < queriesCount){
            int query = random.nextInt(totalNodeCount);
            int qk = pairs.get(query).getKey();
            int v = root.search(qk);
            s += v;
            i++;
        }
        return s;
    }



    public static void evaluateLargeBST(int totalNodeCount, int queryCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(Integer.MAX_VALUE)
                .genPairs(totalNodeCount);
        TreeNode root = buildLargePIMTree(pairs);
        int i = 0;
        int s = 0;
        while(i < queryCount){
            int query = random.nextInt(totalNodeCount);
            int qk = pairs.get(query).getKey();
            int v = root.search(qk);
            s += v;
            i++;
        }
    }
    public static void testLargeBST(int totalNodeCount, int queryCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(totalNodeCount)
                .genPairs(queryCount);
        TreeNode root = buildLargePIMTree(pairs);
        int correct = 0;

        for(int i = 0; i < pairs.size(); i++){
            int key = pairs.get(i).getKey();
            int val = pairs.get(i).getVal();
            int retrievedValue = root.search(key);
            Logger.logf("bst:testing", "==> (Test#"+ i + ") search key = %d. correct value = %d, retrieved value = %d\n", key, val, retrievedValue);

            if(val != retrievedValue){
                Logger.logf("bst:testing" ,"test case fail at index " + i + "\n");
            }else{
                correct++;
            }
        }

        System.out.printf(" == Test finished. Correct %d/%d == \n", correct, pairs.size());
    }




}
