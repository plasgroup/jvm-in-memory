#!/bin/bash
JAVA=~/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
DICT_PATH=$(pwd)/dict.txt
FILES_PATH=$(pwd)/files
REQ_BASE_PATH=$(pwd)
declare -a NODES_COUNTS=(100000 200000 500000 1000000 2000000 5000000 10000000 20000000 50000000 1000000)
declare -a REQ_COUNT=(100 200000 500000 1000000 2000000 5000000 10000000)
declare -a THREADS=(1 2 4 8 16 24)
declare -a DPUS=(64)
declare -a LAYERS=(17 18 19 20)

bash ./prepare-bst-data.sh
exit 0

mkdir record-bst
# PIM Version
for cnt_nodes in ${NODES_COUNTS[@]}; do
        for cnt_reqs in ${REQ_COUNT[@]}; do
                for cnt_threads in ${THREADS[@]}; do
                        for cnt_layer in ${LAYERS[@]}; do
				for cnt_dpus in ${DPUS[@]}; do
                                  echo "NODES=$cnt_doc, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
                                  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main.class TYPE=PIM NODES=$cnt_nodes QUERIES=$cnt_reqs DPU_COUNT=$cnt_dpus CPU_LAYER_COUNT=$cnt_layer THREADS=$cnt_threads PROF_CPUDPU_DM
                                  # ./record-indexsearch/mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  perf report --stdio > ./record-bst/n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
			  	done
                        done
                done
        done
done

echo "Evaluate CPU-Only version"

# CPU-only Version
for cnt_doc in ${NODES_COUNTS[@]}; do
        for cnt_reqs in ${REQ_COUNT[@]}; do
                for cnt_threads in ${THREADS[@]}; do
                        for cnt_dpus in ${DPUS[@]}; do
                                  bash ./kill_all_defunct.sh
                                  echo "NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
                                  sh start_simulator_server.sh $cnt_dpus $cnt_threads &
                                  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main.class TYPE=PIM NODES=$cnt_nodes QUERIES=$cnt_reqs DPU_COUNT=$cnt_dpus THREADS=$cnt_threads PROF_CPUDPU_DM CPU_ONLY > ./record-bst/cpu-only-mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  perf report --stdio > ./record-bst/cpu-only-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  kill %1 2> /dev/null && wait $1 2> dev/null
                                  bash ./kill_all_defunct.sh
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
