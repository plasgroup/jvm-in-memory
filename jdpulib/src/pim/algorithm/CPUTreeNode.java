package pim.algorithm;

import pim.UPMEM;
import pim.dpu.DPUJClass;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

public class CPUTreeNode extends TreeNode {
    final int CriticalHeight = 10;
    int height;
    static Logger cpuTreeNodeLogger = PIMLoggers.cpuTreeNodeLogger;

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
            return (TreeNode) UPMEM.getInstance().createObject(allocateDPU(), DPUTreeNode.class, k, v);
        } else {
            cpuTreeNodeLogger.logln("create node at CPU. new height = " + (this.height + 1));
            return new CPUTreeNode(k, v, this.height + 1);
        }
    }

    public TreeNode createNodeCPU(int k, int v){
        return new CPUTreeNode(k, v);
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
