import com.sun.source.tree.Tree;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.logger.Logger;

import java.util.ArrayList;
import java.util.logging.Level;


public class Main {
    static void testBST(){

        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(10000).genPairs(2000);
        Logger bstTestLogger = Logger.getLogger("bst:testing");
        TreeNode root = BSTBuilder.build(pairs);
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
    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));
        Logger.disableAllBeginWith("pim");
        testBST();
    }


}