#!/bin/bash

JVM_IN_MEMORY_ROOT_DIR=$(realpath $(dirname "$(readlink -f "${BASH_SOURCE[0]}")"))

# UPMEM SDK
if [ ! -d upmem-2023.1.0-Linux-x86_64 ]; then
  wget http://sdk-releases.upmem.com/2023.1.0/debian_10/upmem-2023.1.0-Linux-x86_64.tar.gz
  tar xzvf upmem-2023.1.0-Linux-x86_64.tar.gz
fi
if [ ! -d upmem-2023.1.0-Linux-x86_64/src ]; then
    wget http://sdk-releases.upmem.com/2023.1.0/upmem-src-2023.1.0-Linux-x86_64.tar.gz
    tar xzvf upmem-src-2023.1.0-Linux-x86_64.tar.gz
fi
# src/backends/api/src/api/dpu_jni.c に対する指示をやる（共有ライブラリの入れ替えは libdpu.so と dpujni.so だけでいい）
rm upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
cp reproduce/dpu_jni.c upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
cd upmem-2023.1.0-Linux-x86_64/src/backends
cmake .
make
cd $JVM_IN_MEMORY_ROOT_DIR/upmem-2023.1.0-Linux-x86_64/lib
cp ../src/backends/api/libdpu.so.0.0 .
cp ../src/backends/api/libdpujni.so.0.0 .
rm libdpu.so
rm libdpujni.so
ln -s libdpu.so.0.0 libdpu.so
ln -s libdpujni.so.0.0 libdpujni.so
cd $JVM_IN_MEMORY_ROOT_DIR

#Java
if [ ! -d jdk-17.0.11 ]; then
  wget https://download.oracle.com/java/17/archive/jdk-17.0.11_linux-x64_bin.tar.gz
  tar xzvf jdk-17.0.11_linux-x64_bin.tar.gz
fi
PATH=$(pwd)/jdk-17.0.11/bin:$PATH

source upmem-2023.1.0-Linux-x86_64/upmem_env.sh
export LD_LIBRARY_PATH=$(pwd)/upmem-2023.1.0-Linux-x86_64/lib:$LD_LIBRARY_PATH
