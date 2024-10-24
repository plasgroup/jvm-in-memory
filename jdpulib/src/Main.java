import framework.pim.BatchDispatcher;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import application.bst.BSTBuilder;
import application.bst.BSTTester;
import application.bst.DPUTreeNode;
import application.bst.TreeNode;
import framework.pim.dpu.classloader.ClassWriter;
import framework.primitive.control.ControlPrimitives;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static framework.pim.ExperimentConfigurator.*;
import static application.bst.BSTBuilder.*;
import static application.bst.BSTTester.readIntergerArrayList;
import static application.bst.TreeWriter.writeDPUImages;

/**
 * =======================================================================
 * This is a program for evaluating Binary Search Tree (BST) application
 ** =======================================================================
 **/

public class Main {
    public static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();

    /* Parse parameter for BST Experiment */
    public static void parseParameters(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String[] items = arg.split("=");
            String argumentName = items[0];
            System.out.println(argumentName + " " + args[i]);
            if ("NO_SEARCH".equals(argumentName)) {
                // not perform any search operation after building tree
                noSearch = true;
                if (items.length > 1)
                    noSearch = Integer.parseInt(items[1]) != 0;
            } else if ("BUILD_FROM_IMG".equals(argumentName)) { // build PIM Tree from serialized data
                buildFromSerializedData = false;
                if (items.length > 1)
                    buildFromSerializedData = Integer.parseInt(items[1]) != 0;
            } else if ("SERIALIZE_TREE".equals(argumentName)) { // serialize tree to files
                serializeToFile = true;
                if (items.length > 1)
                    serializeToFile = Integer.parseInt(items[1]) != 0;
            } else if ("TYPE".equals(argumentName)) { // could be "CPU" (experiment with CPU tree) or "PIM" (experiment
                                                      // with PIM tree)
                experimentType = items[1];
            } else if ("QUERIES".equals(argumentName)) { // queries count
                queryCount = Integer.parseInt(items[1]);
            } else if ("CPU_LAYER_COUNT".equals(argumentName)) { // CPU layer count
                cpuLayerCount = Integer.parseInt(items[1]);
            } else if ("DPU_COUNT".equals(argumentName)) { // DPU count
                dpuInUse = Integer.parseInt(items[1]);
            } else if ("NODES".equals(argumentName)) { // nodes count
                totalNodeCount = Integer.parseInt(items[1]);
            } else if ("IMG_PATH".equals(argumentName)) { // path for saving images
                imagesPath = Arrays.stream(items).skip(1).reduce((s1, s2) -> s1 + s2).get().replace("\"", "");
            } else if ("WRITE_KV".equals(argumentName)) { // whether generate key values pairs and write to key-values
                                                          // files
                writeKeyValue = true;
                writeKeyValueCount = Integer.parseInt(items[1]);
            } else if ("PERF_MODE".equals(argumentName)) { // performance mode. In this mode, the execution time would
                                                           // ne measured.
                performanceEvaluationMode = true;
                if (items.length > 1)
                    performanceEvaluationMode = Integer.parseInt(items[1]) != 0;
            } else if ("CPU_PERF_REPEAT".equals(argumentName)) { // CPU Tree repeat measure times
                cpuPerformanceEvaluationRepeatTime = Integer.parseInt(items[1]);
            } else if ("PIM_PERF_REPEAT".equals(argumentName)) { // PIM Tree repeat measure times
                pimPerformanceEvaluationRepeatTime = Integer.parseInt(items[1]);
            } else if ("EVAL_CPU_PERF".equals(argumentName)) { // whether evaluate CPU tree preformance
                cpuPerformanceEvaluation = true;
                if (items.length > 1)
                    cpuPerformanceEvaluation = Integer.parseInt(items[1]) != 0;
            } else if ("EVAL_PIM_PERF".equals(argumentName)) { // whether evaluate PIM tree preformance
                pimPerformanceEvaluation = true;
                if (items.length > 1)
                    pimPerformanceEvaluation = Integer.parseInt(items[1]) != 0;
            } else if ("EVAL_NODES".equals(argumentName)) { // nodes count
                performanceEvaluationNodeCount = Integer.parseInt(items[1]);
            } else if ("BATCH_DISPATCH".equals(argumentName)) { // whether use batch dispatching
                performanceEvaluationEnableBatchDispatch = true;
                if (items.length > 1)
                    performanceEvaluationEnableBatchDispatch = Integer.parseInt(items[1]) != 0;
            } else if ("JVM_SIMULATOR".equals(argumentName)) { // use simulator
                ExperimentConfigurator.useSimulator = true;
                if (items.length > 1)
                    ExperimentConfigurator.useSimulator = Integer.parseInt(items[1]) != 0;
            } else if ("PROFILING".equals(argumentName)) {
                ExperimentConfigurator.profiling = true;
            } else if ("WRITE_KV_ONLY".equals(argumentName)) {
                writeKeyValueOnly = true;
                writeKeyValueCount = Integer.parseInt(items[1]);
            } else if ("THREADS".equals(argumentName)) {
                ExperimentConfigurator.tasklets = Integer.parseInt(items[1]);
            } else if ("PROFILE_QUERY_TIME".equals(argumentName)) {
                profileQueryTime = true;
            }
        }
    }

    public static void performanceEvaluation() {
        TreeNode PIMRoot;
        TreeNode CPURoot;
        long totalTimeInMs = 0;
        int r = 0;
        List<Integer> keys = readIntergerArrayList("keys_random.txt");

        if (cpuPerformanceEvaluation) {
            try {
                CPURoot = BSTBuilder
                        .buildCpuPartTreeFromFile(imagesPath + "CPU_TREE_" + performanceEvaluationNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("begin evaluate CPU Tree 500,000 queries performance");
            for (int i = 0; i < cpuPerformanceEvaluationRepeatTime; i++) {
                long startTime = System.nanoTime();
                for (int j = 0; j < queryCount; j++) {
                    int key = keys.get(j);
                    int v = CPURoot.search(key);
                    r += v;
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + cpuPerformanceEvaluationRepeatTime
                        + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }

            System.out.println(
                    "CPU 500,000 queries average time = " + totalTimeInMs / cpuPerformanceEvaluationRepeatTime);
            System.out.println("single query average time = "
                    + totalTimeInMs / cpuPerformanceEvaluationRepeatTime / (double) keys.size());
            System.out.println("end evaluate CPU Tree 500,000 queries performance");
        }

        if (pimPerformanceEvaluation) {
            try {
                for (int i = 0; i < UPMEM.dpuInUse; i++) {
                    UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, 0, 0);
                }
                writeDPUImages(performanceEvaluationNodeCount, ExperimentConfigurator.imagesPath);
                System.out.println("load CPU part tree");
                PIMRoot = buildCpuPartTreeFromFile(imagesPath + "PIM_TREE_" + performanceEvaluationNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("begin evaluate PIM Tree 500,000 queries performance");
            totalTimeInMs = 0;

            BatchDispatcher bd1 = new BatchDispatcher();

            if (performanceEvaluationEnableBatchDispatch)
                UPMEM.beginRecordBatchDispatching(bd1);

            for (int i = 0; i < pimPerformanceEvaluationRepeatTime; i++) {
                long startTime = System.nanoTime();
                for (int j = 0; j < queryCount / 10000; j++) {
                    for (int k = 0; k < 10000; k++) {
                        int key = keys.get(k);
                        int v = PIMRoot.search(key);
                        r += v;
                    }
                    System.out.println("avg per time = " + (System.nanoTime() - startTime) / 1000000);
                }
                long endTime = System.nanoTime();
                long timeElapsed = endTime - startTime;
                System.out.println((i + 1) + "/" + pimPerformanceEvaluationRepeatTime
                        + " Execution time in milliseconds: " + timeElapsed / 1000000);
                totalTimeInMs += timeElapsed / 1000000;
            }
            if (performanceEvaluationEnableBatchDispatch)
                UPMEM.endRecordBatchDispatching();
            System.out.println(
                    "PIM 500,000 queries average time = " + totalTimeInMs / pimPerformanceEvaluationRepeatTime);
            System.out.println("single queries average time = "
                    + totalTimeInMs / pimPerformanceEvaluationRepeatTime / (double) keys.size());
            System.out.println("end evaluate PIM Tree 500,000 queries performance");
        }
    }

    public static void main(String[] args) throws RemoteException {
        // parse arguments
        parseParameters(args);
        upmemConfigurator.setDpuInUseCount(dpuInUse);

        System.out.println("dpu in use = " + dpuInUse);
        System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
        System.out.println("cpu tree layer count = " + cpuLayerCount);

        // if(noSearch)
        // System.out.println("No search mode");
        // if(buildFromSerializedData)
        // System.out.println("build tree from images, path = " + imagesPath);
        // if(serializeToFile)
        // System.out.println("output tree to file. Imgs path = " + imagesPath);
        // if(writeKeyValue){
        // System.out.println("Generate key value pairs, and write to file. Count = " +
        // writeKeyValueCount);
        // BSTTester.writeKV(writeKeyValueCount, "key_values-" + writeKeyValueCount +
        // ".txt");
        // }
        // if(writeKeyValueOnly){
        // System.out.println("Write key value pairs only, count = " +
        // writeKeyValueCount);
        // BSTTester.writeKV(writeKeyValueCount, "key_values-" + writeKeyValueCount +
        // ".txt");
        // System.out.println("Write key value pairs finish.");
        // return;
        // }

        // UPMEM configurator

        upmemConfigurator
                .setDpuInUseCount(dpuInUse)
                .setThreadPerDPU(tasklets)
                .setUseSimulator(useSimulator)
                .setEnableProfilingRPCDataMovement(true);

        // UPMEM initialization
        UPMEM.initialize(upmemConfigurator);

        // Test DPUTreeNode
        // UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(DPUTreeNode.class);
        TreeNode root = UPMEM.getInstance().getDPUManager(0).createObject(DPUTreeNode.class, 0, 0);
        System.out.println("root.value = " + root.getVal());

        // // Evaluate performance. In performance evaluation mode, the execution time
        // would be measured.
        // if(performanceEvaluationMode) {
        // performanceEvaluation();
        // return;
        // }

        // // default
        // if(args.length == 0){
        // BSTTester.evaluatePIMBST(totalNodeCount,
        // ExperimentConfigurator.queryCount,
        // ExperimentConfigurator.cpuLayerCount);
        // return;
        // }

        // // evaluate CPU Tree or PIM Tree
        // if("CPU".equals(experimentType)){
        // BSTTester.evaluateCPU(totalNodeCount, queryCount);
        // }else if("PIM".equals(experimentType)){
        // BSTTester.evaluatePIMBST(totalNodeCount, queryCount, cpuLayerCount);
        // }

        // UPMEM.reportProfiling();
        // if(UPMEM.getConfigurator().isReportProfiling()){
        // System.out.printf("Simulated data transfer from CPU to DPUs: %d bytes\n",
        // UPMEM.profiler.transferredBytesToDPU);
        // System.out.printf("Simulated data transfer from DPUs to CPU: %d bytes\n",
        // UPMEM.profiler.transferredBytesFromDPU);
        // }

    }
}
