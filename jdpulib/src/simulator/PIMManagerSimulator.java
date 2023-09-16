package simulator;

import com.upmem.dpu.DpuException;
import pim.dpu.DPUManager;
import pim.dpu.DPUManagerUPMEM;
import pim.dpu.PIMManager;

import java.lang.module.Configuration;

public class PIMManagerSimulator extends PIMManager {
    @Override
    public PIMManager init(int dpuInUse) {
        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            System.out.println("init PIMManager Simulator " + i);
            dpuManagers.add(new DPUManagerSimulator(i));
        }

        instance = new PIMManagerSimulator();
        return instance;
    }
}
