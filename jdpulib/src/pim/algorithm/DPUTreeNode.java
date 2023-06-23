package pim.algorithm;

public class DPUTreeNode extends TreeNode {
    public DPUTreeNode(int k, int v) {
        super(k, v);
    }

    @Override
    public TreeNode createNode(int k, int v) {
        System.out.println("create node at DPU.");
        return new DPUTreeNode(k, v);
    }
}
