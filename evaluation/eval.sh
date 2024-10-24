# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST=LLC-load-misses,LLC-store-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
# VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
VM_OPTIONS=""
NODES_COUNT=1000
DPU_COUNT=1
QUERY_COUNT=500
LAYER_COUNT=5
JAVA=java # ~/jdk-17.0.1/bin/java
BST_JAR="bst-latest.jar"
## stat mode
PARAM_LIST_CPU="CPU_LAYER_COUNT=$LAYER_COUNT DPU_COUNT=$DPU_COUNT QUERIES=$QUERY_COUNT IMG_PATH=./imgs/ NODES=$NODES_COUNT TYPE=CPU SERIALIZE_TREE=0 PERF_MODE=1 CPU_PERF_REPEAT=1 PIM_PERF_REPEAT=1 EVAL_CPU_PERF=1 EVAL_PIM_PERF=1 EVAL_NODES=$NODES_COUNT BATCH_DISPATCH=0 JVM_SIMULATOR=0 BUILD_FROM_IMG=0"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=CPU $PARAM_LIST 2> "cpu-nodes-all-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=CPU NO_SEARCH $PARAM_LIST 2> "cpu-nodes-prepare-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=PIM $PARAM_LIST 2> "pim-nodes-all-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=PIM NO_SEARCH $PARAM_LIST 2> "pim-nodes-prepare-$NODES_COUNT-($QUERY_COUNT).txt"

if [ "$PERF_COUNTER_CYCLE" == "true" ]; then
    PARAM_LIST_CPU="$PARAM_LIST_CPU PERF_COUNTER_CYCLE"
fi
if [ "$PERF_COUNTER_INSN" == "true" ]; then
    PARAM_LIST_CPU="$PARAM_LIST_CPU PERF_COUNTER_INSN"
fi

## record 
$JAVA $VM_OPTIONS -Djava.library.path=../upmem-2023.1.0-Linux-x86_64/lib -cp $BST_JAR:dpu.jar Main $PARAM_LIST_CPU
#perf report -n -f --stdio > "./record_files/[q]cpu-profile-${NODES_COUNT}n-${QUERY_COUNT}q.txt";

#$JAVA $VM_OPTIONS -jar $BST_JAR TYPE=PIM $PARAM_LIST
#perf report -n -f --stdio > "./record_files/[q]pim-profile-${NODES_COUNT}n-${QUERY_COUNT}q.txt";
