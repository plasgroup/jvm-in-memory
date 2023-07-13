import com.sun.source.tree.Tree;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;

import java.util.ArrayList;


public class Main {

    static void tryTestCase(TreeNode root, int k, int v){
        int rv = root.search(k);
        if(rv != v) try {
            throw new Exception("when retrieval " + k + " correct val = " + v + " get " + rv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));




        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(10000).genPairs(1000);


        TreeNode root = BSTBuilder.build(pairs);
        UPMEM.getInstance().getDPUManager(0).garbageCollector.readBackHeapSpacePt();



        int correct = 0;
        // test
        for(int i = 0; i < pairs.size(); i++){
            int key = pairs.get(i).getKey();
            int val = pairs.get(i).getVal();
            int retrievedVal = root.search(key);
            System.out.printf("Test search key = %d. Correct val = %d, retrieved val = %d\n", key, val, retrievedVal);
            if(val != retrievedVal){
                System.out.println("test case fail at index " + i);
                System.out.println();
            }else{
                correct++;
            }
        }
        System.out.printf("test finished. Correct %d/%d\n", correct, pairs.size());


    }


}