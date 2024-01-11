JAVA=~/jdk-17/bin/java
NODES_COUNTS=[500000 1000000 2000000 5000000 10000000 20000000 50000000]
REQ_COUNT=[200000 500000 1000000 2000000 5000000 10000000]
THREADS=[1 2 4 8 16 24]
DPUS=[4 8 16 32 64]
for cnt_nodes in $NODES_COUNTS do;
	for cnt_reqs in $REQ_COUNT do;
		for cnt_threads in $THREADS do;
			for cnt_dpus in $DPUS do;
				...
			done
		done
	done
done
$JAVA -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain
