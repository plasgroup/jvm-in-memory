package application.transplant.pimtree;



//import static pimtree.ComputationTransitionHelper.init_dpus;
import framework.pim.UPMEM;

import java.util.ArrayList;
import java.util.List;

import static application.transplant.pimtree.ProgramArgs.L2_SIZE;


public class Main {

    private static int total_communication;
    private static int total_actual_communication;
    public static pim_skip_list[] pim_skip_list_drivers;
    public static int OPERATION_NR_ITEMS = 7;


    static void run(frontend f, int init_batch_size, int test_batch_size) {
        pim_skip_list_drivers = new pim_skip_list[core.num_top_level_threads];
        pim_skip_list_drivers[0].init();

        {
            List<operation> init_ops = f.init_tasks();
            // pu_coverage_timer->reset();
            // pim_coverage_timer->reset();
            core.execute(make_slice(init_ops), init_batch_size,
                init_batch_size, 1);
        }
        total_communication = 0;
        total_actual_communication = 0;

        for (int i = 0; i < core.num_top_level_threads; i ++) {
            pim_skip_list_drivers[i].push_pull_limit_dynamic = core.push_pull_limit_dynamic;
        }

//        dpu_energy_stats(false);
//        reset_all_timers();
        {
            List<operation> test_ops = f.test_tasks();
//            cpu_coverage_timer->reset();
//            pim_coverage_timer->reset();


            core.execute(make_slice(test_ops), test_batch_size, test_batch_size, core.num_top_level_threads);



//            print_all_timers(print_type::pt_full);
//            print_all_timers(print_type::pt_name);
//            print_all_timers(print_type::pt_time);
//            print_all_timers_average();
//            cpu_coverage_timer->print(pt_full);
//            pim_coverage_timer->print(pt_full);

        }

        // dpu_energy_stats(false);
        // delete[] pim_skip_list_drivers;
    }

    public static <T> List<T> make_slice(List<T> initOps) {
        return initOps;
    }

    static void exec(int argc, String argv[]) {
//        auto program = parser();
//        try {
//            program.parse_args(argc, argv);
//        } catch (const std::runtime_error& err) {
//            std::cerr << err.what() << std::endl;
//            std::cerr << program;
//            std::exit(1);
//        }
//
//        parlay::sequence<double> pos(OPERATION_NR_ITEMS, 0.0);
//        pos[1] = program.get<double>("-g");
//        pos[2] = program.get<double>("-u");
//        pos[3] = program.get<double>("-p");
//        pos[4] = program.get<double>("-s");
//        pos[5] = program.get<double>("-i");
//        pos[6] = program.get<double>("-r");
        List<Double> pos = new ArrayList<>(OPERATION_NR_ITEMS);
//
//        core::check_result = (program["--nocheck"] == false);
//        timer::print_when_time = (program["--noprint"] == false);
//        timer::default_detail = (program["--nodetail"] == false);
        int bias = 1; // program.get<int>("--bias");
        core.push_pull_limit_dynamic = L2_SIZE;// program.get<int>("--push_pull_limit_dynamic");

        core.num_top_level_threads = 1; //program.get<int>("--top_level_threads");

        core.num_wait_microsecond = 0; //program.get<int>("--wait_microsecond");
//        ASSERT(core::num_top_level_threads >= 1);
//        ASSERT(core::num_wait_microsecond >= 0);
        System.out.println("thread: " + core.num_top_level_threads); // << endl;
        System.out.println("wait: " + core.num_wait_microsecond + " microsecond");

        double alpha = 0.6; //program.get<double>("--alpha");
        // auto files = program.get<vector<string>>("-f");
        // auto output_file = program.get<vector<string>>("-o");
        //auto ns = program.get<vector<int>>("-l");
        // assert(ns.size() == 2);
        int init_n = 400; // ns[0];
        int test_n = 200; // ns[1];
        int init_batch_size = 400; //program.get<int>("--init_batch_size");
        int test_batch_size = 200; //program.get<int>("--test_batch_size");
        int output_batch_size = 200; //program.get<int>("--output_batch_size");


//
//        if (program.is_used("--generate_all_test_cases") == true) {
//            cout << "start generating all tests" << endl;
//            string init_file = program.get<string>("--generate_all_test_cases");
//            frontend_testgen frontend(init_n, test_n, move(pos), bias,
//                    init_file, "", output_batch_size);
//            frontend.generate_all_test();
//        } else if (files.size() > 0) {  // test from file
//            assert(files.size() == 2);
//            printf("Test from file: [%s] [%s]\n", files[0].c_str(),
//                    files[1].c_str());
//            int in = -1, tn = -1;
//            if (program.is_used("-l")) {
//                in = ns[0];
//                tn = ns[1];
//            }
//            frontend_by_file frontend(files[0], files[1], in, tn);
//            run(frontend, init_batch_size, test_batch_size);
//        } else if (output_file.size() > 0) {  // print test file
//            assert(output_file.size() == 2);
//            printf("To generated file:\n");
//            for (int i = 1; i < OPERATION_NR_ITEMS; i++) {
//                printf("pos[%d]=%lf\n", i, pos[i]);
//            }
//            printf("\n");
//
//            frontend_testgen frontend(init_n, test_n, move(pos), bias,
//                    output_file[0], output_file[1],
//                    output_batch_size);
//            frontend.write_file();
//        } else {  // in memory test
            System.out.println("Test with generated data:\n");
            for (int i = 1; i < OPERATION_NR_ITEMS; i++) {
                System.out.printf("pos[%d]=%lf\n", i, pos.get(i));
            }
            System.out.println("\n");

//            auto ns = program.get<vector<int>>("-l");
//            assert(ns.size() == 2);
            init_n = 400; //ns[0];
            test_n = 200; //ns[1];
            frontend_by_generation frontend = new frontend_by_generation(init_n, test_n, pos, bias,
                    init_batch_size, test_batch_size);
            run(frontend, init_batch_size, test_batch_size);
//        }
//
//        // print_all_timers(print_type::pt_full);
//        // print_all_timers(print_type::pt_time);
//        // print_all_timers(print_type::pt_name);
//        cout << "total communication" << total_communication.load() << endl;
//        cout << "total actual communication"
//                << total_actual_communication.load() << endl;
    }

    static PIMExecutorComputationContext[] computationContext;
    public static void main(String[] args){

        computationContext = new PIMExecutorComputationContext[UPMEM.dpuInUse];
        for(int i = 0; i < computationContext.length; i++){
            computationContext[i] = new PIMExecutorComputationContext();
        }

        List<Double> pos = new ArrayList<>(OPERATION_NR_ITEMS);
        pos.set(0, 10.0);
        pos.set(0, 20.0);
        pos.set(0, 30.0);
        pos.set(0, 40.0);
        pos.set(0, 50.0);
        pos.set(0, 60.0);

        frontend_by_generation frontend = new frontend_by_generation(200, 100, pos, 0, 20, 20);
        run(frontend, 200, 100);

    }
}
