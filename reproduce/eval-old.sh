EVENT_LIST=LLC-load-misses,offcore_requests.all_data_rd,uncore_imc_0/cas_count_read/,uncore_imc_0/cas_count_write/,uncore_imc_1/cas_count_read/,uncore_imc_1/cas_count_write/
RECORD_EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
NODES_COUNT=500000000
DPU_COUNT=32
QUERY_COUNT=500000
LAYER=18
## stat mode
java -jar bst-latest.jar CPU  $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER #2> "cpu-nodes-all-$i-($j).txt"
java -jar bst-latest.jar CPU  $NODES_COUNT $QUERY_COUNT NO_SEARCH $DPU_COUNT $LAYER 2> "cpu-nodes-prepare-$i-($j).txt"
java -jar bst-latest.jar PIM  $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER #2> "pim-nodes-all-$i-($j).txt"
java -jar bst-latest.jar PIM $NODES_COUNT $QUERY_COUNT NO_SEARCH $DPU_COUNT $LAYER 2> "pim-nodes-prepare-$i-($j).txt"


## record mode
#perf record -e $RECORD_EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar CPU $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER
#perf script --itrace | grep 'search' >  "./record_files/[q]cpu-search-samples-${i}q.txt";

#perf record -e $RECORD_EVENT_LIST java $VM_OPTIONS -jar bst-latest.jar PIM $NODES_COUNT $QUERY_COUNT - $DPU_COUNT $LAYER
#perf script --itrace | grep 'search' >  "./record_files/[q]pim-search-samples-${i}q.txt";
