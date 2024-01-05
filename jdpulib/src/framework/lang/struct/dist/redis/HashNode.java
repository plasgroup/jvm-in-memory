package framework.lang.struct.dist.redis;

public class HashNode<K, V> {
    K key;
    V val;
    HashNode<K, V> next;

    public HashNode(K key, V value) {
        this.key = key;
        this.val = value;
    }

    public V getVal() {
        return val;
    }

    public K getKey() {
        return key;
    }

    public void setVal(V val) {
        this.val = val;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
