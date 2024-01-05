package framework.lang.struct.dist.redis;

public class SSHashSet<T> extends SSet<T>{
    protected SHash<T, Object> storage = new SLinkedHash();
    @Override
    public boolean contains(T element) {
        return storage.containsKey(element);
    }

    @Override
    public void put(T element) {
        storage.put(element, null);
    }

    @Override
    public int size() {
        return storage.size();
    }


}
