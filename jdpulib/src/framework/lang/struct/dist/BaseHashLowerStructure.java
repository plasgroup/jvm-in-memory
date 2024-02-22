package framework.lang.struct.dist;

import java.util.Enumeration;
import java.util.List;

public abstract class BaseHashLowerStructure<K,V> {

    public abstract boolean containsKey(K key);

    public abstract V getValue(K key);

    public abstract void setValue(K key, V val);

    public abstract void removeKey(K key);

    public abstract int getLength();

    public abstract List<RObject> getValues();

    public abstract Enumeration<RObject> getKeys() ;

    public abstract void incrby(K key);

    public abstract void getAll();

    public abstract void scan(int cursor, V matchPattern, int count);

    public abstract Enumeration<String> randfield(int count);

    public abstract void setNX() ;
}


