package framework.lang.struct.dist.redis;


/**
 * capacity 即容量，默认16。
 * loadFactor 加载因子，默认是0.75
 * threshold 阈值。阈值=容量*加载因子。默认12。当元素数量超过阈值时便会触发扩容。
 * **/
public abstract class SHash<K, V>{
    public abstract void put(K key, V value);

    public abstract V get(K key);

    public abstract boolean containsKey(K key);

    public abstract int size();


//
//    BaseHashLowerStructure<String, String> dictionary = new DictionaryHash();
//
//    public void HSET(String key, String value){
//        dictionary.setValue(key, value);
//    }
//    public String HGET(String key){
//        if(containsKey(key))
//            return dictionary.getValue(key);
//        return null;
//    }
//    public void HDEL(String key){
//        dictionary.removeKey(key);
//    }
//    public boolean HEXISTS(String key){
//        return containsKey(key);
//    }
//    public int HLEN(){
//        return dictionary.getLength();
//    }
//    public void HINCRBY(String key){
//        dictionary.incrby(key);
//    }
//
//    // batch
//    public void HMSET(String... keyValues){
//        for(int i = 0; i < keyValues.length / 2; i++){
//            HSET(keyValues[i], keyValues[i + 1]);
//            i += 2;
//        }
//    }
//
//    public void HMGET(String... keys){
//        for(int i = 0; i < keys.length / 2; i++){
//            HGET(keys[i]);
//            i += 2;
//        }
//    }
//
//    public Enumeration<RObject> HKEYS(){
//        return dictionary.getKeys();
//    }
//
//    public Enumeration<RObject> HVALS(){
//        return dictionary.getValues();
//    }
//
//    public void HGETALL(){
//        dictionary.getAll();
//    }
//
//    public void HSCAN(int cursor, String matchPattern, int count){
//        dictionary.scan(cursor, matchPattern, count);
//    }
//
//    // Returns one or more random fields from a hash.
//    public Enumeration<String> HRANDFIELD(int count){
//        return dictionary.randfield(count);
//    }
//
//    // Sets the value of a field in a hash only when the field doesn't exist.
//    public void HSETNX(String key, String val){
//        if(!containsKey(key))
//            dictionary.setValue(key, val);
//    }
//
//    private boolean containsKey(String key) {
//        return dictionary.containsKey(key);
//    }

}
