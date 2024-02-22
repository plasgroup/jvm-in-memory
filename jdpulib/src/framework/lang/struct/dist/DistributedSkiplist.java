package framework.lang.struct.dist;

import framework.lang.struct.dist.proxy.SSkipListProxy;
import framework.pim.UPMEM;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class DistSkipNode<K extends Comparable, T> extends SkipNode
{
    int site = -1;
    DistSkipNode right,down;//右下个方向的指针
    public DistSkipNode (K key, T value) {
        super(key, value);
        this.key = key;
        this.value = value;
    }
    public DistSkipNode (K key, T value, int site) {
        super(key, value);
        this.key = key;
        this.value = value;
        this.site = site;
    }
}

public class DistributedSkiplist<K extends Comparable, V> extends SSkipList<K, V> {
    int partitionCount;

    int count = 0;
    Random random = new Random();
    SSkipList<K, V>[] skipLists;
    public DistributedSkiplist(int partitionCount){
        this.partitionCount = partitionCount;
        skipLists = new SSkipList[partitionCount];
        for(int i = 0; i < partitionCount; i++){
            skipLists[i] = (SSkipListProxy<K, V>) UPMEM.getInstance().createObject(i, SClassicalSkiplist.class);
        }
    }

    @Override
    public boolean put(K key, V value) {
        int site = random.nextInt(partitionCount);
        if(!skipLists[site].put(key, value)){
            return false;
        }
        count++;
        return true;
    }

    @Override
    public Object search(K key) {
        for(int i = 0; i < partitionCount; i++){
            Object result = skipLists[i].search(key);
            if(result != null) return result;
        }
        return null;
    }

    @Override
    public void delete(K key) {
        for(int i = 0; i < partitionCount; i++){
            skipLists[i].delete(key);
        }
    }

    @Override
    public List<SkipNode<K, V>> rangeSearch(K beginKey, K endKey) {
        List<SkipNode<K, V>> result = new ArrayList<>();
        for(int i = 0; i < partitionCount; i++){
            result.addAll(skipLists[i].rangeSearch(beginKey, endKey));
        }

        return result;
    }

    @Override
    public boolean contains(V element) {
        for(int i = 0; i < partitionCount; i++){
            if(skipLists[i].contains(element))
                return true;
        }
        return false;
    }

    @Override
    public int size() {
        return count;
    }
}
