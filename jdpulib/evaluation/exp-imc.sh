for i in 500000 1000000 2000000
do
	export EVENT_LIST="uncore_imc_0/cas_count_read/,uncore_imc_1/cas_count_read/,uncore_imc_2/cas_count_read/,uncore_imc_3/cas_count_read/,uncore_imc_4/cas_count_read/,uncore_imc_5/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_write/,uncore_imc_2/cas_count_write/,uncore_imc_3/cas_count_write/,uncore_imc_4/cas_count_write/,uncore_imc_5/cas_count_write/"
	export JAVA_RUNTIME="~/jdk-17.0.1/bin/java"
        perf stat -B -e $EVENT_LIST $JAVA_RUNTIME -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit bst-experiment-no-search.jar CPU 200000000 $i 2> "./record_files/[imc]cpu-200000000n-${i}q-prepare-only.txt"
        perf stat -B -e $EVENT_LIST $JAVA_RUNTIME -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit bst-experiment-no-search.jar PIM 200000000 $i 2> "./record_files/[imc]pim-200000000n-${i}q-prepare-only.txt"
	perf stat -B -e $EVENT_LIST $JAVA_RUNTIME -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit bst-experiment.jar CPU 200000000 $i 2> "./record_files/[imc]cpu-200000000n-${i}q.txt"
        perf stat -B -e $EVENT_LIST $JAVA_RUNTIME -jar -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit bst-experiment.jar PIM 200000000 $i 2> "./record_files/[imc]pim-200000000n-${i}q.txt"
done
