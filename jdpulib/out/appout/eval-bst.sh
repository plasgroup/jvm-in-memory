#!/bin/bash
JAVA=~/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
DICT_PATH=$(pwd)/dict.txt
FILES_PATH=$(pwd)/files
REQ_BASE_PATH=$(pwd)
declare -a NODES_COUNTS=(100000000)
declare -a REQ_COUNT=(200000)
declare -a THREADS=(1 24)
declare -a DPUS=(256)
declare -a LAYERS=(18)

## 1 thread, batch dispatch
## 24 thread, batch dispatch
## 1 thread, non batch dispatch

# PIM Version
for cnt_reqs in ${REQ_COUNT[@]}; do
    for cnt_threads in ${THREADS[@]}; do
	  for cnt_nodes in ${NODES_COUNTS[@]}; do
                        for cnt_layer in ${LAYERS[@]}; do
				for cnt_dpus in ${DPUS[@]}; do
                                  echo "NODES=$cnt_doc, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_dpus"
				  echo "img_path=/bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/"

				  ### PIM version
          perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main BUILD_FROM_IMG=1 TYPE=PIM NODES=$cnt_nodes QUERIES=${cnt_reqs} DPU_COUNT=$cnt_dpus CPU_LAYER_COUNT=$cnt_layer THREADS=$cnt_threads PROF_CPUDPU_DM SERIALIZE_TREE=0 IMG_PATH=./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/ BATCH_DISPATCH PROFILE_QUERY_TIME=1 NO_SEARCH=0 > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[PIM-EXEC]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt

				  perf report --stdio > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[PIM]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt

				  ### PIM version
          perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main BUILD_FROM_IMG=1 TYPE=PIM NODES=$cnt_nodes QUERIES=${cnt_reqs} DPU_COUNT=$cnt_dpus CPU_LAYER_COUNT=$cnt_layer THREADS=$cnt_threads PROF_CPUDPU_DM SERIALIZE_TREE=0 IMG_PATH=./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/ PROFILE_QUERY_TIME=1 NO_SEARCH=0 > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[PIM-EXEC-NO-BATCH]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt

          perf report --stdio > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[PIM]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}-no-batch.txt



				 ### CPU-Only version 
				  perf record -e $EVENT_LIST $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main BUILD_FROM_IMG=1 TYPE=CPU NODES=$cnt_nodes QUERIES=${cnt_reqs} DPU_COUNT=$cnt_dpus CPU_LAYER_COUNT=$cnt_layer THREADS=$cnt_threads PROF_CPUDPU_DM SERIALIZE_TREE=0 IMG_PATH=./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/ BATCH_DISPATCH PROFILE_QUERY_TIME=1 NO_SEARCH=0  > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[CPU-EXEC]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
                                  
				  perf report --stdio > ./bst-tree/n-${cnt_nodes}-l-${cnt_layer}-d-${cnt_dpus}/[CPU]n-${cnt_nodes}-r${cnt_reqs}-t${cnt_threads}-d${cnt_dpus}.txt
			  	done
                        done
                done
        done
done

