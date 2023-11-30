package application.bst;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.dpu.DPUGarbageCollector;
import framework.pim.dpu.java_strut.DPUJVMMemSpaceKind;
import framework.pim.utils.BytesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class TreeWriter {
    final static int INSTANCE_SIZE = 24;
    final static int KEY_POS = 0;
    final static int VALUE_POS = 1;
    final static int LEFT_POS = 2;
    final static int RIGHT_POS = 3;
    final static int DPU_MAX_NODES_COUNT = 2000000;
    static Deque<Integer> deque = new ArrayDeque<>();
    static byte[] heapMemory;


    /** write an instance's field value to the DPU's heap memory **/
    static void writeKey(int key, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, key, instanceAddress + 8 + 4 * KEY_POS);
    }
    static void writeValue(int val, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, val, instanceAddress + 8 + 4 * VALUE_POS);
    }
    static void writeLeft(int left, byte[] heap, int instanceAddress){
        if(left == 0)
            BytesUtils.writeU4LittleEndian(heap, 0, instanceAddress + 8 + 4 * LEFT_POS);
        else
            BytesUtils.writeU4LittleEndian(heap, DPUGarbageCollector.heapSpaceBeginAddr + left, instanceAddress + 8 + 4 * LEFT_POS);
    }
    static void writeRight(int right, byte[] heap, int instanceAddress){
        if(right == 0)
            BytesUtils.writeU4LittleEndian(heap, 0, instanceAddress + 8 + 4 * RIGHT_POS);
        else
            BytesUtils.writeU4LittleEndian(heap, DPUGarbageCollector.heapSpaceBeginAddr + right, instanceAddress + 8 + 4 * RIGHT_POS);
    }

    static void writeClassReference(int classReference, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, classReference, instanceAddress + 4);
    }

    public static void writeDPUImages(int totalNodeCount, String imagesPath) {
        int i = 0;
        while(true){
            String filePath = imagesPath + "[" + totalNodeCount + "]DPU#" + i + ".img";
            File imgI = new File(filePath);
            if(!imgI.exists()) return;
            System.out.println("load image to DPU#" + i + " from file " + filePath);
            try (FileInputStream inputStream = new FileInputStream(imagesPath + "[" + totalNodeCount + "]DPU#" + i + ".img")) {
                byte[] bs = inputStream.readAllBytes();
                UPMEM.getInstance().getDPUManager(i).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_HEAPSPACE,2000000 * INSTANCE_SIZE);
                UPMEM.getInstance().getDPUManager(i).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE,  bs, DPUGarbageCollector.heapSpaceBeginAddr);
                UPMEM.getInstance().getDPUManager(i).garbageCollector.updateHeapPointerToDPU();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            i++;
        }
    }

    public static void verifyPIMTree(TreeNode root, byte[][] heapMemory){
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        Dictionary<Integer, List<Integer>> rootList = new Hashtable<>();
        while(queue.size() > 0){
            TreeNode node = queue.remove();
            if(node instanceof CPUTreeNode){
                if(deque.remove() != node.key){
                    throw new RuntimeException();
                }
            }else{
                int addr = node.key;
                int dpuID = ((DPUTreeNodeProxyAutoGen) node).dpuID;
                if(rootList.get(dpuID) == null) rootList.put(dpuID, new ArrayList<>());
                rootList.get(dpuID).add(addr);
                int k = BytesUtils.readU4LittleEndian(heapMemory[dpuID], addr + 8);
                // See whether the key in the DPU heap is equal to the DPUTreeNodeProxy's key
                if(deque.remove() != k){
                    throw new RuntimeException();
                }
            }
            if(node.left != null)
                queue.add(node.left);
            if(node.right != null)
                queue.add(node.right);
        }

        // Verify tree nodes in heap memory are built correctly
        for(int dpuID = 0; dpuID < heapMemory.length; dpuID++){

            Queue<Integer> dpuTreeNodeAddressQueue = new ArrayDeque<>();
            List<Integer> roots = rootList.get(dpuID);
            for(Integer rootAddress : roots){
                dpuTreeNodeAddressQueue.add(rootAddress);
            }

            while(dpuTreeNodeAddressQueue.size() > 0){
                int addr = dpuTreeNodeAddressQueue.remove();
                int left = BytesUtils.readU4LittleEndian(heapMemory[dpuID], addr + 8 + 4 * LEFT_POS);
                int right = BytesUtils.readU4LittleEndian(heapMemory[dpuID], addr + 8 + 4 * RIGHT_POS);
                int key = BytesUtils.readU4LittleEndian(heapMemory[dpuID], addr + 8 + 4 * KEY_POS);
                if(left != 0) dpuTreeNodeAddressQueue.add(left);
                if(right != 0) dpuTreeNodeAddressQueue.add(right);
                if(left != 0){
                    int leftKey = BytesUtils.readU4LittleEndian(heapMemory[dpuID], left + 8 + 4 * KEY_POS);
                    if(leftKey > key) throw new RuntimeException("exist a left node's key greater than current node");
                }
                if(right != 0){
                    int rightKey = BytesUtils.readU4LittleEndian(heapMemory[dpuID], right + 8 + 4 * KEY_POS);
                    if(rightKey < key) throw new RuntimeException("exist a left node's key smaller than current node");
                }
            }
        }

    }


    /* Output image to File */
    static void outputImage(int dpuID){
        if(ExperimentConfigurator.serializeToFile){
            try (FileOutputStream outputStream = new FileOutputStream(ExperimentConfigurator.imagesPath + "[" + ExperimentConfigurator.totalNodeCount + "]" + "DPU#" + dpuID + ".img")) {
                outputStream.write(heapMemory);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    /* Convert a CPU tree to PIM tree, with keeping first cpuLayerCount layers in CPU */
    static void convertCPUTreeToPIMTree(TreeNode root, int cpuLayerCount){
        TreeNode point = root;
        Queue<TreeNode[]> queue = new ArrayDeque<>();
        queue.add(new TreeNode[]{null, point});

        int currentLayer = 0;
        int cpuNode = 0;
        int cpuProxyNode = 0;

        // first cpuLayerCount layers retain on CPU
        while(currentLayer < cpuLayerCount){
            int size = queue.size();
            for(int i = 0; i < size; i++){
                TreeNode[] record = queue.remove();
                TreeNode thisNode = record[1];
                if(thisNode.left != null) queue.add(new TreeNode[]{thisNode, thisNode.left});
                if(thisNode.right != null) queue.add(new TreeNode[]{thisNode, thisNode.right});
                cpuNode ++;
            }
            currentLayer ++;
        }

        System.out.println("cpu nodes (top k layers) " + cpuNode);

        heapMemory = new byte[DPU_MAX_NODES_COUNT * INSTANCE_SIZE + 8];
        int currentChildrenCount = 0;
        int dpuID = 0;
        int currentHeapAddr = 8;

        // move subtree from (cpuLayerCount + 1) layers to a certain DPU
        while(queue.size() > 0){
            TreeNode[] record = queue.remove();
            TreeNode thisNode = record[1];
            TreeNode parent = record[0];
            int c = getTreeSize(thisNode);

            /* when a nodes' tree size exceed the DPU capacity limitation, retain
             this node in CPU, but put its children into workspace
             */
            if(c > DPU_MAX_NODES_COUNT){
                cpuNode++;
                if(thisNode.right != null)
                    queue.add(new TreeNode[]{thisNode, thisNode.right});
                if(thisNode.left != null)
                    queue.add(new TreeNode[]{thisNode, thisNode.left});
                continue;
            }


            int classAddress = UPMEM.getInstance().getDPUManager(dpuID)
                    .classCacheManager.getClassStrutCacheLine("pim/algorithm/DPUTreeNode").marmAddr;


            // create and write images to DPUs
            if(currentChildrenCount + c <= DPU_MAX_NODES_COUNT){
                DPUTreeNodeProxyAutoGen dpuTreeNodeProxyAutoGen =
                        new DPUTreeNodeProxyAutoGen(thisNode.key, thisNode.val);
                currentChildrenCount += c;
                cpuProxyNode++;
                dpuTreeNodeProxyAutoGen.dpuID = dpuID;
                dpuTreeNodeProxyAutoGen.address = DPUGarbageCollector.heapSpaceBeginAddr + currentHeapAddr;
                dpuTreeNodeProxyAutoGen.left = null;
                dpuTreeNodeProxyAutoGen.right = null;

                // write heap
                int[] res = writeSubTreeBytes(currentHeapAddr, thisNode, heapMemory, classAddress);
                currentHeapAddr = res[0];

                if(parent.left == thisNode){
                    parent.left = dpuTreeNodeProxyAutoGen;
                }else if(parent.right == thisNode){
                    parent.right = dpuTreeNodeProxyAutoGen;
                }
            }else{
                System.out.println("write image to DPU " + dpuID + " children count = " + currentChildrenCount + " heap_pt_current = " + currentHeapAddr);

                // flush
                writeHeapImageToDPU(dpuID);
                outputImage(dpuID);
                dpuID++;

                // reset
                currentHeapAddr = 8;
                Arrays.fill(heapMemory, (byte) 0);

                // add new node
                currentChildrenCount = c;

                DPUTreeNodeProxyAutoGen dpuTreeNodeProxyAutoGen =
                        new DPUTreeNodeProxyAutoGen(thisNode.key, thisNode.val);

                cpuProxyNode++;
                dpuTreeNodeProxyAutoGen.dpuID = dpuID;
                dpuTreeNodeProxyAutoGen.address = DPUGarbageCollector.heapSpaceBeginAddr + currentHeapAddr;
                dpuTreeNodeProxyAutoGen.left = null;
                dpuTreeNodeProxyAutoGen.right = null;

                currentHeapAddr = writeSubTreeBytes(currentHeapAddr, thisNode, heapMemory, classAddress)[0];
                if(parent.left == thisNode){
                    parent.left = dpuTreeNodeProxyAutoGen;
                }else if(parent.right == thisNode){
                    parent.right = dpuTreeNodeProxyAutoGen;
                }
            }
        }
        if(currentHeapAddr != 8){
            System.out.println("write image to DPU " + dpuID + " children count = " + currentChildrenCount + " heap_pt_current = " + currentHeapAddr);
            writeHeapImageToDPU(dpuID);
            outputImage(dpuID);
        }
        System.out.println(cpuNode + " cpu nodes (final) and " + cpuProxyNode + " proxy in CPU");
        int size = getTreeSize(root);
        System.out.println("cpu part size = " + size);
    }

    /* Write the heap memory bytes array to a DPU */
    private static void writeHeapImageToDPU(int dpuID) {
        UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.allocate(DPUJVMMemSpaceKind.DPU_HEAPSPACE,2000000 * INSTANCE_SIZE);
        UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.transfer(DPUJVMMemSpaceKind.DPU_HEAPSPACE, heapMemory, DPUGarbageCollector.heapSpaceBeginAddr);
        UPMEM.getInstance().getDPUManager(dpuID).garbageCollector.updateHeapPointerToDPU();
    }

    /* Write the tree represented by thisNode to the heap memory, write from 'heapAddress'*/
    private static int[] writeSubTreeBytes(int heapAddress, TreeNode thisNode, byte[] heapMemory, int classAddress) {
        if(thisNode == null) return new int[]{heapAddress, 0, 0};
        // write this node to heap
        int thisNodeAddress = heapAddress;
        writeKey(thisNode.key, heapMemory, thisNodeAddress);
        writeValue(thisNode.val, heapMemory, thisNodeAddress);
        writeClassReference(classAddress, heapMemory, thisNodeAddress);
        heapAddress += INSTANCE_SIZE;

        int[] res;
        int l = 0;
        int r = 0;
        if(thisNode.left != null){
            // recursive left
            l = heapAddress;
            writeLeft(heapAddress, heapMemory, thisNodeAddress);
            res =  writeSubTreeBytes(heapAddress, thisNode.left, heapMemory, classAddress);
            heapAddress = res[0];
        }else{
            writeLeft(0, heapMemory, thisNodeAddress);
        }

        if(thisNode.right != null){
            // recursive right
            r = heapAddress;
            writeRight(heapAddress, heapMemory, thisNodeAddress);
            res = writeSubTreeBytes(heapAddress, thisNode.right, heapMemory, classAddress);
            heapAddress = res[0];
        }else{
            writeRight(0, heapMemory, thisNodeAddress);
        }
        return new int[]{heapAddress, l, r};
    }

    public static int getTreeSize(TreeNode thisNode) {
        if(thisNode == null) return 0;
        return 1 + getTreeSize(thisNode.left) + getTreeSize(thisNode.right);
    }


}
