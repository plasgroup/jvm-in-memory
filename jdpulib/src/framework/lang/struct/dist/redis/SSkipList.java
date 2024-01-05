package framework.lang.struct.dist.redis;

import java.util.List;


public abstract class SSkipList<K extends Comparable, T>{
    public abstract boolean put(K key, T value);
    public abstract Object search(K key);
    public abstract void delete(K key);
    public abstract List<SkipNode<K, T>> rangeSearch(K beginKey, K endKey);


    public abstract boolean contains(T element);

    public abstract int size();
}
