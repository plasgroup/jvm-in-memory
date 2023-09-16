package pim;

import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import com.upmem.dpu.DpuSystem;
import pim.dpu.DPUManager;
import pim.dpu.DPUManagerUPMEM;
import pim.dpu.PIMManager;

public class PIMManagerUPMEM extends PIMManager {
    @Override
    public PIMManager init(int dpuInUse) {
        if (instance == null){
            synchronized (dpuManagers){
                instance = new PIMManagerUPMEM();
                pimManagerLogger.logln("DPUSystem load " + dpuInUse + " DPUs");
                    // init DpuSystem. Allocate dpuInUses' DPUs
                    try {
                        system = DpuSystem.allocate(dpuInUse, "");
                    } catch (DpuException e) {
                        throw new RuntimeException(e);
                    }
                    // init dpuInUses' DPU Managers
                    for(int i = 0; i < dpuInUse; i++){
                        Dpu dpu = system.dpus().get(i);
                        try {
                            dpu.load("dpuslave");
                        } catch (DpuException e) {
                            throw new RuntimeException(e);
                        }
                        DPUManager dm = null;
                        try {
                            dm = new DPUManagerUPMEM(dpu, i);
                        } catch (DpuException e) {
                            throw new RuntimeException(e);
                        }
                        dpuManagers.add(dm);
                    }



            }
        }
        return instance;
    }
}
