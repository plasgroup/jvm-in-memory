package simulator;

import pim.dpu.PIMManager;

public class PIMManagerSimulator extends PIMManager {
    @Override
    public PIMManager init(int dpuInUse) {
        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            System.out.println("init DPU JVM simulator " + i);
            dpuManagers.add(new DPUManagerSimulator(i));
        }
        instance = this;
        return instance;
    }
}
