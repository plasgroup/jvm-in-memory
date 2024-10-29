#!/bin/bash

set -e

EVAL_DIR=$(realpath $(dirname "$(readlink -f "${BASH_SOURCE[0]}")"))
DPUJVM_DIR=$EVAL_DIR/../dpu_jvm
JDPU_DIR=$EVAL_DIR/../jdpulib
OUT2_DIR=$JDPU_DIR/out2

cd $DPUJVM_DIR
rm dpuslave || true
make dpuslave
cp dpuslave $EVAL_DIR/dpuslave
cd $JDPU_DIR
javac -encoding UTF-8 -sourcepath ./src -d ./out2 ./src/application/nbody/Main.java -classpath ./src/dpu.jar
cp ./src/MANIFEST.MF $OUT2_DIR
cd $OUT2_DIR
jar cvfm nbody-latest.jar MANIFEST.MF .
cp nbody-latest.jar $EVAL_DIR/nbody-latest.jar
cd $EVAL_DIR

trap "cd $EVAL_DIR" EXIT
