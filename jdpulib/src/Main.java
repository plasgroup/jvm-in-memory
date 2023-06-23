import com.upmem.dpu.DpuException;
import pim.UPMEMConfigurator;
import pim.algorithm.BSTBuilder;
import pim.UPMEM;


public class Main {
    public static void main(String[] args) {
        UPMEM.init(new UPMEMConfigurator()
                .setDpuInUseCount(UPMEM.TOTAL_DPU_COUNT)
                .setThreadPerDPU(UPMEM.perDPUThreadsInUse));

        BSTBuilder.build();
    }


}