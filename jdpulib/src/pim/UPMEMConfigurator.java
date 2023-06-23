package pim;

public class UPMEMConfigurator {
    private int dpuInUseCount = UPMEM.TOTAL_DPU_COUNT;
    private int threadPerDPU = UPMEM.TOTAL_HARDWARE_THREADS_COUNT;

    public int getDpuInUseCount() {
        return dpuInUseCount;
    }

    public int getThreadPerDPU() {
        return threadPerDPU;
    }

    public UPMEMConfigurator setDpuInUseCount(int dpuInUseCount){
        this.dpuInUseCount = dpuInUseCount;
        return this;
    }
    public UPMEMConfigurator setThreadPerDPU(int threadPerDPU){
        this.threadPerDPU = threadPerDPU;
        return this;
    }
}
