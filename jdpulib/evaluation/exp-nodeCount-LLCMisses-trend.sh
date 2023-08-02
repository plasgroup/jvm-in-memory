for i in 2000000 10000000 100000000 200000000
do
	perf record -B -e LLC-load-misses,LLC-loads,mem-loads,mem-stores,L1-dcache-stores,LLC-store-misses,offcore_response.pf_l3_data_rd.any_response,mem_load_l3_miss_retired.local_dram,mem_load_l3_miss_retired.remote_dram,offcore_requests.all_data_rd,offcore_response.pf_l3_rfo.any_response,offcore_requests.l3_miss_demand_data_rd,offcore_response.all_data_rd.l3_miss.any_snoop,offcore_response.pf_l3_rfo.l3_miss.snoop_miss_or_no_fwd,offcore_response.pf_l3_data_rd.l3_miss.snoop_miss_or_no_fwd,offcore_response.demand_rfo.l3_miss.any_snoop,offcore_response.demand_data_rd.l3_miss_local_dram.snoop_miss_or_no_fwd java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit bst-experiment.jar CPU $i 1000000
        perf report -f --stdio > "./record_files/cpu-only-profile-${i}n-1000000q.txt";
	perf script --itrace | grep 'search(int)' >  "./record_files/[q]cpu-only-search-samples-${i}q.txt";
done