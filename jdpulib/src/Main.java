import com.sun.source.tree.Tree;
import com.upmem.dpu.DpuException;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUGarbageCollector;
import pim.logger.Logger;
import pim.utils.BytesUtils;

import java.io.IOException;
import java.util.*;

import static pim.algorithm.BSTBuilder.buildLargePIMTree;
import static pim.algorithm.TreeWriter.verifyLargePIMTree;


public class Main {
    static void testBST(){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(100000).genPairs(20000);

        Logger bstTestLogger = Logger.getLogger("bst:testing");
        TreeNode root = BSTBuilder.buildPIMTree(pairs);
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

    static void testBSTCPU(){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(100000).genPairs(20000);

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

    static int evaluateCPU(){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(100000000).genPairs(2000000);
        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        int i = 0;
        int s = 0;
        while(i < 10000000){
            int query = random.nextInt(2000000);
            int qk = pairs.get(query).getKey();
            int v = root.search(qk);
            s += v;
            i++;
        }
        return s;
    }
    static int evaluateDPU(){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(100000).genPairs(20000);
        TreeNode root = BSTBuilder.buildPIMTree(pairs);

        int i = 0;
        int s = 0;
        while(i < 1000){
            int query = random.nextInt(20000);
            int qk = pairs.get(query).getKey();
            int v = root.search(qk);
            s += v;
            i++;
        }
        return 0;
    }







    static Random random = new Random();

        public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        Logger.disableAllBeginWith("pim");


            ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(100000)
                    .genPairs(2000);
            TreeNode root = buildLargePIMTree(pairs);
            int correct = 0;

            for(int i = 0; i < pairs.size(); i++){
                int key = pairs.get(i).getKey();
                int val = pairs.get(i).getVal();
                int retrievedValue = root.search(key);
                Logger.logf("bst:testing", "==> (Test) search key = %d. correct value = %d, retrieved value = %d\n", key, val, retrievedValue);

                if(val != retrievedValue){
                    Logger.logf("bst:testing" ,"test case fail at index " + i + "\n");
                }else{
                    correct++;
                }
            }

            System.out.printf(" == Test finished. Correct %d/%d == \n", correct, pairs.size());
    }
    /*
    * java -XX:+PreserveFramePointer -Djava.library.path=/home/huang/Desktop/upmem-2023.1.0-Linux-x86_64/lib -Dfile.encoding=UTF-8 -classpath "/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/jdpulib/out/production/jdpulib":"/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/lib/dpu.jar" Main
    * */

}