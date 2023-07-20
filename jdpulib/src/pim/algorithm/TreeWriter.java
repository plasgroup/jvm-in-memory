package pim.algorithm;

import pim.dpu.DPUGarbageCollector;
import pim.dpu.DPUObjectHandler;
import pim.utils.BytesUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class TreeWriter {
    final static int INSTANCE_SIZE = 24;
    final static int KEY_POS = 0;
    final static int VALUE_POS = 1;
    final static int LEFT_POS = 2;
    final static int RIGHT_POS = 3;

    static void writeKey(int key, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, key, instanceAddress + 8 + 4 * KEY_POS);
    }
    static void writeValue(int val, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, val, instanceAddress + 8 + 4 * VALUE_POS);
    }
    static void writeLeft(int left, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, left, instanceAddress + 8 + 4 * LEFT_POS);
    }
    static void writeRight(int right, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, right, instanceAddress + 8 + 4 * RIGHT_POS);
    }

    static void writeClassReference(int classReference, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, classReference, instanceAddress + 4);
    }

    static Deque<Integer> deque = new ArrayDeque<>();

    static byte[] heapMemory;

    public static void verifyLargePIMTree(TreeNode root){
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while(queue.size() > 0){
            TreeNode node = queue.remove();
            if(node instanceof CPUTreeNode){
                if(deque.remove() != node.key){
                    throw new RuntimeException();
                }
            }else{
                int addr = node.key;
                int k = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 );
                if(deque.remove() != k){
                    throw new RuntimeException();
                }
            }
            if(node.left != null) queue.add(node.left);
            if(node.right != null) queue.add(node.right);

        }

        // verify tree nodes in heap memory are built correctly
        Queue<Integer> dpuTreeNodeAddressQueue = new ArrayDeque<>();
        dpuTreeNodeAddressQueue.add(8);
        while(dpuTreeNodeAddressQueue.size() > 0){
            int addr = dpuTreeNodeAddressQueue.remove();
            int left = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * LEFT_POS);
            int right = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * RIGHT_POS);
            int key = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * KEY_POS);
            if(left != 0) dpuTreeNodeAddressQueue.add(left);
            if(right != 0) dpuTreeNodeAddressQueue.add(right);
            if(left != 0){
                int leftKey = BytesUtils.readU4LittleEndian(heapMemory, left + 8 + 4 * KEY_POS);
                if(leftKey > key) throw new RuntimeException("exist a left node's key greater than current node");
            }
            if(right != 0){
                int rightKey = BytesUtils.readU4LittleEndian(heapMemory, right + 8 + 4 * KEY_POS);
                if(rightKey < key) throw new RuntimeException("exist a left node's key smaller than current node");
            }
        }
    }
    static void convertCPUTreeToPIMTree(TreeNode root, int totalTreeNodeCount, int nodeAmountInCPU, int classAddress){
        int nodeInDPU = 0;
        int nodeInCPU = 0;
        int heapPoint = DPUGarbageCollector.heapSpaceBeginAddr + 8;

        heapMemory = new byte[(totalTreeNodeCount - nodeAmountInCPU) * INSTANCE_SIZE + 8];
        TreeNode point = root;
        Queue<TreeNode[]> queue = new ArrayDeque<>();
        queue.add(new TreeNode[]{null, point});

        while(queue.size() > 0){
            TreeNode[] record = queue.remove();
            TreeNode thisNode = record[1];
            TreeNode parent = record[0];
            if(thisNode.left != null) queue.add(new TreeNode[]{thisNode, thisNode.left});
            if(thisNode.right != null) queue.add(new TreeNode[]{thisNode, thisNode.right});

            deque.add(thisNode.key);

            // if currently nodes in DPU not reach the limitation
            if(nodeInCPU < nodeAmountInCPU){
                nodeInCPU++;
                continue;
            }

            // convert to DPUTreeNodeProxy
            DPUTreeNode dpuNodeConverted =  new DPUTreeNodeProxyAutoGen(thisNode.key, thisNode.val);
            ((DPUTreeNodeProxyAutoGen)dpuNodeConverted).objectHandler = new DPUObjectHandler(0, heapPoint);

            dpuNodeConverted.left = thisNode.left;
            dpuNodeConverted.right = thisNode.right;
            dpuNodeConverted.key = heapPoint; // set dpu MRAM pt in key field

            writeKey(thisNode.key, heapMemory, heapPoint); // write key
            writeValue(thisNode.val, heapMemory, heapPoint); // write value
            writeClassReference(classAddress, heapMemory, heapPoint);

            // mark as forward
            thisNode.key = -1;
            thisNode.val = -1;
            // store forward reference in left field
            thisNode.left = dpuNodeConverted;

            if(parent == null) continue;

            // if the parent node already be forward
            if(parent.key == -1 && parent.val == -1){
                // write this node to the current real parent node's left/right field
                DPUTreeNode realNode = (DPUTreeNode) parent.left;
                int parentAddress = realNode.key;
                if(realNode.left == thisNode){
                    realNode.left = dpuNodeConverted;
                    writeLeft(heapPoint, heapMemory, parentAddress);
                }

                if(realNode.right == thisNode){
                    realNode.right = dpuNodeConverted;
                   writeRight(heapPoint, heapMemory, parentAddress);
                }
            }else {
                // parent node not been forward, simply set to the parent's left/right field
                if(parent.left == thisNode) parent.left = dpuNodeConverted;
                if(parent.right == thisNode) parent.right = dpuNodeConverted;
            }
            heapPoint += INSTANCE_SIZE;
            nodeInDPU++;
        }

        if(nodeInCPU + nodeInDPU != totalTreeNodeCount){
            throw new RuntimeException();
        }

        // repair
        queue.add(new TreeNode[]{null, root});
        while(queue.size() > 0){
            TreeNode[] record = queue.remove();
            TreeNode node = record[1];
            TreeNode parent = record[0];
            if(node.val == -1){
                TreeNode realNode = node.left;
                if(parent.left == node) parent.left = realNode;
                if(parent.right == node) parent.right = realNode;
            }
            if(node.left != null) queue.add(new TreeNode[]{node, node.left});
            if(node.right != null) queue.add(new TreeNode[]{node, node.right});
        }


    }


}
