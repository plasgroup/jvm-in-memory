#!/bin/bash

VM_OPTIONS=""
JAVA=java # ~/jdk-17.0.1/bin/java
NBODY_JAR="nbody-latest.jar"

## execute 
$JAVA $VM_OPTIONS -Djava.library.path=../upmem-2023.1.0-Linux-x86_64/lib -cp $NBODY_JAR:dpu.jar application.nbody.Main 100
