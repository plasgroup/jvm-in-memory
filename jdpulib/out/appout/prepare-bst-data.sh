#!/bin/bash
JAVA=~/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
DICT_PATH=$(pwd)/dict.txt
FILES_PATH=$(pwd)/files
REQ_BASE_PATH=$(pwd)
declare -a NODES_COUNTS=(500000 1000000 2000000 5000000 10000000 20000000 50000000 100000000)
declare -a REQ_COUNT=(10000 50000 100000 200000)
declare -a THREADS=(1 4 8 16 24)
declare -a DPUS=(64)
declare -a LAYERS=(18)


# generate key-value-pairs
for cnt_nodes in ${NODES_COUNTS[@]}; do
  if [ ! -f ./key_values-${cnt_nodes}.txt ]; then
    echo "generate ./key_values-${cnt_nodes}.txt";
    $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main WRITE_KV_ONLY=$cnt_nodes
  fi
done

echo "generate key-value pairs finished"

for cnt_nodes in ${NODES_COUNTS[@]}; do
                        for cnt_layer in ${LAYERS[@]}; do
                                for cnt_dpus in ${DPUS[@]}; do
                                  echo "generate tree in conf = NODES=$cnt_nodes, REQUEST=$cnt_reqs, THREADS=$cnt_threads, DPUS=$cnt_layer"
				  mkdir -p ./bst-tree/n-$cnt_nodes-l-$cnt_layer-d-$cnt_dpus
                                  $JAVA $VM_OPTIONS -cp bst.jar:dpu.jar Main TYPE=PIM NODES=$cnt_nodes DPU_COUNT=$cnt_dpus CPU_LAYER_COUNT=$cnt_layer IMG_PATH=./bst-tree/n-$cnt_nodes-l-$cnt_layer-d-$cnt_dpus/ NO_SEARCH THREADS=1 BUILD_FROM_IMG=0 SERIALIZE_TREE > ./bst-tree/n-$cnt_nodes-l-$cnt_layer-d-$cnt_dpus/building-log.log

				 
                                done
                        done
                done
