DPUCOUNT=8
THREADS_PER_DPU=24
echo "start simulator servers...."
~/jdk-17/bin/java -jar simulator-server.jar DPU_COUNT=$DPUCOUNT THREAD_PER_DPU=$THREADS_PER_DPU
