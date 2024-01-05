package framework.pim;


import java.util.HashSet;

/**
 *      UPMEM Configurator Class.
 *      An UPMEM instance can be created by a UPMEM configurator, using UPMEM.init(upmemConfigurator);
 * **/
public class UPMEMConfigurator {
    private int dpuInUseCount = UPMEM.TOTAL_DPU_COUNT;
    private int threadPerDPU = UPMEM.TOTAL_HARDWARE_THREADS_COUNT;
    private boolean useSimulator = false;

    private String packageSearchPath = "";
    private HashSet<String> allowSet = new HashSet<>();
    private boolean useAllowSet;

    public boolean isUseSimulator() {
        return useSimulator;
    }

    public UPMEMConfigurator setUseSimulator(boolean useSimulator) {
        this.useSimulator = useSimulator;
        return this;
    }

    public UPMEMConfigurator setUseAllowSet(boolean useAllowSet) {
        this.useAllowSet = useAllowSet;
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

    public UPMEMConfigurator setPackageSearchPath(String packageSearchPath) {
        this.packageSearchPath = packageSearchPath;
        return this;
    }

    public String getPackageSearchPath() {
        return packageSearchPath;
    }
    public UPMEMConfigurator addClassesAllow(String... classes){
        for(String className : classes){
            allowSet.add(className);
        }
        return this;
    }
    public HashSet<String> getAllowSet() {
        return allowSet;
    }

    public boolean isUseAllowSet() {
        return useAllowSet;
    }
}
