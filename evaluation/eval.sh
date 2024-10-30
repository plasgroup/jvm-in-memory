#!/bin/bash

set -e

OUTPUT_DIR=$(pwd)/$(date '+%Y%m%d_%H%M%S_jvm-in-memory_nbody')
mkdir "$OUTPUT_DIR"
cd $(dirname "$(readlink -f "$0")")

VM_OPTIONS=""
JAVA=java # ~/jdk-17.0.1/bin/java
NBODY_JAR="nbody-latest.jar"

## execute 
for ITERATIONS in 1 10 100; do
    for i in {1..10}; do
        OUTPUT_FILE="$OUTPUT_DIR/jvm-in-memory-$ITERATIONS-$i.txt"
        $JAVA $VM_OPTIONS -Djava.library.path=../upmem-2023.1.0-Linux-x86_64/lib -cp $NBODY_JAR:dpu.jar application.nbody.Main $ITERATIONS > $OUTPUT_FILE
    done
done
