import framework.pim.BatchDispatcher;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import application.bst.NBodySystem;
import application.bst.NBodySystemProxy;
import application.bst.Body;
import application.bst.Sqrt;

import framework.pim.dpu.classloader.ClassWriter;
import framework.primitive.control.ControlPrimitives;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static framework.pim.ExperimentConfigurator.*;

/**
 * =======================================================================
 * This is a program for evaluating N-Body Simulation (NBody) application
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

    public static void main(String[] args) throws RemoteException {
        // parse arguments
        parseParameters(args);
        upmemConfigurator.setDpuInUseCount(dpuInUse);

        System.out.println("dpu in use = " + dpuInUse);
        System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
        System.out.println("cpu tree layer count = " + cpuLayerCount);

        // UPMEM configurator

        upmemConfigurator
                .setDpuInUseCount(1)
                .setThreadPerDPU(1)
                .setUseSimulator(false)
                .setEnableProfilingRPCDataMovement(false)
                // .setPrintStream(System.out);
                .setPrintStream(null);

        // UPMEM initialization
        UPMEM.initialize(upmemConfigurator);
        UPMEM.setPackageSearchPath("application.bst.");

        // Test DPUTreeNode
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Body.class);
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Sqrt.class);
        NBodySystemProxy nbs = (NBodySystemProxy) UPMEM.getInstance().createObject(0, NBodySystem.class);
        nbs.advance(0.01f);
        System.out.println(nbs.energy());
    }
}
