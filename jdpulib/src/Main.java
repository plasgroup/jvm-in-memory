import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import simulator.DPUJVMRemote;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import static pim.ExperimentConfigurator.*;
import static pim.algorithm.BSTBuilder.buildCpuPartTreeFromFile;
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
                buildFromSerializedData = true;
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
            }

        }
    }

    public static void performanceEvaluation(){
        TreeNode PIMRoot;
        TreeNode CPURoot;
        long totalTimeInMs = 0;
        List<Integer> keys = readIntergerArrayList("keys_random.txt");

        if (cpuPerformanceEvaluation) {
            try {
                CPURoot = BSTBuilder.buildCpuPartTreeFromFile("CPU_TREE_" + performanceEvaluationNodeCount + ".txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("begin evaluate CPU Tree 500,000 queries performance");
            for (int i = 0; i < cpuPerformanceEvaluationRepeatTime; i++) {
                long startTime = System.nanoTime();
                for(int j = 0; j < queryCount; j++){
                    int key = keys.get(j);
                    int v = CPURoot.search(key);
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


            System.out.println("begin evaluate PIM Tree 500,000 queries performance");
            totalTimeInMs = 0;

            BatchDispatcher bd1 = new BatchDispatcher();

            if(performanceEvaluationEnableBatchDispatch)
                UPMEM.beginRecordBatchDispatching(bd1);
            for (int i = 0; i < pimPerformanceEvaluationRepeatTime; i++) {
                long startTime = System.nanoTime();
                for(int j = 0; j < queryCount; j++){
                    int key = keys.get(j);
                    int v = PIMRoot.search(key);
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
        UPMEM.initialize(upmemConfigurator);
        //if(true) return;
        UPMEM.getInstance().createObject(0, TreeNode.class, 123, 424);
        if(true) return;
        Registry registry = LocateRegistry.getRegistry("localhost", 9239 + 5);
        TNodeProxy tp = new TNodeProxy();

        tp.getKey();
//        upmemConfigurator.setDpuInUseCount(dpuInUse);
//
//        System.out.println("dpu in use = " + dpuInUse);
//        System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
//        System.out.println("cpu tree layer count = " + cpuLayerCount);
//        if(noSearch) System.out.println("No search mode");
//        if(buildFromSerializedData) System.out.println("build tree from images, path = " + imagesPath);
//        if(serializeToFile) System.out.println("output tree to file. Imgs path = " + imagesPath);
//        if(writeKeyValue){
//            System.out.println("Generate key value paris, and write to file. Count = " + writeKeyValueCount);
//            writeKV(writeKeyValueCount, "key_values-" + writeKeyValueCount + ".txt");
//        }
//
//        UPMEM.initialize(upmemConfigurator);
//
//        upmemConfigurator
//                .setDpuInUseCount(dpuInUse)
//                .setThreadPerDPU(UPMEM.perDPUThreadsInUse);
//
//        if(performanceEvaluationMode) {
//            performanceEvaluation();
//            return;
//        }
//
//        if(args.length == 0){
//            BSTTester.evaluatePIMBST(totalNodeCount, ExperimentConfigurator.queryCount,  ExperimentConfigurator.cpuLayerCount);
//            return;
//        }
//
//        if("CPU".equals(experimentType)){
//            BSTTester.evaluateCPU(totalNodeCount, queryCount);
//        }else if("PIM".equals(experimentType)){
//            BSTTester.evaluatePIMBST(totalNodeCount, queryCount, cpuLayerCount);
//        }

    }
}


class TNodeProxy extends TNode{
    int dpuID = 4;
    int dpuAddress = 5;

    @Override
    public int getKey() {
        Registry registry;
        try {

            // 1. Find DPU
            // 2. send argument and method/class pt to queue.
            // 3. compile method at the first time
            // 4. send binary if needed
            // 5. launch processor for execution
               // 5.1 server get instance and method structure, execution;
            // 6. finish the execution
            System.out.println("1. Find DPU " + dpuID);
            registry = LocateRegistry.getRegistry("localhost", 9239 + dpuID);
            DPUJVMRemote DPUJVMRemote = (DPUJVMRemote) registry.lookup("DPUJVM" + dpuID);
            System.out.println("get JVM id = " + DPUJVMRemote.getID());
            System.out.println("2. send argument and method/class pt to queue.");
            DPUJVMRemote.setParameter(0, 0);
            DPUJVMRemote.setParameter(1, 1);
            DPUJVMRemote.setParameter(2, 0);
            DPUJVMRemote.pushToMetaSpace(TNode.class);
            DPUJVMRemote.pushToMetaSpace(TNode.class, "getKey");
            DPUJVMRemote.pushObject(0, new TNode());
            DPUJVMRemote.start();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
