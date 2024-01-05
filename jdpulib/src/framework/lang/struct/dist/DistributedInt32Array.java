package framework.lang.struct.dist;

import framework.pim.UPMEM;

import static framework.lang.struct.dist.ArrayDistributedStrategy.BLOCK;


/** A distributed array structure **/
public class DistributedInt32Array{
    final ArrayDistributedStrategy strategy;
    final int partitionCount;
    final int length;
    DPUInt32ArrayHandler[] handler;
    public DistributedInt32Array(int len, int partitionCount, ArrayDistributedStrategy strategy) {
        this.partitionCount = partitionCount;
        this.strategy = strategy;
        this.length = len;
        init();
    }

    private void init() {
        if(strategy == BLOCK){
            int averageLength = length / this.partitionCount;
            int remain = length % this.partitionCount;
            for(int i = 0; i < partitionCount - 1; i++){
                handler[i] = UPMEM.getInstance().createArray(i, averageLength);
            }
            handler[partitionCount - 1] = UPMEM.getInstance().createArray(partitionCount - 1, averageLength + remain);
        }
    }

    public DistributedInt32Array(int len, int partitionCount) {
        this(len, partitionCount, BLOCK);
    }



    int get(int i){
        if(strategy == BLOCK){
            int p = i / partitionCount;
            return handler[p].get(i % partitionCount);
        }
        return -1;
    }

    void set(int i){

    }

}
