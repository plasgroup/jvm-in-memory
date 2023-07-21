#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in 1000 2000 5000 10000 20000 50000 100000 200000 500000 1000000 2000000 5000000 10000000
do
	perf record -F 20500 -B -e  cache-references,cache-misses,cycles,bus-cycles,LLC-loads,LLC-load-misses,LLC-store-misses,LLC-stores,mem_inst_retired.all_loads,mem_inst_retired.all_stores,mem-stores,mem-loads,mem_load_retired.l3_hit,mem_load_retired.l3_miss,mem_load_retired.l2_hit,mem_load_retired.l2_miss,mem_load_retired.l1_miss,mem_load_retired.l1_hit ~/jdk-17.0.1/bin/java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit jdpulib.jar CPU 2000000 $i;
	perf report -f --stdio > "./cpu-only-profile-2000000n-${i}q.txt";
done
