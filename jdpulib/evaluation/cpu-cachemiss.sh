#!/bin/bash
export SCRIPT_DIR="$(pwd)"
for i in {0..10}
do
	rm tmp.txt;
	cd "/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/jdpulib/out/production/jdpulib";
	perf record -B -e cache-references,cache-misses,cycles,instructions,branches,faults,migrations java -XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Djava.library.path=/home/huang/Desktop/upmem-2023.1.0-Linux-x86_64/lib -Dfile.encoding=UTF-8 -classpath "/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/jdpulib/out/production/jdpulib":"/media/huang/Local Disk2/jvm-in-memory-dev/jvm-in-memory/lib/dpu.jar" Main 2> slient.log;
	sudo perf report -f --stdio > "./tmp.txt";
        python "$SCRIPT_DIR/read_search_cachemiss.py" "./tmp.txt";
done
