JAVA=~/jdk-17/bin/java
EVENT_LIST="LLC-load-misses,LLC-store-misses"
VM_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+PreserveFramePointer -XX:+DumpPerfMapAtExit -Xmx131072m"
DOC_COUNTS=[10000 20000 50000 100000 200000 500000 1000000]
REQ_COUNT=[200000 500000 1000000 2000000 5000000 10000000]
THREADS=[1 2 4 8 16 24]
DPUS=[4 8 16 32 64]

# PIM Version
for NODES_COUNT $NODES_COUNTS do;
        for cnt_reqs in $REQ_COUNT do;
                for cnt_threads in $THREADS do;
                        for cnt_dpus in $DPUS do;
                                ...
                        done
                done
        done
done

# CPU Version
for NODES_COUNT in $NODES_COUNTS do;
        for DPU_COUNT in $DPU_COUNTS do;
                for QUERY_COUNT in $QUERY_COUNTS do;
                        for LAYER in $LAYERS do;
                                # ....
                        done
                done
        done
done

$JAVA -cp pimtree.jar:dpu.jar application.transplant.pimtree.PIMTreeMain
java -cp index-search.jar:dpu.jar application.transplant.index.search.IndexSearchMain