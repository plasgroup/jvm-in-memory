package pim.algorithm;

import pim.UPMEM;

public class CPUTreeNode extends TreeNode {
    final int CriticalHeight = 10;
    int height;

    public CPUTreeNode(int k, int v) {
        this(k, v, 1);
    }

    public CPUTreeNode(int k, int v, int height) {
        super(k, v);
        this.height = height;
    }


    @Override
    public TreeNode createNode(int k, int v) {
        if (height >= this.CriticalHeight) {
            System.out.println("create node at DPU from CPU.");
            return (TreeNode) UPMEM.getInstance().createObject(allocateDPU(), DPUTreeNode.class, k, v);
        } else {
            System.out.println("create node at CPU. new height = " + (this.height + 1));
            return new CPUTreeNode(k, v, this.height + 1);
        }
    }

    int allocateDPU(){
        if(true) return 1;
        for (int i = 0; i < UPMEM.TOTAL_DPU_COUNT; i++) {
            int remainHeapMemory = UPMEM.getInstance().getDPUHeapMemoryRemain(i);
            if (remainHeapMemory > 32) {
                return i;
            }
        }
        return -1;
    }


}
