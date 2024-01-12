#!/bin/bash
JAVA=/home/huang/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
declare -a NODES_COUNTS=(500 500000 1000000 2000000 5000000 10000000 20000000 50000000)
declare -a REQ_COUNT=(200 200000 500000 1000000 2000000 5000000 10000000)
declare -a THREADS=(1 2 4 8 16 24)
declare -a DPUS=(4 8 16 32 64)

mkdir record-pimtree
# PIM Version
for cnt_nodes in ${NODES_COUNTS[@]}; do
	for cnt_reqs in ${REQ_COUNT[@]}; do
		for cnt_threads in ${THREADS[@]}; do
			for cnt_dpus in ${DPUS[@]}; do
				bash ./kill_all_defunct.sh
				echo "NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
			        sh start_simulator_server.sh $cnt_dpus $cnt_threads &
				perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain KEYS_COUNT=$cnt_nodes TSK_N=$cnt_reqs
				perf report --stdio > ./record-pimtree/n${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
			        kill %1 2> /dev/null && wait $1 2> dev/null
				bash ./kill_all_defunct.sh
			done
		done
	done
done

# CPU-only Version
for NODES_COUNT in $NODES_COUNTS do;
        for DPU_COUNT in $DPU_COUNTS do;
                for QUERY_COUNT in $QUERY_COUNTS do;
                        for LAYER in $LAYERS do;
                                # ....
                        done
                done
        done
done

$JAVA -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain
