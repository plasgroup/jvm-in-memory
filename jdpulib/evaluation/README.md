1. git clone git@github.com:plasgroup/jvm-in-memory.git
2. cd ./jvm-in-memory/jdpulib/evaluation
   sudo apt install flex bison libelf-dev systemtap-sdt-dev libaudit-dev libslang2-dev libperl-dev libdw-dev
3. sudo apt-get install linux-tools
   sudo apt-get install linux-tools-common 
   sudo apt-get install linux-tools-generic
> git clone --depth 1 https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git
cd linux/tools/perf
make
cp perf /usr/bin
4. sudo apt install openjdk-17-jdk
   export JAVA_HOME=/usr/lib/jvm/...
   
5. vi ~/.bashrc
+ append
  + export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
  + export PERF="/usr/lib/linux-tools/..."
  + export UPMEM_HOME="...."
  + export LD_LIBRARY_PATH="${UPMEM_HOME}/lib${LD_LIBRARY_PATH+:$LD_LIBRARY_PATH}"
  + export PATH="${UPMEM_HOME}/bin:${PATH}"

6. sh profiling-cpu.sh
7. sh profiling-pim.sh

