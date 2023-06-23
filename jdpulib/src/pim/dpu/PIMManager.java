package pim.dpu;

import java.util.ArrayList;
import com.upmem.dpu.Dpu;
import com.upmem.dpu.DpuException;
import com.upmem.dpu.DpuSystem;


public class PIMManager {
    private static ArrayList<DPUManager> dpuManagers = new ArrayList<>();
    private static PIMManager instance;
    private static DpuSystem system;
    public DPUManager getDPUManager(int dpuID){
        return dpuManagers.get(dpuID);
    }
    public static PIMManager init(int dpuInUse) throws DpuException {
        if (instance == null){
            synchronized (dpuManagers){
                instance = new PIMManager();
                System.out.println("DPUSystem load " + dpuInUse + " DPUs");
                // Init DpuSystem. Allocate dpuInUses' DPUs
                system = DpuSystem.allocate(dpuInUse, "");

                // Init dpuInUses' DPU Managers
                for(int i = 0; i < dpuInUse; i++){
                    Dpu dpu = system.dpus().get(i);
                    dpu.load("dpuslave");
                    DPUManager dm = new DPUManager(dpu, i);
                    dpuManagers.add(dm);
                }
            }
        }
        return instance;
    }

    public PIMManager getInstance(){
        return instance;
    }

    private PIMManager(){

    }
}