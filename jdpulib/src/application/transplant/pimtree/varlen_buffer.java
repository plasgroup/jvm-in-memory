package application.transplant.pimtree;

public class varlen_buffer {
    public int len;
    public int capacity;
    public Object[] ptr;
    public varlen_buffer(int c){
        capacity = c;
        ptr = new Object[c];
    }


}
