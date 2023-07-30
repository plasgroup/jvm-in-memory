#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in 1000 2000 5000 10000 20000 50000 100000 200000 500000 1000000 2000000 5000000 10000000
do
	perf record -F 20500 -B -e LLC-load-misses,LLC-loads,mem-loads,mem-stores,L1-dcache-stores,LLC-store-misses,offcore_response.pf_l3_data_rd.any_response,mem_load_l3_miss_retired.local_dram,mem_load_l3_miss_retired.remote_dram,offcore_requests.all_data_rd,offcore_response.pf_l3_rfo.any_response,offcore_requests.l3_miss_demand_data_rd,offcore_response.all_data_rd.l3_miss.any_snoop,offcore_response.pf_l3_rfo.l3_miss.snoop_miss_or_no_fwd,offcore_response.pf_l3_data_rd.l3_miss.snoop_miss_or_no_fwd,offcore_response.demand_rfo.l3_miss.any_snoop,offcore_response.demand_data_rd.l3_miss_local_dram.snoop_miss_or_no_fwd ~/jdk-17.0.1/bin/java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit jdpulib.jar CPU 2000000 $i;
	perf report -f --stdio > "./cpu-only-profile-2000000n-${i}q.txt";
	perf script --itrace | grep 'search' >  "cpu-only-search-samples-${i}q.txt"; 
done
