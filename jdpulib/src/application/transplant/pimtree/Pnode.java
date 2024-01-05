package application.transplant.pimtree;

public class Pnode {
    public long key;
    public long height;
    public long value;

    public Pnode(){}
    public Pnode(int randomKey, int randomValue) {
        this.key = randomKey;
        this.value = randomValue;
    }
}
