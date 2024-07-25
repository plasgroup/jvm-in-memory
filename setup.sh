#!/bin/bash

# UPMEM SDK
# wget http://sdk-releases.upmem.com/2023.1.0/debian_10/upmem-2023.1.0-Linux-x86_64.tar.gz
# wget http://sdk-releases.upmem.com/2023.1.0/upmem-src-2023.1.0-Linux-x86_64.tar.gz
# tar xzvf upmem-2023.1.0-Linux-x86_64.tar.gz
# tar xzvf upmem-src-2023.1.0-Linux-x86_64.tar.gz
# src/backends/api/src/api/dpu_jni.c に対する指示をやる（共有ライブラリの入れ替えは libdpu.so と dpujni.so だけでいい）
# rm upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
# cp dpu_jni.c upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
# cd upmem-2023.1.0-Linux-x86_64/src/backends
# cmake .
# make
# cp upmem-2023.1.0-Linux-x86_64/src/backends
source upmem-2023.1.0-Linux-x86_64/upmem_env.sh
export LD_LIBRARY_PATH=$(pwd)/upmem-2023.1.0-Linux-x86_64/lib:$LD_LIBRARY_PATH
