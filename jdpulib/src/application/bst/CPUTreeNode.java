package application.bst;

import framework.pim.UPMEM;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

public class CPUTreeNode extends TreeNode {
    final int CriticalHeight = 10;
    int height;
    static Logger cpuTreeNodeLogger = PIMLoggers.cpuTreeNodeLogger;
    private static int dpuNode = 0;
    private static int cpuNode = 0;

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
            cpuTreeNodeLogger.logln("create node at DPU from CPU.");
            dpuNode++;
            int dpu = allocateDPU();
            return (TreeNode) UPMEM.getInstance().createObject(dpu, DPUTreeNode.class, k, v);
        } else {
            cpuNode++;
            cpuTreeNodeLogger.logln("create node at CPU. new height = " + (this.height + 1));

            return new CPUTreeNode(k, v, this.height + 1);
        }
    }

    public TreeNode createNodeCPU(int k, int v) {
        return new CPUTreeNode(k, v);
    }

    int allocateDPU() {

        for (int i = 0; i < UPMEM.TOTAL_DPU_COUNT; i++) {
            int remainHeapMemory = UPMEM.getInstance().getDPUHeapMemoryRemain(i);
            if (remainHeapMemory > 32) {
                return i;
            }
        }
        return -1;
    }

}
