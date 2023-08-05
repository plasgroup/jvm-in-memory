import com.upmem.dpu.DpuException;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.logger.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {


    // 明天要做的实验 -
    //   - 2 oku jiedian xia CPU DPU version comparasion



//    public static void writeKV(int count){
//        try {
//            int B = 20;
//            int target = 200000000;
//            int batch = target / B;
//            FileOutputStream fos = new FileOutputStream("key_values-200M.txt");
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//            OutputStreamWriter osw = new OutputStreamWriter(bos);
//            for(int i = 0; i < target; i += batch){
//                ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs =
//                        new IntIntValuePairGenerator(i, i + batch).generatePairs(batch);
//                for(BSTBuilder.Pair<Integer, Integer> pair : pairs){
//                    osw.write(pair.getKey() + " " + pair.getVal() + "\n");
//                }
//            }
//
//            osw.close();
//            bos.close();
//            fos.close();
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
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

    static int totalNodeCount = 10000000;
    static int queryCount = 100000;
    static int dpuInUse = 64;
    static int cpuLayerCount = 18;
    static String experimentType = "CPU";
    static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();

    public static void parseParameters(String[] args){
        if(args.length >= 3){
            experimentType = args[0];
            totalNodeCount = Integer.parseInt(args[1]);
            queryCount = Integer.parseInt(args[2]);
            if(args.length >= 4 && "NO_SEARCH".equals(args[3])){
                BSTTester.noSearch = true;
            }
            if(args.length >= 5){
                dpuInUse =  Integer.parseInt(args[4]);
            }
            if(args.length >= 6){
                cpuLayerCount = Integer.parseInt(args[5]);
            }
        }
    }

    public static void main(String[] args) {
        if(args.length < 3){
            BSTTester.evaluateLargeBST(totalNodeCount, queryCount, cpuLayerCount);
            return;
        }

        parseParameters(args);
        upmemConfigurator.setDpuInUseCount(dpuInUse);

        System.out.println("dpu in use = " + dpuInUse);
        System.out.println(experimentType + " mode, nodes count = " + totalNodeCount + " query count = " + queryCount);
        System.out.println("cpu tree layer count = " + cpuLayerCount);
        if(BSTTester.noSearch) System.out.println("No search mode");

        UPMEM.initialize(upmemConfigurator);

        upmemConfigurator
                .setDpuInUseCount(dpuInUse)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse);

        if("CPU".equals(experimentType)){
            BSTTester.evaluateCPU(totalNodeCount, queryCount);
        }else if("PIM".equals(experimentType)){
            BSTTester.evaluateLargeBST(totalNodeCount, queryCount, cpuLayerCount);
        }
    }


}