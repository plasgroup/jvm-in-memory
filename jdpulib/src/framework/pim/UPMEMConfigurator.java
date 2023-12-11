package framework.pim;


/**
 *      UPMEM Configurator Class.
 *      An UPMEM instance can be created by a UPMEM configurator, using UPMEM.init(upmemConfigurator);
 * **/
public class UPMEMConfigurator {
    private int dpuInUseCount = UPMEM.TOTAL_DPU_COUNT;
    private int threadPerDPU = UPMEM.TOTAL_HARDWARE_THREADS_COUNT;
    private boolean useSimulator = false;

    public boolean isUseSimulator() {
        return useSimulator;
    }

    public UPMEMConfigurator setUseSimulator(boolean useSimulator) {
        this.useSimulator = useSimulator;
        return this;
    }

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
