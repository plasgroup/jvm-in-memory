package framework.lang.struct.dist.redis;

import java.util.Random;

public class DistributedSet<E> extends SSet<E>{

    SSet<E>[] sSet;
    int count;
    Random random = new Random();

    public DistributedSet(int partitionCount){
        sSet = new DistributedSet[partitionCount];
        for(int i = 0; i < partitionCount; i++){
            sSet[i] = new SSHashSet<>();
        }
    }
    @Override
    public boolean contains(E element) {
        for(int i = 0; i < sSet.length; i++)
        {
            if(sSet[i].contains(element)) return true;
        }
        return false;
    }

    @Override
    public void put(E element) {
        if(!contains(element)){
            sSet[random.nextInt()].put(element);
        }
    }

    @Override
    public int size() {
        return count;
    }
}
