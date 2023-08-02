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




    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        int totalNodeCount = 100000000;
        int queryCount = 100000;
        String experimentType = "CPU";
        if(args.length >= 3){
            experimentType = args[0];
            totalNodeCount = Integer.parseInt(args[1]);
            queryCount = Integer.parseInt(args[2]);
            if(args.length >= 4 && "NO_SEARCH".equals(args[3])){
                BSTTester.noSearch = true;
            }
        }else{
            BSTTester.evaluateLargeBST(totalNodeCount, queryCount);
        }

        if("CPU".equals(experimentType)){
            BSTTester.evaluateCPU(totalNodeCount, queryCount);
        }else if("PIM".equals(experimentType)){
            BSTTester.evaluateLargeBST(totalNodeCount, queryCount);
        }
    }


}