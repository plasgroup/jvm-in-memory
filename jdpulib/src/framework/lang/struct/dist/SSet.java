package framework.lang.struct.dist;

public abstract class SSet<T> {
    public abstract boolean contains(T element);
    public abstract void put(T element);
    public abstract int size();
}
