package framework.lang.struct.dist.redis;

import java.util.Random;

public class DistributedHash<K, V> extends SHash<K, V>{
    int count = 0;
    SHash[] hashes;
    Random random = new Random();
    int partitionCount;

    public DistributedHash(int partitionCount) {
        hashes = new SHash[partitionCount];
        this.partitionCount = partitionCount;
    }

    int getSite(K key){
        for(int i = 0; i < hashes.length; i++){
            if(hashes[i].containsKey(key)) return i;
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
            if(hashes[i].containsKey(key)) return true;
        }
        return false;
    }

    @Override
    public int size() {
        return count;
    }

}
