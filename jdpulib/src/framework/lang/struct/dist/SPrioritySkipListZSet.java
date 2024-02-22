package framework.lang.struct.dist;

import java.util.ArrayList;
import java.util.List;

public class SPrioritySkipListZSet<K extends Comparable, E> extends SZSet<K, E>{
    SSkipList<K, E> skipList = new SClassicalSkiplist<>();

    @Override
    public boolean contains(E element) {
        return skipList.contains(element);
    }

    @Override
    public void put(K score, E element) {
        skipList.put(score, element);
    }

    @Override
    public int size() {
        return skipList.size();
    }

    @Override
    public List<E> rangeScan(K scoreLeft, K scoreRight) {
        List<SkipNode<K, E>> skipNodes = skipList.rangeSearch(scoreLeft, scoreRight);
        List<E> result = new ArrayList<>();
        for(int i = 0; i < skipNodes.size(); i++){
            result.add(skipNodes.get(i).value);
        }
        return result;
    }

    @Override
    public List<E> rank(E element) {
        return null;
    }
}
