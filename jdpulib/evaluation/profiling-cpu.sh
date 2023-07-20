#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in 1000 2000 5000 10000 20000 50000 100000 200000 500000 1000000 2000000 5000000 10000000
do
	perf record -B -e cache-references,cache-misses,cycles,instructions,branches,faults,migrations java -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit jdpulib.jar CPU 2000000 $i
	sudo perf report -f --stdio > "./cpu-only-profile-2000000n-${i}q.txt";
done
