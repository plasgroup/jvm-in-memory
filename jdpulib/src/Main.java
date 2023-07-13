import com.sun.source.tree.Tree;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;

import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        TreeNode cpuNode = new CPUTreeNode(53, 21);
        TreeNode dr1 = (TreeNode) UPMEM.getInstance().createObject(0, DPUTreeNode.class, 48, 29);
//        TreeNode dr2 = (TreeNode) UPMEM.getInstance().createObject(0, DPUTreeNode.class, 56, 45);
//        cpuNode.setLeft(dr1);
//        cpuNode.setRight(dr2);
//
//        System.out.println("retrieve " + cpuNode.search(53));
//        System.out.println("retrieve " + cpuNode.search(48));
//        System.out.println("retrieve " + cpuNode.search(56));

        if(true) return;
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new IntIntValuePairGenerator(1000).genPairs(100);


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