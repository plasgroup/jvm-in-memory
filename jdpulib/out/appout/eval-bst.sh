# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNTS=[10000000 20000000 50000000 100000000]
DPU_COUNTS=[1 2 4 8 16 32 64 128 256 512 1024 2560]
QUERY_COUNTS=[200000 500000 1000000]
LAYERS=[17 18 19 20]
JAVA=~/jdk-17/bin/java

for NODES_COUNT in $NODES_COUNTS do;
	for DPU_COUNT in $DPU_COUNTS do;
		for QUERY_COUNT in $QUERY_COUNTS do;
			for LAYER in $LAYERS do;
				# ....
			done
		done
	done
done

## stat mode
echo "profile status CPU-Full"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "cpu-nodes-all-$i-($j).txt"
echo "profile status CPU-prepare"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "cpu-nodes-prepare-$i-($j).txt"
echo "profile status PIM-Full"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "pim-nodes-all-$i-($j).txt"
echo "profile status PIM-prepare"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "pim-nodes-prepare-$i-($j).txt"


## record mode
echo "profile record CPU"
perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${QUERY_COUNT}q.txt";

echo "profile record PIM"
perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -cp bst-latest.jar:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${QUERY_COUNT}q.txt";
