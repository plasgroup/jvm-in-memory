#!/bin/bash
JAVA=~/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
DICT_PATH=$(pwd)/dict.txt
FILES_PATH=$(pwd)/files
REQ_BASE_PATH=$(pwd)
declare -a DOC_COUNTS=(100 10000 20000 50000 100000 200000)
declare -a REQ_COUNT=(100 200000 500000 1000000 2000000 5000000 10000000)
declare -a THREADS=(1 2 4 8 16 24)
declare -a DPUS=(4 8 16 32 64)


mkdir record-indexsearch
# PIM Version
for cnt_doc in ${DOC_COUNTS[@]}; do
        for cnt_reqs in ${REQ_COUNT[@]}; do
                for cnt_threads in ${THREADS[@]}; do
                        for cnt_dpus in ${DPUS[@]}; do
                                  bash ./kill_all_defunct.sh
                                  echo "NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
                                  sh start_simulator_server.sh $cnt_dpus $cnt_threads &
                                  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp index-search.jar:dpu.jar application.transplant.index.search.IndexSearchMain DOC_COUNT=$cnt_doc TSK_N=$cnt_reqs DPU_COUNT=$cnt_dpus THREADS=$cnt_threads PROF_CPUDPU_DM DICT_PATH=$DICT_PATH FILE_PATH=$FILES_PATH REQ_FILE=$REQ_BASE_PATH 
				  #> ./record-indexsearch/mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  perf report --stdio > ./record-indexsearch/n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  kill %1 2> /dev/null && wait $1 2> dev/null
                                  bash ./kill_all_defunct.sh
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
                                  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp index-search.jar:dpu.jar application.transplant.index.search.IndexSearchMain DOC_COUNT=$cnt_nodes TSK_N=$cnt_reqs DPU_COUNT=$cnt_dpus THREADS=$cnt_threads PROF_CPUDPU_DM > ./record-indexsearch/cpu-only-mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  perf report --stdio > ./record-indexsearch/cpu-only-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  kill %1 2> /dev/null && wait $1 2> dev/null
                                  bash ./kill_all_defunct.sh
                        done
                done
        done
done
