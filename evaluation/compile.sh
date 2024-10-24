#!/bin/bash

set -e

EVAL_DIR=$(realpath $(dirname "$(readlink -f "${BASH_SOURCE[0]}")"))
DPUJVM_DIR=$EVAL_DIR/../dpu_jvm
JDPU_DIR=$EVAL_DIR/../jdpulib
OUT2_DIR=$JDPU_DIR/out2

# javac -sourcepath $JDPU_DIR/src -d $JDPU_DIR/out2 $JDPU_DIR/src/Main.java -classpath $JDPU_DIR/src/dpu.jar  
# jar cvfm bst-latest.jar $OUT2_DIR/MANIFEST.MF $OUT2_DIR
# cp $OUT2_DIR/bst-latest.jar $EVAL_DIR/bst-latest.jar

cd $DPUJVM_DIR
rm dpuslave || true
make dpuslave
cp dpuslave $EVAL_DIR/dpuslave
cd $JDPU_DIR
javac -sourcepath ./src -d ./out2 ./src/Main.java -classpath ./src/dpu.jar
cd $OUT2_DIR
jar cvfm bst-latest.jar MANIFEST.MF .
cp bst-latest.jar $EVAL_DIR/bst-latest.jar
cd $EVAL_DIR

trap "cd $EVAL_DIR" EXIT
