package framework.lang.struct.dist;

import framework.lang.struct.dist.proxy.SArrayListProxy;
import framework.pim.UPMEM;

public class DistributedList<E> extends SList<E> {
    int partitionAmount = 0;
    int partitionMaxLength = 0;
    SList<E>[] sLists;
    int currentPartition = 0;
    int currentPartitionElementCount = 0;

    int count = 0;
    DistributedList(int partitionAmount, int partitionMaxLength){
        sLists = new SList[partitionAmount];

        for(int i = 0; i < partitionAmount - 1; i++){
            sLists[i] = (SArrayListProxy) UPMEM.getInstance().createObject(i, SArrayList.class);
        }
        sLists[partitionAmount - 1] = new SArrayList<>();
        this.partitionAmount = partitionAmount;
        this.partitionMaxLength = partitionMaxLength;
    }
    @Override
    public E get(int loc) {
        int partitionLocation = loc / partitionMaxLength;
        int offset = loc % partitionMaxLength;
        return sLists[partitionLocation].get(offset);
    }

    @Override
    public void set(int loc, E value) {
        int partitionLocation = loc / partitionMaxLength;
        int offset = loc % partitionMaxLength;
        sLists[partitionLocation].set(offset, value);
    }

    @Override
    public void append(E value) {
        if(currentPartitionElementCount < partitionMaxLength){
            sLists[currentPartition].append(value);
            currentPartitionElementCount++;
        }else if (currentPartitionElementCount == partitionMaxLength){
            if(currentPartition >= partitionAmount){
                throw new RuntimeException("Distributed List Exceed Capacity");
            }else{
                currentPartition++;
                currentPartitionElementCount = 0;
                sLists[currentPartition].set(0, value);
            }
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }
}
