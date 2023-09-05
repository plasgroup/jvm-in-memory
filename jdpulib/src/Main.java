import com.sun.source.tree.Tree;
import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.UPMEMConfigurator;
import pim.algorithm.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static pim.ExperimentConfigurator.*;
import static pim.algorithm.BSTBuilder.buildCpuPartTreeFromFile;
import static pim.algorithm.BSTTester.readIntergerArrayList;
import static pim.algorithm.BSTTester.writeKV;
import static pim.algorithm.TreeWriter.writeDPUImages;

public class Main {
    public static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();
    public static void parseParameters(String[] args){
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            String[] items = arg.split("=");
            String argumentName = items[0];
            if("NO_SEARCH".equals(argumentName)){
                noSearch = true;
            }else if("BUILD_FROM_IMG".equals(argumentName)){
                buildFromSerializedData = true;
            }else if("SERIALIZE_TREE".equals(argumentName)){
                serializeToFile = true;
            }else if("TYPE".equals(argumentName)){
                experimentType = items[1];
            }else if("QUERIES".equals(argumentName)){
                queryCount = Integer.parseInt(items[1]);
            }else if("CPU_LAYER_COUNT".equals(argumentName)){
                cpuLayerCount = Integer.parseInt(items[1]);
            }else if("DPU_COUNT".equals(items[1])){
                dpuInUse = Integer.parseInt(items[1]);
            }else if("NODES".equals(items[1])){
                totalNodeCount = Integer.parseInt(items[1]);
            }else if("IMG_PATH".equals(items[1])){
                imagesPath = Arrays.stream(items).skip(1).reduce((s1,s2) -> s1+s2).get().replace("\"", "");
            }else if("WRITE_KV".equals(argumentName)){
                writeKeyValue = true;
                writeKeyValueCount = Integer.parseInt(items[1]);
            }
        }
    }

    /**
     * Checkout
     *
     */

    public static void main(String[] args) {
        parseParameters(args);
        upmemConfigurator.setDpuInUseCount(dpuInUse);

        System.out.println("dpu in use = " + dpuInUse);
        System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
        System.out.println("cpu tree layer count = " + cpuLayerCount);
        if(noSearch) System.out.println("No search mode");
        if(buildFromSerializedData) System.out.println("build tree from images, path = " + imagesPath);
        if(serializeToFile) System.out.println("output tree to file. Imgs path = " + imagesPath);
        if(writeKeyValue){
            System.out.println("Generate key value paris, and write to file. Count = " + writeKeyValueCount);
            writeKV(writeKeyValueCount, "key_values-" + writeKeyValueCount + ".txt");
        }

        UPMEM.initialize(upmemConfigurator);

        upmemConfigurator
                .setDpuInUseCount(dpuInUse)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse);



        // 任务队列内的任务识别是正常结束的。
        // 单个任务下也能正常结束
//
//
//        BatchDispatcher bd = new BatchDispatcher();
//        UPMEM.beginRecordBatchDispatching(bd);
//        TreeNode tn = (TreeNode) UPMEM.getInstance().createObject(0, DPUTreeNode.class, 2000,1214);
//        for(int i = 0; i < 1000; i++){
//            tn.search(2000);
//        }
//        if(true) return;
        try {
            TreeNode CPURoot = BSTBuilder.buildCpuPartTreeFromFile("CPU_TREE_10000000.txt");
            TreeNode PIMRoot;
            try {
                for(int i = 0; i < UPMEM.dpuInUse; i++){
                    UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
                }
                writeDPUImages(totalNodeCount, ExperimentConfigurator.imagesPath);

                System.out.println("load CPU part tree");
                PIMRoot = buildCpuPartTreeFromFile("PIM_TREE_" + totalNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DpuException e) {
                throw new RuntimeException(e);
            }

            List<Integer> keys = readIntergerArrayList("keys_random.txt");

            System.out.println("begin evaluate CPU Tree 500,000 queries performance");
            int repeatTime = 1;
            long totalTimeInMs = 0;
            for(int i = 0; i < repeatTime; i++){
                long startTime = System.nanoTime();
                for(int key : keys){
                    int v = CPURoot.search(key);
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + repeatTime + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }

            System.out.println("CPU 500,000 queries average time = " + totalTimeInMs / repeatTime);
            System.out.println("end evaluate CPU Tree 500,000 queries performance");

            System.out.println("begin evaluate PIM Tree 500,000 queries performance");
            totalTimeInMs = 0;
            repeatTime = 5;

            BatchDispatcher bd1 = new BatchDispatcher();

            //UPMEM.beginRecordBatchDispatching(bd1);
            for(int i = 0; i < repeatTime; i++){
                long startTime = System.nanoTime();
                int k = 0;
                for(int key : keys){
                    int v = PIMRoot.search(key);
                    k++;
                    if(k % 1000 == 0)
                        System.out.println(k);
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + repeatTime + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }
            UPMEM.endRecordBatchDispatching();
            System.out.println("PIM 500,000 queries average time = " + totalTimeInMs / repeatTime);
            System.out.println("end evaluate PIM Tree 500,000 queries performance");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(true) return;
        if(args.length == 0){
            BSTTester.evaluatePIMBST(totalNodeCount, ExperimentConfigurator.queryCount,  ExperimentConfigurator.cpuLayerCount);
            return;
        }

        if("CPU".equals(experimentType)){
            BSTTester.evaluateCPU(totalNodeCount, queryCount);
        }else if("PIM".equals(experimentType)){
            BSTTester.evaluatePIMBST(totalNodeCount, queryCount, cpuLayerCount);
        }

    }
}

