package application.transplant.pimtree;

import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import simulator.PIMRemoteJVMConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static application.transplant.pimtree.PIMTreeCore.*;
import static framework.pim.ExperimentConfigurator.imagesPath;

public class PIMTreeMain {
    private static int total_communication;
    private static int total_actual_communication;
    public static int OPERATION_NR_ITEMS = 7;
    public static PIMTreeCore[] cores = new PIMTreeCore[nr_of_dpus];
    public static boolean saveKeyValue;
    public static Object keyValuePath = "./";
    public static boolean cpuOnly = false;
    private static boolean useSimulator = true;

    public static void main(String[] args) throws IOException {
        int keyCount = 2000;
        int initN = 2000;
        int testN = 1000;
        int loadBatchSize = 200;
        int executeBatchSize = 200;
        int dpuCount = 4;
        boolean  profileCPUDPUDataMovement = true;
        int threads = 1;

        if(args.length != 0){
            for(int i = 0; i < args.length; i++){
                String[] argItem = args[i].split("=");
                System.out.println(args[i]);
//                System.out.println("parse arg:" + (argItem.length > 0 ?  argItem[0] : "")
//                        + " = " + (argItem.length > 1 ? argItem[1] : ""));
                if ("KEYS_COUNT".equals(argItem[0].strip().toUpperCase())){
                    keyCount = Integer.parseInt(argItem[1].strip());
                } else if("TSK_N".equals(argItem[0].strip().toUpperCase())){
                    initN = Integer.parseInt(argItem[1].strip());
                } else if("LOAD_BATCH".equals(argItem[0].strip().toUpperCase())){
                    loadBatchSize = Integer.parseInt(argItem[1].strip());
                } else if("EXEC_BATCH".equals(argItem[0].strip().toUpperCase())){
                    executeBatchSize = Integer.parseInt(argItem[1].strip());
                } else if("DPU_COUNT".equals(argItem[0].strip().toUpperCase())){
                    dpuCount = Integer.parseInt(argItem[1].strip());
                } else if("PROF_CPUDPU_DM".equals(argItem[0].strip().toUpperCase())){
                    profileCPUDPUDataMovement = true;
                } else if("THREADS".equals(argItem[0].strip().toUpperCase())){
                    threads = Integer.parseInt(argItem[1].strip());
                } else if("SAVE_KEY_VALUE".equals(argItem[0].strip().toUpperCase())){
                    saveKeyValue = true;
                } else if("KEY_VALUE_PATH".equals(argItem[0].strip().toUpperCase())){
                    keyValuePath = Arrays.stream(argItem).skip(1).reduce((s1, s2) -> s1+s2).get().replace("\"", "");

                }else if("CPU_ONLY".equals(argItem[0].strip().toUpperCase())){
                    cpuOnly = true;
                }else if("USE_SIMULATOR".equals(argItem[0].strip().toUpperCase())){
                    if(argItem.length > 1){
                        useSimulator = Integer.parseInt(argItem[1]) != 0;
                    }else{
                        useSimulator = true;
                    }
                }
            }
        }

        PIMRemoteJVMConfiguration.JVMCount = nr_of_dpus;
        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(threads
                        ).setUseSimulator(useSimulator)
                .setUseAllowSet(true).setDpuInUseCount(dpuCount)
                .addClassesAllow("application.transplant.pimtree.PIMTreeCore",
                        "application.transplant.pimtree.PIMExecutorComputationContext")
                .setPackageSearchPath("application.transplant.pimtree.")
                .setEnableProfilingRPCDataMovement(profileCPUDPUDataMovement)
                .setCPUOnly(cpuOnly)
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


        System.out.println("Begin PIM-Tree application, key = " + keyCount
                + ", task count = " + initN + " execution batch size = " + executeBatchSize + " threads = " +
                threads + " dpus = " + dpuCount);

        frontend_by_generation frontend =
                new frontend_by_generation(initN,  testN, pos, 0,
                        loadBatchSize, executeBatchSize);

        run(frontend, loadBatchSize, executeBatchSize, keyCount);
        if(profileCPUDPUDataMovement){
            System.out.println("Simulated data transfer between CPU and DPUs:" + UPMEM.profiler.transferredBytes);
        }
    }

    static void run(frontend f, int init_batch_size, int test_batch_size, int keyCount) {

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
            executors[i] = (PIMExecutorComputationContext)
                    UPMEM.getInstance()
                            .createObject(i, PIMExecutorComputationContext.class);
        }

        try {
            PIMTreeCore.generateData(keyCount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UPMEM.profiler.resetAllCounter();

        {
            List<operation> init_ops = f.init_tasks();

            PIMTreeCore.execute(
                    make_slice(init_ops),
                    init_batch_size,
                    init_batch_size, 1);
        }

        total_communication = 0;
        total_actual_communication = 0;

        for (int i = 0; i < PIMTreeCore.num_top_level_threads; i ++) {
            pim_skip_list_drivers[i].push_pull_limit_dynamic = PIMTreeCore.push_pull_limit_dynamic;
        }


    }

}
