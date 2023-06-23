package pim.algorithm;

public abstract class TreeNode{
    private int key;
    private int val;
    private TreeNode left;
    private TreeNode right;

    public TreeNode(int k, int v) {
        this.key = k;
        this.val = v;
    }
    public TreeNode getLeft() {
        return left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public int getKey() {
        return key;
    }

    public int getVal() {
        return val;
    }

    // Algorithm
    public void insert(int k, int v){
        if(k < key){
            if (getLeft() == null)
                left = createNode(k, v);
            else
                getLeft().insert(k, v);
        }else{
            if (getRight() == null)
                right = createNode(k, v);
            else
                getRight().insert(k, v);
        }
    }

    public int search(int k){
        if(this.left == null && this.right == null && k == this.key){
            return this.val;
        }else if(k < this.key && this.left != null){
            return this.left.search(k);
        }else if(this.right != null){
            return this.right.search(k);
        }

        return -1;
    }

    public abstract TreeNode createNode(int k, int v);
}


