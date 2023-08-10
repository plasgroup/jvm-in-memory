# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST=LLC-load-misses,LLC-store-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNT=10000000
DPU_COUNT=64
QUERY_COUNT=20000
LAYER_COUNT=18
JAVA=java
BST_JAR="bst-latest.jar"
## stat mode
PARAM_LIST="BUILD_FROM_IMG CPU_LAYER_COUNT=$LAYER_COUNT DPU_COUNT=$DPU_COUNT QUERIES=$QUERY_COUNT IMG_PATH=./imgs/ NODES=$NODES_COUNT"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=CPU $PARAM_LIST 2> "cpu-nodes-all-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=CPU NO_SEARCH $PARAM_LIST 2> "cpu-nodes-prepare-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=PIM $PARAM_LIST 2> "pim-nodes-all-$NODES_COUNT-($QUERY_COUNT).txt"
#perf stat -a -e $EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar TYPE=PIM NO_SEARCH $PARAM_LIST 2> "pim-nodes-prepare-$NODES_COUNT-($QUERY_COUNT).txt"

## record mode
perf record -B -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -jar $BST_JAR TYPE=CPU $PARAM_LIST
perf report -n -f --stdio > "./record_files/[q]cpu-profile-${NODES_COUNT}n-${QUERY_COUNT}q.txt";

perf record -B -e $RECORD_EVENT_LIST $JAVA $VM_OPTIONS -jar $BST_JAR TYPE=PIM $PARAM_LIST
perf report -n -f --stdio > "./record_files/[q]pim-profile-${NODES_COUNT}n-${QUERY_COUNT}q.txt";

