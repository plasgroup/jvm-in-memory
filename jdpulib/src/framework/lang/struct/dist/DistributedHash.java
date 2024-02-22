package framework.lang.struct.dist;

import framework.lang.struct.dist.proxy.DictionaryProxy;
import framework.pim.UPMEM;

import java.util.Dictionary;
import java.util.Random;

public class DistributedHash<K, V> extends SHash<K, V>{
    int count = 0;
    Dictionary[] hashes;
    Random random = new Random();
    int partitionCount;

    public DistributedHash(int partitionCount) {
        hashes = new Dictionary[partitionCount];
        this.partitionCount = partitionCount;
        for(int i = 0; i < partitionCount; i++){
            hashes[i] = (DictionaryProxy) UPMEM.getInstance().createObject(i, Dictionary.class);
        }
    }

    int getSite(K key){
        for(int i = 0; i < hashes.length; i++){
            if(hashes[i].get(key) != null) return i;
        }
        return -1;
    }
    @Override
    public void put(K key, V value) {
        int site = getSite(key);
        if(site == -1){
            hashes[random.nextInt(partitionCount)].put(key, value);
            count++;
        }else{
            hashes[site].put(key, value);
        }
    }

    @Override
    public V get(K key) {
        for(int i = 0; i < hashes.length; i++){
            V value = (V) hashes[i].get(key);
            if(value != null) return value;
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        for(int i = 0; i < hashes.length; i++){
            if(hashes[i].get(key) != null) return true;
        }
        return false;
    }

    @Override
    public int size() {
        return count;
    }

}
