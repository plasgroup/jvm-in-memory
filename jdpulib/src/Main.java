import com.sun.source.tree.Tree;
import com.upmem.dpu.DpuException;
import pim.UPMEMConfigurator;
import pim.algorithm.*;
import pim.UPMEM;
import pim.dpu.DPUCacheManager;
import pim.dpu.DPUGarbageCollector;
import pim.logger.Logger;
import pim.utils.BytesUtils;

import java.io.IOException;
import java.util.*;

import static pim.algorithm.BSTBuilder.buildLargePIMTree;
import static pim.algorithm.TreeWriter.verifyLargePIMTree;


public class Main {




    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        Logger.disableAllBeginWith("pim");

        int totalNodeCount = 2000;
        int queryCount = 1000;
        if(args.length >= 2){
            totalNodeCount = Integer.parseInt(args[0]);
            queryCount = Integer.parseInt(args[1]);
        }
        BSTTester.evaluateCPU(totalNodeCount, queryCount);
        //BSTTester.testLargeBST();
    }


}