#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in 100 2000 5000 10000 20000 50000 100000 200000 500000 1000000 2000000 5000000 10000000
do
	perf record -B -e cache-references,cache-misses,cycles,instructions,branches,faults,migrations,bus_cycles,LLC-loads,LLC-load-misses,LLC-store-misses,LLC-stores,mem_inst_retired.all_loads,mem_inst_retired.all_stores,mem_inst_retired.l3_hit,mem_inst_retired.l3_miss,mem_inst_retired.l2_hit,mem_inst_retired.l2_misses,mem_inst_retired.l1_misses,mem_inst_retired.l1_hit java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit jdpulib.jar PIM 2000000 $i
	perf report -f --stdio > "./pim-profile-2000000n-${i}q.txt";
done
