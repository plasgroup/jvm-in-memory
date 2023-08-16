package pim.algorithm;

import com.sun.source.tree.Tree;
import com.upmem.dpu.DpuException;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.dpu.DPUJVMMemSpaceKind;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static pim.ExperimentConfigurator.noSearch;
import static pim.algorithm.BSTBuilder.*;
import static pim.algorithm.TreeWriter.INSTANCE_SIZE;
import static pim.algorithm.TreeWriter.getTreeSize;

public class BSTTester {

    static List<Integer> keys = readArrayList();

    public static void writeKV(int count, String path){
        try {
            int B = 20;
            int batch = Integer.MAX_VALUE / B;
            FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(bos);
            int upper = batch;
            int avg = count / 20;
            for(int i = 0; i < B; i ++){
                ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
                        new IntIntValuePairGenerator(upper - batch, upper).generatePairs(avg);
                for(BSTBuilder.Pair<Integer, Integer> pair : pairs){
                    osw.write(pair.getKey() + " " + pair.getVal() + "\n");
                }
                osw.flush();
                upper += batch;
            }
            osw.close();
            bos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        TreeNode root;
        if(ExperimentConfigurator.buildFromSerializedData){
            try{
                root = cpuPartTreeFromFile("CPU_TREE_" + totalNodeCount + ".txt");
            }catch (IOException e){
                 throw new RuntimeException(e);
            }
        } else {
            root = BSTBuilder.buildCPUTree("key_values-" + totalNodeCount + ".txt");
        }


        if(ExperimentConfigurator.serializeToFile)
            serializeTreeToFile(root, "CPU_TREE_" + totalNodeCount + ".txt");
        return queryInTree(queriesCount, root);
    }

    public static int evaluatePIMBST(int totalNodeCount, int queryCount, int cpuLayerCount){
        TreeNode root;
        if(ExperimentConfigurator.buildFromSerializedData){
            System.out.println("Build Tree From Images");
            try {

                for(int i = 0; i < UPMEM.dpuInUse; i++){
                    UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
                }
                writeDPUImages(totalNodeCount, ExperimentConfigurator.imagesPath);

                System.out.println("load CPU part tree");
                root = cpuPartTreeFromFile("PIM_TREE_" + totalNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }
        }else{
            root = buildLargePIMTree("key_values-" + totalNodeCount + ".txt", cpuLayerCount);
        }

        if(ExperimentConfigurator.serializeToFile)
            serializeTreeToFile(root, "PIM_TREE_" + totalNodeCount + ".txt");

        int t = queryInTree(queryCount, root);
        System.out.println("proxy search count = " + DPUTreeNodeProxyAutoGen.searchDispatchCount);
        return t;
    }

    private static void writeDPUImages(int totalNodeCount, String imagesPath) {
        int i = 0;
        while(true){
            String filePath = imagesPath + "[" + totalNodeCount + "]DPU#" + i + ".img";
            File imgI = new File(filePath);
            if(!imgI.exists()) return;
            System.out.println("load image to DPU#" + i + " from file " + filePath);
            try (FileInputStream inputStream = new FileInputStream(imagesPath + "[" + totalNodeCount + "]DPU#" + i + ".img")) {
                byte[] bs = inputStream.readAllBytes();
                UPMEM.getInstance().getDPUManager(i).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_HEAPSPACE,2000000 * INSTANCE_SIZE);
                UPMEM.getInstance().getDPUManager(i).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE,  bs, 0);
                UPMEM.getInstance().getDPUManager(i).garbageCollector.updateHeapPointerToDPU();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            i++;
        }
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
