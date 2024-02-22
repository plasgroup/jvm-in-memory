package framework.lang.struct.dist;
class SArrayListNode<T>{
    T value;
}
public class SArrayList<T> extends SList<T>{

    SArrayListNode<T>[] array;
    int capacity = 16;
    int size = 0;
    double factor = 0.75;
    int threshHold = (int) (capacity * factor);
    @Override
    public T get(int loc) {
        if(loc < 0 || loc > size){
            return null;
        }
        return array[loc].value;
    }

    @Override
    public void set(int loc, T value) {
        if(loc < 0 || loc > size){
            return;
        }
        array[loc].value = value;
    }

    @Override
    public void append(T value) {
        if(size + 1 < capacity){
            array[size + 1].value = value;
            size ++;
        }
        if(size > threshHold){
            extendList();
        }
    }


    private void extendList(){
        SArrayListNode[] newArray = new SArrayListNode[capacity * 2];
        System.arraycopy(array, 0, newArray, 0, capacity);
        capacity *= 2;
        threshHold = (int) (capacity * factor);
    }

    @Override
    public int size() {
        return size;
    }
}

