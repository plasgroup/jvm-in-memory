import pim.ExperimentConfigurator;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;

import java.io.*;
import java.util.*;

import static pim.ExperimentConfigurator.*;

public class Main {
    public static void writeKV(int count){
        try {
            int B = 20;
            int batch = Integer.MAX_VALUE / B;
            FileOutputStream fos = new FileOutputStream("key_values-" + count + ".txt");
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


    public static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();

    public static void parseParameters(String[] args){
        if(args.length >= 3){
            experimentType = args[0];
            totalNodeCount = Integer.parseInt(args[1]);
            ExperimentConfigurator.queryCount = Integer.parseInt(args[2]);
            if(args.length >= 4 && "NO_SEARCH".equals(args[3])){
                noSearch = true;
            }
            if(args.length >= 5){
                dpuInUse =  Integer.parseInt(args[4]);
            }
            if(args.length >= 6){
                ExperimentConfigurator.cpuLayerCount = Integer.parseInt(args[5]);
            }
            if(args.length >= 7){
                String[] options = args[6].split(",");
                HashSet<String> hashSet = new HashSet<>();
                for(String s : options) hashSet.add(s);
                if(hashSet.contains("IMG")){
                    ExperimentConfigurator.buildFromSerializedData = true;
                }
                if(hashSet.contains("SERIALIZE")){
                    ExperimentConfigurator.serializeToFile = true;
                }
                if(hashSet.contains("NO_SEARCH")){
                    noSearch = true;
                }
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

        UPMEM.initialize(upmemConfigurator);

        upmemConfigurator
                .setDpuInUseCount(dpuInUse)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse);

        if(args.length < 3){
            BSTTester.evaluatePIMBST(totalNodeCount,  ExperimentConfigurator.queryCount,  ExperimentConfigurator.cpuLayerCount);
            //BSTTester.evaluateCPU(totalNodeCount, queryCount);
            return;
        }

        if("CPU".equals(experimentType)){
            BSTTester.evaluateCPU(totalNodeCount, queryCount);
        }else if("PIM".equals(experimentType)){
            BSTTester.evaluatePIMBST(totalNodeCount, queryCount, cpuLayerCount);
        }
    }
}