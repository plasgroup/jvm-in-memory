#!/bin/bash
export SCRIPT_DIR="$(pwd)"
#for i in 100 2000 5000 10000 20000 50000 100000 200000 500000 1000000 2000000 5000000 10000000
for i in 1000 2000 5000 10000 20000 50000 100000 200000 500000 1000000
do
	perf record -F 20500 -B -e UNC_M_CAS_COUNT.RD[0] ~/jdk-17.0.1/bin/java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit jdpulib.jar PIM 2000000 $i;
	perf report -f --stdio > "./pim-profile-2000000n-${i}q.txt";
	perf script --itrace | grep 'search' >  "pim-search-samples-${i}q.txt";
done
