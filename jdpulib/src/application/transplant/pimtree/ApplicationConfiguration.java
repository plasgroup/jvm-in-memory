package application.transplant.pimtree;

public class ApplicationConfiguration {
    public int dpuInUse = 64;
    public static final int DB_SIZE = 16;  // size of the data block
    public int getDpuInUse() {
        return dpuInUse;
    }

    public ApplicationConfiguration setDpuInUse(int dpuInUse) {
        this.dpuInUse = dpuInUse;
        return this;
    }


}
