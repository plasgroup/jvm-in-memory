package framework.pim;

public class PIMProfiler {
    public long transferredBytesToDPU = 0;
    public long transferredBytesFromDPU = 0;


    public void resetAllCounter(){
        this.transferredBytesToDPU = 0;
        this.transferredBytesFromDPU = 0;
    }

    public void reportProfiledData() {
        System.out.printf("Simulated data transfer from CPU to DPUs: %d bytes\n", transferredBytesToDPU);
        System.out.printf("Simulated data transfer from DPUs to CPU: %d bytes\n", transferredBytesFromDPU);
    }
}
