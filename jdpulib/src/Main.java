import com.upmem.dpu.DpuException;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.logger.Logger;

import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        int totalNodeCount = 2000;
        int queryCount = 1000;
        String experimentType = "CPU";
        if(args.length >= 3){
            experimentType = args[0];
            totalNodeCount = Integer.parseInt(args[1]);
            queryCount = Integer.parseInt(args[2]);
        }
        if("CPU".equals(experimentType)){
            BSTTester.evaluateCPU(totalNodeCount, queryCount);
        }else if("PIM".equals(experimentType)){
            BSTTester.evaluateLargeBST(totalNodeCount, queryCount);
        }

        BSTTester.evaluateLargeBST(totalNodeCount, queryCount);

    }


}