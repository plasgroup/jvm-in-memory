package pim.dpu;

import java.util.ArrayList;
import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import com.upmem.dpu.DpuSystem;
import pim.ExperimentConfigurator;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

public abstract class PIMManager {
    protected ArrayList<DPUManager> dpuManagers = new ArrayList<>();
    protected PIMManager instance;
    protected DpuSystem system;
    public DPUManager getDPUManager(int dpuID){
        return dpuManagers.get(dpuID);
    }
    public Logger pimManagerLogger = PIMLoggers.pimManagerLogger;

    DpuSystem getDpuSystem(){
        return system;
    }

    public DpuSystem getSystem() {
        return system;
    }

    public abstract PIMManager init(int dpuInUse);

    public PIMManager getInstance(){
        return instance;
    }

    protected PIMManager(){

    }
}