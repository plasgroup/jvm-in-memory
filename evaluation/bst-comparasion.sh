# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST=LLC-load-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNT=500000000
DPU_COUNT=1024
QUERY_COUNT=500000
LAYER=21
JAVA=~/jdk-17.0.1/bin/java

## stat mode
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -jar bst-latest.jar TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER -classpath dpu.jar 2> "cpu-nodes-all-$i-($j).txt"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -jar bst-latest.jar TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER -classpath dpu.jar 2> "cpu-nodes-prepare-$i-($j).txt"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -jar bst-latest.jar TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER -classpath dpu.jar 2> "pim-nodes-all-$i-($j).txt"
perf stat -a -e $EVENT_LIST $JAVA $VM_OPTIONS -jar bst-latest.jar TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER -classpath dpu.jar 2> "pim-nodes-prepare-$i-($j).txt"


## record mode
perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -jar -classpath dpu.jar bst-latest.jar TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${i}q.txt";

perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -jar -classpath dpu.jar bst-latest.jar TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${i}q.txt";

