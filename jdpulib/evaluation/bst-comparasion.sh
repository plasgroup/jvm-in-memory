# EVENT_LIST=LLC-load-misses,uncore_imc_free_running_0/data_total/,uncore_imc_free_running_0/data_write/,uncore_imc_free_running_0/data_read/
EVENT_LIST=LLC-load-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNT=500000000
DPU_COUNT=1024
QUERY_COUNT=500000
LAYER=21
## stat mode
perf stat -a -e $EVENT_LIST java -jar bst-latest.jar CPU  $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER 2> "cpu-nodes-all-$i-($j).txt"
perf stat -a -e $EVENT_LIST java -jar bst-latest.jar CPU  $NODES_COUNT $QUERY_COUNT NO_SEARCH $DPU_COUNT $LAYER 2> "cpu-nodes-prepare-$i-($j).txt"
perf stat -a -e $EVENT_LIST java -jar bst-latest.jar PIM  $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER 2> "pim-nodes-all-$i-($j).txt"
perf stat -a -e $EVENT_LIST java -jar bst-latest.jar PIM $NODES_COUNT $QUERY_COUNT NO_SEARCH $DPU_COUNT $LAYER 2> "pim-nodes-prepare-$i-($j).txt"


## record mode
perf record -e $RECORD_EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar CPU $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${i}q.txt";

perf record -e $RECORD_EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar PIM $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER
perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${i}q.txt";
