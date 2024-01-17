#!/bin/bash
JAVA=/home/huang/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
declare -a NODES_COUNTS=(500 500000 1000000 2000000 5000000 10000000 20000000 50000000)
declare -a REQ_COUNT=(200 200000 500000 1000000 2000000)
declare -a THREADS=(1) # 2 4 8 16 24)
declare -a DPUS=(64) #(4 8 16 32 64)
declare -a BATCH=( 200)
mkdir record-pimtree
# PIM Version
for cnt_nodes in ${NODES_COUNTS[@]}; do
	for cnt_reqs in ${REQ_COUNT[@]}; do
		for cnt_threads in ${THREADS[@]}; do
			for cnt_dpus in ${DPUS[@]}; do
				for cnt_batch_size in ${BATCH[@]}; do
				  # PIM
				  bash ./kill_all_defunct.sh
				  echo "NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
			          sh start_simulator_server.sh $cnt_dpus $cnt_threads &
				  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain KEYS_COUNT=$cnt_nodes TSK_N=$cnt_reqs DPU_COUNT=$cnt_dpus THREADS=$cnt_threads LOAD_BATCH=$cnt_batch_size EXEC_BATCH=$cnt_batch_size PROF_CPUDPU_DM KEY_VALUE_PATH=./pimtree-keyvalues/ > ./record-pimtree/[PIM-EXEC]mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}-b${cnt_batch_size}.txt
				  perf report --stdio > ./record-pimtree/[PIM]n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}-b${cnt_batch_size}.txt
			          kill %1 2> /dev/null && wait $1 2> dev/null
				  bash ./kill_all_defunct.sh


				  # CPU Only
				  bash ./kill_all_defunct.sh
                                  echo "CPU ONLY --- NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
                                  sh start_simulator_server.sh $cnt_dpus $cnt_threads &
                                  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain KEYS_COUNT=$cnt_nodes TSK_N=$cnt_reqs DPU_COUNT=$cnt_dpus THREADS=$cnt_threads LOAD_BATCH=$cnt_batch_size EXEC_BATCH=$cnt_batch_size PROF_CPUDPU_DM KEY_VALUE_PATH=./pimtree-keyvalues/ CPU_ONLY > ./record-pimtree/[CPU-EXEC]mov-n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}-b${cnt_batch_size}.txt
                                  perf report --stdio > ./record-pimtree/[CPU]n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}-b${cnt_batch_size}.txt
                                  kill %1 2> /dev/null && wait $1 2> dev/null
                                  bash ./kill_all_defunct.sh

				done
			done
		done
	done
done

