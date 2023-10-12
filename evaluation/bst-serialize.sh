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
PARAM_LIST_CPU="CPU_LAYER_COUNT=$LAYER_COUNT DPU_COUNT=$DPU_COUNT QUERIES=$QUERY_COUNT IMG_PATH=./imgs/ NODES=$NODES_COUNT NO_SEARCH TYPE=PIM SERIALIZE_TREE=1 WRITE_KV=$NODES_COUNT JVM_SIMULATOR=0 BUILD_FROM_IMG=0"
mkdir -p imgs
$JAVA $VM_OPTIONS -cp $BST_JAR:dpu.jar Main $PARAM_LIST_CPU

