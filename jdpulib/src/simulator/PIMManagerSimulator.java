package simulator;

import framework.pim.dpu.PIMManager;

public class PIMManagerSimulator extends PIMManager {
    @Override
    public PIMManager init(int dpuInUse) {
        for(int i = 0; i < dpuInUse; i++){
            System.out.println("init DPU JVM simulator " + i + "/" + dpuInUse);
            dpuManagers.add(new DPUManagerSimulator(i));
        }
        instance = this;
        return instance;
    }
}
