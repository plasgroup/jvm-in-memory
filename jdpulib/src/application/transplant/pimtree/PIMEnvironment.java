package application.transplant.pimtree;

public class PIMEnvironment {
    public static int[] wram_save_pos;

    public static PIMTreeExecutor[] executors;
    public static int dpuInUse = 64;

    public PIMEnvironment(ApplicationConfiguration applicationConfiguration) {
        wram_save_pos = new int[applicationConfiguration.getDpuInUse()];
        executors = new PIMTreeExecutor[applicationConfiguration.getDpuInUse()];
        this.dpuInUse = applicationConfiguration.getDpuInUse();
    }

    public static void init_wram_save_pos() {
        for (int i = 0; i < dpuInUse; i++)
            wram_save_pos[i] = 0;
    }
}
