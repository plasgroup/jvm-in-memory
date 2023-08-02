package pim.algorithm;

import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static pim.algorithm.BSTBuilder.buildLargePIMTree;

public class BSTTester {

    static List<Integer> keys = readArrayList();

    public static boolean noSearch = false;
    public static ArrayList<Integer> readArrayList(){
        ArrayList<Integer> resultList = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream("keys_random.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            String[] str = new String(bis.readAllBytes()).split("\n");
            for(int i = 0; i < str.length; i++){
                if(!"".equals(str[i])){
                    resultList.add(Integer.parseInt(str[i]));
                }
            }
            return resultList;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Logger bstTestLogger = PIMLoggers.bstTestLogger;
    public static void testBSTCPU(int totalNodeCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(0, Integer.MAX_VALUE).generatePairs(totalNodeCount);

        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        int correct = 0;

        for(int i = 0; i < pairs.size(); i++){
            int key = pairs.get(i).getKey();
            int val = pairs.get(i).getVal();
            int retrievedValue = root.search(key);
            bstTestLogger.logf( "==> (Test) search key = %d. correct value = %d, retrieved value = %d\n", key, val, retrievedValue);
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
                IntIntValuePairGenerator.fromFile("key_values-" + totalNodeCount + ".txt");
        TreeNode root = BSTBuilder.buildCPUTree(pairs);

        return queryInTree(queriesCount, root);
    }


    public static int evaluateLargeBST(int totalNodeCount, int queryCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                IntIntValuePairGenerator.fromFile("key_values-" + totalNodeCount + ".txt");
        TreeNode root = buildLargePIMTree(pairs);

        return queryInTree(queryCount, root);
    }

    private static int queryInTree(int queryCount, TreeNode root) {
        int i = 0;
        int s = 0;
        if(noSearch) return -1;
        while(i < queryCount){
            int qk = keys.get(i);
            int v = root.search(qk);
            s += v;
            i++;
        }
        return s;
    }

    public static void testLargeBST(int totalNodeCount, int queryCount){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(0, totalNodeCount)
                .generatePairs(queryCount);
        TreeNode root = buildLargePIMTree(pairs);
        int correct = 0;

        for(int i = 0; i < pairs.size(); i++){
            int key = pairs.get(i).getKey();
            int val = pairs.get(i).getVal();
            int retrievedValue = root.search(key);

            bstTestLogger.logf("==> (Test#"+ i + ") search key = %d. correct value = %d, retrieved value = %d\n", key, val, retrievedValue);

            if(val != retrievedValue){
                bstTestLogger.logln("test case fail at index " + i + "\n");
            }else{
                correct++;
            }
        }

        System.out.printf(" == Test finished. Correct %d/%d == \n", correct, pairs.size());
    }




}
