#!/bin/bash

VM_OPTIONS=""
JAVA=java # ~/jdk-17.0.1/bin/java
BST_JAR="bst-latest.jar"

## execute 
$JAVA $VM_OPTIONS -Djava.library.path=../upmem-2023.1.0-Linux-x86_64/lib -cp $BST_JAR:dpu.jar Main
