package framework.lang.struct.dist;

import java.util.*;

public class DictionaryHash<K,V> extends BaseHashLowerStructure<K,V> {
    Dictionary<K, V> dictionary = new Hashtable<>();

    @Override
    public boolean containsKey(K key) {
        return dictionary.get(key) != null;
    }

    @Override
    public V getValue(K key) {
        return dictionary.get(key);
    }

    @Override
    public void setValue(K key, V val) {
        dictionary.put(key, val);
    }

    @Override
    public void removeKey(K key) {
        dictionary.remove(key);
    }

    @Override
    public int getLength() {
        return dictionary.size();
    }

    @Override
    public List<RObject> getValues() {
        List<V> result = new ArrayList<>();
        for (Iterator<K> it = dictionary.keys().asIterator(); it.hasNext(); ) {
            K key = it.next();
            result.add(getValue(key));
        }
        return (List<RObject>) result;
    }

    @Override
    public Enumeration<RObject> getKeys() {
        return null;
    }

    @Override
    public void incrby(K key) {

    }

    @Override
    public void getAll() {

    }

    @Override
    public void scan(int cursor, V matchPattern, int count) {

    }

    @Override
    public Enumeration<String> randfield(int count) {
        return null;
    }

    @Override
    public void setNX() {

    }


}
