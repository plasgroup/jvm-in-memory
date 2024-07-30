#!/bin/bash

JVM_IN_MEMORY_ROOT_DIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

echo $JVM_IN_MEMORY_ROOT_DIR

exit(0)

# UPMEM SDK
wget http://sdk-releases.upmem.com/2023.1.0/debian_10/upmem-2023.1.0-Linux-x86_64.tar.gz
wget http://sdk-releases.upmem.com/2023.1.0/upmem-src-2023.1.0-Linux-x86_64.tar.gz
tar xzvf upmem-2023.1.0-Linux-x86_64.tar.gz
tar xzvf upmem-src-2023.1.0-Linux-x86_64.tar.gz
# src/backends/api/src/api/dpu_jni.c に対する指示をやる（共有ライブラリの入れ替えは libdpu.so と dpujni.so だけでいい）
rm upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
cp reproduce/dpu_jni.c upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
cd upmem-2023.1.0-Linux-x86_64/src/backends
cmake .
make
cd $JVM_IN_MEMORY_ROOT_DIR/upmem-2023.1.0-Linux-x86_64/lib
rm libdpu.so
rm libdpujni.so
ln -s ../src/backends/api/libdpu.so.0.0 ./libdpu.so
ln -s ../src/backends/api/libdpujni.so.0.0 ./libdpujni.so
cd $JVM_IN_MEMORY_ROOT_DIR

#Java
wget https://download.oracle.com/java/17/archive/jdk-17.0.11_linux-x64_bin.tar.gz
tar xzvf jdk-17.0.11_linux-x64_bin.tar.gz
PATH=$(pwd)/jdk-17.0.11/bin:$PATH

source upmem-2023.1.0-Linux-x86_64/upmem_env.sh
export LD_LIBRARY_PATH=$(pwd)/upmem-2023.1.0-Linux-x86_64/lib:$LD_LIBRARY_PATH
