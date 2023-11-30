package application.bst;

import java.io.Serializable;

public abstract class TreeNode implements Serializable {
    public int key;
    public int val;
    public TreeNode left;
    public TreeNode right;

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

    public void insert(int k, int v){
        if(k < getKey()){
            if (getLeft() == null)
                setLeft(createNode(k, v));
            else
                getLeft().insert(k, v);
        }else{
            if (getRight() == null)
                setRight(createNode(k, v));
            else
                getRight().insert(k, v);
        }
    }

    public int search(int k){
        if(k == getKey()) return getVal();
        if(k < getKey() && getLeft() != null){
            return getLeft().search(k);
        }else if(k >= getKey() && getRight() != null){
            return getRight().search(k);
        }
        return -1;
    }

    public abstract TreeNode createNode(int k, int v);


}


