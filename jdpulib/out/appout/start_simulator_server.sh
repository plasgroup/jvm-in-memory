DPUCOUNT=${1:-8}
THREADS_PER_DPU=${2:-24}
echo "start simulator servers.... cnt_dpu=$DPUCOUNT, cnt_threads=$THREADS_PER_DPU"
/home/huang/jdk-17/bin/java -jar simulator-server.jar DPU_COUNT=$DPUCOUNT THREAD_PER_DPU=$THREADS_PER_DPU
