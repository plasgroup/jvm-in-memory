package framework.lang.struct.dist;

public abstract class SList<T> {


    public abstract T get(int loc);


    public abstract void set(int loc, T value);

    public abstract void append(T value);


    public abstract int size();
}
