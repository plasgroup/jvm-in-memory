# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST=LLC-load-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNT=10000000
DPU_COUNT=64
QUERY_COUNT=500000
LAYER=18
JAVA=~/jdk-17/bin/java
PROG=./bst_new.jar

$JAVA -jar generate-key-values.jar $NODES_COUNT 

## record mode
echo "profile record CPU"
perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER

perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${QUERY_COUNT}q.txt";

echo "profile record PIM"
perf record -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${QUERY_COUNT}q.txt";


## stat mode
echo "profile status CPU-Full"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "cpu-nodes-all-${NODES_COUNT}-(${QUERY_COUNT}).txt"
echo $(cat "cpu-nodes-all-${NODES_COUNT}-(${QUERY_COUNT}).txt")

echo "profile status CPU-prepare"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "cpu-nodes-prepare-${NODES_COUNT}-(${QUERY_COUNT}).txt"
echo $(cat "cpu-nodes-prepare-${NODES_COUNT}-(${QUERY_COUNT}).txt")

echo "profile status PIM-Full"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "pim-nodes-all-${NODES_COUNT}-(${QUERY_COUNT}).txt"
echo $(cat "pim-nodes-all-${NODES_COUNT}-(${QUERY_COUNT}).txt")

echo "profile status PIM-prepare"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT NO_SEARCH DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER 2> "pim-nodes-prepare-${NODES_COUNT}-(${QUERY_COUNT}).txt"
echo $(cat "pim-nodes-prepare-${NODES_COUNT}-(${QUERY_COUNT}).txt")


## record mode
echo "profile record CPU"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=CPU NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER

perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${QUERY_COUNT}q.txt";

echo "profile record PIM"
$JAVA $VM_OPTIONS -cp ${PROG}:dpu.jar Main TYPE=PIM NODES=$NODES_COUNT QUERIES=$QUERY_COUNT DPU_COUNT=$DPU_COUNT CPU_LAYER_COUNT=$LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${QUERY_COUNT}q.txt";

