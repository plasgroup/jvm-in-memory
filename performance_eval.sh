#!/bin/bash
set -e

export OUTPUT_DIR=$(pwd)/$(date '+%Y%m%d_%H%M%S_jvm-in-memory')
mkdir "$OUTPUT_DIR"
JVM_IN_MEMORY_ROOT_DIR=$(dirname "$(readlink -f "$0")")


# PERF_COUNTER_CYCLE and PERF_COUNTER_INSN cannot be true at the same time
export PERF_COUNTER_CYCLE=false
export PERF_COUNTER_INSN=false
cd $JVM_IN_MEMORY_ROOT_DIR/dpu_jvm
rm dpuslave
make dpuslave
cd $JVM_IN_MEMORY_ROOT_DIR/jdpulib
export OUTPUT_FILE="$OUTPUT_DIR"/"FASTEST.txt"
$JVM_IN_MEMORY_ROOT_DIR/evaluation/bst-performance-eval.sh > $OUTPUT_FILE


# PERF_COUNTER_CYCLE and PERF_COUNTER_INSN cannot be true at the same time
export PERF_COUNTER_CYCLE=true
export PERF_COUNTER_INSN=false
cd $JVM_IN_MEMORY_ROOT_DIR/dpu_jvm
rm dpuslave
make dpuslave
cd $JVM_IN_MEMORY_ROOT_DIR/jdpulib
export OUTPUT_FILE="$OUTPUT_DIR"/"CYCLE.txt"
$JVM_IN_MEMORY_ROOT_DIR/evaluation/bst-performance-eval.sh > $OUTPUT_FILE


# PERF_COUNTER_CYCLE and PERF_COUNTER_INSN cannot be true at the same time
export PERF_COUNTER_CYCLE=false
export PERF_COUNTER_INSN=true
cd $JVM_IN_MEMORY_ROOT_DIR/dpu_jvm
rm dpuslave
make dpuslave
cd $JVM_IN_MEMORY_ROOT_DIR/jdpulib
export OUTPUT_FILE="$OUTPUT_DIR"/"INSN.txt"
$JVM_IN_MEMORY_ROOT_DIR/evaluation/bst-performance-eval.sh > $OUTPUT_FILE


export OUTPUT_DIR=""
export OUTPUT_FILE=""
