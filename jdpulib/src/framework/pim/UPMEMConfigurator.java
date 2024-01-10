package framework.pim;


import java.util.Collections;
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
    private boolean enableProfilingRPCDataMovement = false;
    private boolean reportProfiling = false;
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
        Collections.addAll(allowSet, classes);
        return this;
    }
    public HashSet<String> getAllowSet() {
        return allowSet;
    }
    public boolean isUseAllowSet() {
        return useAllowSet;
    }
    public boolean isEnableProfilingRPCDataMovement() {
        return enableProfilingRPCDataMovement;
    }
    public UPMEMConfigurator setEnableProfilingRPCDataMovement(boolean enableProfilingRPCDataMovement) {
        this.enableProfilingRPCDataMovement = enableProfilingRPCDataMovement;
        return this;
    }
    public boolean isReportProfiling() {
        return reportProfiling;
    }
    public UPMEMConfigurator setReportProfiling(boolean reportProfiling) {
        this.reportProfiling = reportProfiling;
        return this;
    }
}
