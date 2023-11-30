package application.transplant.pimtree;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;

import java.util.ArrayList;
import java.util.List;

import static application.transplant.pimtree.PIMTreeCore.*;

public class PIMTreeMain {

    private static int total_communication;
    private static int total_actual_communication;
    public static int OPERATION_NR_ITEMS = 7;

    public static void main(String[] args){
        ExperimentConfigurator.useSimulator = true;
        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(1).setDpuInUseCount(64));


        for(int i = 0; i < executors.length; i++){
            executors[i] = new PIMTreeExecutor();
        }

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

        frontend_by_generation frontend = new frontend_by_generation(200, 100, pos, 0, 20, 20);
        run(frontend, 200, 100);

    }

    static void run(frontend f, int init_batch_size, int test_batch_size) {
        pim_skip_list_drivers = new pim_skip_list[PIMTreeCore.num_top_level_threads];
        for(int i = 0; i < PIMTreeCore.num_top_level_threads; i++){
            pim_skip_list_drivers[i] = new pim_skip_list();
        }
        pim_skip_list_drivers[0].init();

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
