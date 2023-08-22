import com.upmem.dpu.DpuException;
import pim.BatchDispatcher;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.UPMEMConfigurator;
import pim.algorithm.BSTTester;
import pim.algorithm.DPUTreeNode;
import pim.algorithm.TreeNode;

import java.util.Arrays;

import static pim.ExperimentConfigurator.*;
import static pim.algorithm.BSTTester.writeKV;

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


        TreeNode tn = (TreeNode) UPMEM.getInstance().createObject(0, DPUTreeNode.class, 10,22231);
        UPMEM.beginRecordBatchDispatching(new BatchDispatcher());
        for(int i = 0; i < 100; i++){
            tn.search(10);
        }
        UPMEM.endRecordBatchDispatching();
        try {
            UPMEM.batchDispatcher.dispatchAll();
        } catch (DpuException e) {
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

