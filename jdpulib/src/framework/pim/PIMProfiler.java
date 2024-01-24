package framework.pim;

public class PIMProfiler {
    public long transferredBytesToDPU = 0;
    public long transferredBytesFromDPU = 0;


    public void resetAllCounter(){
        this.transferredBytesToDPU = 0;
        this.transferredBytesFromDPU = 0;
    }

    public void reportProfiledData() {
        System.out.println("Simulated data transfer from CPU to DPUs: " + transferredBytesToDPU + " bytes");
        System.out.println("Simulated data transfer from CPU to DPUs: " + transferredBytesFromDPU + " bytes");

    }
}
