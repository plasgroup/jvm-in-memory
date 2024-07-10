import pim.BatchDispatcher;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.UPMEMConfigurator;
import pim.algorithm.BSTBuilder;
import pim.algorithm.BSTTester;
import pim.algorithm.DPUTreeNode;
import pim.algorithm.IntIntValuePairGenerator;
import pim.algorithm.TreeNode;
import pim.algorithm.TreeWriter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pim.ExperimentConfigurator.*;
import static pim.algorithm.BSTBuilder.buildCpuPartTreeFromFile;
import static pim.algorithm.BSTBuilder.buildPIMTreeByInsert;
import static pim.algorithm.BSTTester.readIntergerArrayList;
import static pim.algorithm.TreeWriter.writeDPUImages;

public class Main {
    public static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();
    public static void parseParameters(String[] args){
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            String[] items = arg.split("=");
            String argumentName = items[0];
            System.out.println(argumentName + " " + args[i]);
            if("NO_SEARCH".equals(argumentName)){
                noSearch = true;
                if(items.length > 1) noSearch = Integer.parseInt(items[1]) != 0;
            }else if("BUILD_FROM_IMG".equals(argumentName)){
                buildFromSerializedData = false;
                if(items.length > 1) buildFromSerializedData = Integer.parseInt(items[1]) != 0;
            }else if("SERIALIZE_TREE".equals(argumentName)){
                serializeToFile = true;
                if(items.length > 1) serializeToFile = Integer.parseInt(items[1]) != 0;
            }else if("TYPE".equals(argumentName)){
                experimentType = items[1];
            }else if("QUERIES".equals(argumentName)){
                queryCount = Integer.parseInt(items[1]);
            }else if("CPU_LAYER_COUNT".equals(argumentName)){
                cpuLayerCount = Integer.parseInt(items[1]);
            }else if("DPU_COUNT".equals(argumentName)){
                dpuInUse = Integer.parseInt(items[1]);
            }else if("NODES".equals(argumentName)){
                totalNodeCount = Integer.parseInt(items[1]);
            }else if("IMG_PATH".equals(argumentName)){
                imagesPath = Arrays.stream(items).skip(1).reduce((s1,s2) -> s1+s2).get().replace("\"", "");
            }else if("WRITE_KV".equals(argumentName)){
                writeKeyValue = true;
                writeKeyValueCount = Integer.parseInt(items[1]);
            }else if("PERF_MODE".equals(argumentName)){
                performanceEvaluationMode = true;
                if(items.length > 1) performanceEvaluationMode = Integer.parseInt(items[1]) != 0;
            }else if("CPU_PERF_REPEAT".equals(argumentName)){
                cpuPerformanceEvaluationRepeatTime = Integer.parseInt(items[1]);
            }else if("PIM_PERF_REPEAT".equals(argumentName)){
                pimPerformanceEvaluationRepeatTime = Integer.parseInt(items[1]);
            }else if("EVAL_CPU_PERF".equals(argumentName)){
                cpuPerformanceEvaluation = true;
                if(items.length > 1) cpuPerformanceEvaluation = Integer.parseInt(items[1]) != 0;
            }else if("EVAL_PIM_PERF".equals(argumentName)){
                pimPerformanceEvaluation = true;
                if(items.length > 1) pimPerformanceEvaluation = Integer.parseInt(items[1]) != 0;
            }else if("EVAL_NODES".equals(argumentName)){
                performanceEvaluationNodeCount = Integer.parseInt(items[1]);
            }else if("BATCH_DISPATCH".equals(argumentName)){
                performanceEvaluationEnableBatchDispatch = true;
                if(items.length > 1) performanceEvaluationEnableBatchDispatch = Integer.parseInt(items[1]) != 0;
            }else if("DIRECT_TREE_BULID".equals(argumentName)){
                buildTreeDirectly = true;
                if(items.length > 1) buildTreeDirectly = Integer.parseInt(items[1]) != 0;
            }else if("JVM_SIMULATOR".equals(argumentName)){
	        useSimulator = true;
		if(items.length > 1) useSimulator = Integer.parseInt(items[1]) != 0;
	    }

        }
    }

    public static void performanceEvaluation(){
        TreeNode PIMRoot;
        TreeNode CPURoot;
        long totalTimeInMs = 0;
        int r = 0;
        List<Integer> keys = readIntergerArrayList("keys_random.txt");

        if (cpuPerformanceEvaluation) {
            try {
                CPURoot = BSTBuilder.buildCpuPartTreeFromFile("CPU_TREE_" + performanceEvaluationNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("begin evaluate CPU Tree 500,000 queries performance");
	    int pos = 0;
            for (int i = 0; i < cpuPerformanceEvaluationRepeatTime; i++) {
                
		long startTime = System.nanoTime();
                // ! Changed: 10000 -> 100
                for(int j = 0; j < queryCount / 100; j++){
			for(int k = 0; k < 100; k++){
                    int key = keys.get(pos++);
                    int v = CPURoot.search(key);
                    r += v;
			}
			  System.out.println("avg per query time = " + (System.nanoTime() - startTime) / pos / 1000 + "us");
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + cpuPerformanceEvaluationRepeatTime + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }

            System.out.println("CPU 500,000 queries average time = " + totalTimeInMs / cpuPerformanceEvaluationRepeatTime);
            System.out.println("single query average time = " + totalTimeInMs / cpuPerformanceEvaluationRepeatTime / (double)keys.size());
            System.out.println("end evaluate CPU Tree 500,000 queries performance");
        }

        if (pimPerformanceEvaluation) {
            // ! Build CPU and DPU tree here
            if (buildTreeDirectly) {
                PIMRoot = BSTBuilder.buildPIMTreeDirect("key_values-" + totalNodeCount + ".txt", cpuLayerCount);
                int count = TreeWriter.getTreeSize(PIMRoot);
                System.out.println("cpu size count = " + count);
            } else {
                try {
                    for (int i = 0; i < UPMEM.dpuInUse; i++) {
                        UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
                    }
                    writeDPUImages(performanceEvaluationNodeCount, ExperimentConfigurator.imagesPath);
                    System.out.println("load CPU part tree");
                    PIMRoot = buildCpuPartTreeFromFile("PIM_TREE_" + performanceEvaluationNodeCount + ".txt");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("begin evaluate PIM Tree 500,000 queries performance");
            totalTimeInMs = 0;

            BatchDispatcher bd1 = new BatchDispatcher();

            if(performanceEvaluationEnableBatchDispatch)
                UPMEM.beginRecordBatchDispatching(bd1);
            int pos = 0;
	    System.out.println("query count = " + queryCount);
            for (int i = 0; i < pimPerformanceEvaluationRepeatTime; i++) {
                long startTime = System.nanoTime();
                // ! Changed: 10000 -> 100
                for(int j = 0; j < queryCount / 100; j++){    
                    for(int k = 0; k < 100; k++){
                        int key = keys.get(pos++);
                        int v = PIMRoot.search(key);
                        r += v;
                    }
                    System.out.println("avg per query time = " + (System.nanoTime() - startTime) / pos / 100 + "us");
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + pimPerformanceEvaluationRepeatTime + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }
            if(performanceEvaluationEnableBatchDispatch)
                UPMEM.endRecordBatchDispatching();
            System.out.println("PIM 500,000 queries average time = " + totalTimeInMs / pimPerformanceEvaluationRepeatTime);
            System.out.println("single queries average time = " + totalTimeInMs / pimPerformanceEvaluationRepeatTime / (double)keys.size());
            System.out.println("end evaluate PIM Tree 500,000 queries performance");
        }
    }

    public static void main(String[] args) throws RemoteException {
        parseParameters(args);
        // if(true) return;

       upmemConfigurator.setDpuInUseCount(dpuInUse);

       System.out.println("dpu in use = " + dpuInUse);
       System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
       System.out.println("cpu tree layer count = " + cpuLayerCount);
       if(noSearch) System.out.println("No search mode");
       if(buildFromSerializedData) System.out.println("build tree from images, path = " + imagesPath);
       if(serializeToFile) System.out.println("output tree to file. Imgs path = " + imagesPath);
       if(writeKeyValue){
           System.out.println("Generate key value paris, and write to file. Count = " + writeKeyValueCount);
           BSTTester.writeKV(writeKeyValueCount, "key_values-" + writeKeyValueCount + ".txt");
       }

       UPMEM.initialize(upmemConfigurator);

       upmemConfigurator
               .setDpuInUseCount(dpuInUse)
               .setThreadPerDPU(UPMEM.perDPUThreadsInUse);

       if(performanceEvaluationMode) {
           performanceEvaluation();
           return;
       }

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


