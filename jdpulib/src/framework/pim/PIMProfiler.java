package framework.pim;

public class PIMProfiler {
    public long transferredBytes = 0;

    public void resetAllCounter(){
        this.transferredBytes = 0;
    }

    public void reportProfiledData() {
        System.out.printf("Simulated data transfer between CPU and DPUs: %d bytes\n", transferredBytes);
    }
}
