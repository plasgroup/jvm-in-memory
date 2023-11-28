package pim.dpu;

import com.upmem.dpu.DpuSystem;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.util.ArrayList;

public abstract class PIMManager {
    protected final ArrayList<DPUManager> dpuManagers = new ArrayList<>();
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