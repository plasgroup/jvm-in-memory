package framework.lang.struct.dist.redis;

public class SLinkedHash<K, V> extends SHash<K, V>{
    int capacity = 16;
    double loadFactor = 0.75;

    HashNode<K, V>[] array = new HashNode[capacity];
    int count = 0;
    int threshHold = (int) (capacity * loadFactor);


    public void put(HashNode<K, V>[] targetArray, K key, V value){
        int hash = key.hashCode();
        int loc = (hash % targetArray.length);
        HashNode<K, V> node = targetArray[loc];
        if(node == null){
            targetArray[loc] = new HashNode<>(key, value);
            count++;
            extendIfThresholdExceed();
        }else{
            while(true){
                if(node.key == key){
                    node.setVal(value);
                    return;
                }
                if(node.next != null){
                    node = node.next;
                }else{
                    node.next = new HashNode<>(key, value);
                    count++;
                    extendIfThresholdExceed();
                    return;
                }
            }

        }
    }

    public void extendIfThresholdExceed(){
        if(count < threshHold) return;
        HashNode<K, V>[] newArray = new HashNode[capacity * 2];
        // rehash
        for (HashNode<K, V> kvHashNode : array) {
            if (kvHashNode == null) continue;
            HashNode<K, V> node = kvHashNode;
            while (node != null) {
                put(newArray, node.key, node.val);
                node = node.next;
            }
        }
        capacity *= 2;
        threshHold = (int) (capacity * loadFactor);

        array = newArray;

    }


    public void put(K key, V value){
        put(array, key, value);
    }

    public V get(K key){
        int hash = key.hashCode();
        int loc = (hash % capacity);
        HashNode<K, V> node = array[loc];
        while(node != null){
            if(node.key == key) return node.val;
            node = node.next;
        }
        return null;
    }

    public boolean containsKey(K key){
        int hash = key.hashCode();
        int loc = (hash % capacity);
        HashNode<K, V> node = array[loc];
        while(node != null){
            if(node.key == key) return true;
            node = node.next;
        }
        return false;
    }

    @Override
    public int size() {
        return count;
    }

}
