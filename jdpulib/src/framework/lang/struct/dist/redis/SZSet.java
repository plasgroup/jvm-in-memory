package framework.lang.struct.dist.redis;

import java.util.List;

public abstract class SZSet<K extends Comparable, T> {
    public abstract boolean contains(T element);
    public abstract void put(K score, T element);
    public abstract int size();
    public abstract List<T> rangeScan(K scoreLeft, K scoreRight);
    public abstract List<T> rank(T element);
}
