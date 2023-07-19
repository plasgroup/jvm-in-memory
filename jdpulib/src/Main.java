import com.sun.source.tree.Tree;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.dpu.DPUGarbageCollector;
import pim.logger.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;


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


    static void writeKey(int key, byte[] heap, int instanceAddress){

    }
    static void writeValue(int val, byte[] heap, int instanceAddress){

    }
    static void writeLeft(int left, byte[] heap, int instanceAddress){

    }
    static void writeRight(int right, byte[] heap, int instanceAddress){

    }
    static void convertCPUTreeToPIMTree(TreeNode root, int totalTreeNodeCount, int nodeAmountInCPU){
        byte[] heapMemory = new byte[(totalTreeNodeCount - nodeAmountInCPU) * 24 + 8];
        int nodeInDPU = 0;
        int nodeInCPU = 0;
        int heapPoint = DPUGarbageCollector.heapSpaceBeginAddr + 8;
        TreeNode point = root;
        Queue<TreeNode[]> queue = new ArrayDeque<>();
        queue.add(new TreeNode[]{null, point});
        while(queue.size() > 0){
            TreeNode[] record = queue.remove();
            TreeNode thisNode = record[1];
            TreeNode parent = record[0];
            if(thisNode.getLeft() != null) queue.add(new TreeNode[]{thisNode, thisNode.getLeft()});
            if(thisNode.getRight() != null) queue.add(new TreeNode[]{thisNode, thisNode.getRight()});

            // whether convert this node to DPUNode or not
            if(nodeInCPU < nodeAmountInCPU){
                nodeInCPU++;
                continue;
            }
            DPUTreeNode dpuNodeConverted = new DPUTreeNode(thisNode.getKey(), thisNode.getVal());
            dpuNodeConverted.setKey(heapPoint); // set dpu mram pt in key field
            writeKey(thisNode.getKey(), heapMemory, heapPoint); // write key
            writeValue(thisNode.getVal(), heapMemory, heapPoint); // write value

            // forward
            thisNode.setKey(-1);
            thisNode.setVal(-1);
            thisNode.setLeft(dpuNodeConverted);

            if(parent == null) continue;

            // already be forward
            if(parent.getKey() != -1 && parent.getVal() == -1){
                DPUTreeNode treeNode = (DPUTreeNode) parent.getLeft();
                int parentAddress = treeNode.getKey();
                if(treeNode.getLeft() == thisNode){
                    treeNode.setLeft(dpuNodeConverted);
                    writeLeft(heapPoint, heapMemory, parentAddress);
                }
                if(treeNode.getRight() == thisNode){
                    treeNode.setRight(dpuNodeConverted);
                    writeRight(heapPoint, heapMemory, parentAddress);
                }

            }
            heapPoint += 24;
            nodeInDPU ++;
        }
        if(nodeInCPU + nodeInDPU != totalTreeNodeCount){
            throw new RuntimeException();
        }
    }


    public static TreeNode buildLargePIMTree(){
        ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                new IntIntValuePairGenerator(100000).genPairs(20000);
        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, 20000, 10);
        return root;
    }



    public byte[] convertToInstance(TreeNode treeNode){
        byte[] instanceData = new byte[24];

        return instanceData;
    }

    public void verifyLargePIMTree(TreeNode root, byte[] heap){

    }
    static Random random = new Random();


        public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        Logger.disableAllBeginWith("pim");
        evaluateDPU();
        //evaluateCPU();
       //     buildLargePIMTree();
    }
    /*
    * java -XX:+PreserveFramePointer -Djava.library.path=/home/huang/Desktop/upmem-2023.1.0-Linux-x86_64/lib -Dfile.encoding=UTF-8 -classpath "/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/jdpulib/out/production/jdpulib":"/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/lib/dpu.jar" Main
    * */

}