package application.transplant.pimtree;

import com.upmem.dpu.DpuException;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import simulator.PIMRemoteJVMConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static application.transplant.pimtree.PIMTreeCore.*;

public class PIMTreeMain {
    private static int total_communication;
    private static int total_actual_communication;
    public static int OPERATION_NR_ITEMS = 7;
    public static PIMTreeCore[] cores = new PIMTreeCore[nr_of_dpus];


    public static void main(String[] args) throws IOException {
        PIMRemoteJVMConfiguration.JVMCount = nr_of_dpus;
        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(1).setDpuInUseCount(64).setUseSimulator(true)
                .setUseAllowSet(true)
                .addClassesAllow("application.transplant.pimtree.PIMTreeCore", "application.transplant.pimtree.PIMExecutorComputationContext")
                .setPackageSearchPath("application.transplant.pimtree.")
        );


        List<Double> pos = new ArrayList<>(OPERATION_NR_ITEMS);
        for(int i = 0; i < OPERATION_NR_ITEMS; i++)
        {
            pos.add(0.0);
        }
        pos.set(0, 10.0);
        pos.set(1, 20.0);
        pos.set(2, 30.0);
        pos.set(3, 40.0);
        pos.set(4, 50.0);
        pos.set(5, 60.0);

        frontend_by_generation frontend = new frontend_by_generation(20000, 100, pos, 0, 20, 20);
        run(frontend, 200, 100);

    }

    static void run(frontend f, int init_batch_size, int test_batch_size) {
        // top-level threads, which is host << -

        pim_skip_list_drivers = new pim_skip_list[PIMTreeCore.num_top_level_threads];


        // each host thread hold a pim_skip_list
        // in core::execute(). each core load batch
        // only one thread can execute run_batch()
        for(int i = 0; i < PIMTreeCore.num_top_level_threads; i++){
            pim_skip_list_drivers[i] = new pim_skip_list();
        }

        for(int i = 0; i < num_top_level_threads; i++){
            pim_skip_list_drivers[0].init();
        }

        for(int i = 0; i < nr_of_dpus; i++){
            executors[i] = (PIMExecutorComputationContext) UPMEM.getInstance().createObject(i, PIMExecutorComputationContext.class);
        }

        PIMTreeCore.generateData(2000);

        {
            List<operation> init_ops = f.init_tasks();

            PIMTreeCore.execute(make_slice(init_ops), init_batch_size,
                    init_batch_size, 1);
        }
        total_communication = 0;
        total_actual_communication = 0;

        for (int i = 0; i < PIMTreeCore.num_top_level_threads; i ++) {
            pim_skip_list_drivers[i].push_pull_limit_dynamic = PIMTreeCore.push_pull_limit_dynamic;
        }

        {


            List<operation> test_ops = f.test_tasks();


            PIMTreeCore.execute(make_slice(test_ops), test_batch_size, test_batch_size, PIMTreeCore.num_top_level_threads);


        }

    }

}
