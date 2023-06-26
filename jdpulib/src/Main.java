import pim.UPMEMConfigurator;
import pim.algorithm.BSTBuilder;
import pim.UPMEM;


public class Main {
    public static void main(String[] args) {
        UPMEM.initialize(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        BSTBuilder.build();
    }


}