import pim.UPMEMConfigurator;
import pim.algorithm.BSTBuilder;
import pim.UPMEM;
import pim.algorithm.IntIntValuePairGenerator;
import pim.algorithm.TreeNode;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(1000).genPairs(500);


        TreeNode root = BSTBuilder.build(pairs);

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