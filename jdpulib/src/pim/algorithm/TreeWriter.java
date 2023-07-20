package pim.algorithm;

import pim.dpu.DPUGarbageCollector;
import pim.dpu.DPUObjectHandler;
import pim.utils.BytesUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class TreeWriter {

    static void writeKey(int key, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, key, instanceAddress + 8 + 4 * 0);
    }
    static void writeValue(int val, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, val, instanceAddress + 8 + 4 * 1);
    }
    static void writeLeft(int left, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, left, instanceAddress + 8 + 4 * 2);
    }
    static void writeRight(int right, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, right, instanceAddress + 8 + 4 * 3);
    }

    static void writeClassReference(int classReference, byte[] heap, int instanceAddress){
        BytesUtils.writeU4LittleEndian(heap, classReference, instanceAddress + 4);
    }

    static Deque<Integer> deque = new ArrayDeque<>();

    static byte[] heapMemory;

    public static void verifyLargePIMTree(TreeNode root){
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        int i = 1;
        while(queue.size() > 0){
            //System.out.print("#" + i);
            TreeNode node = queue.remove();
            if(node instanceof CPUTreeNode){
                if(deque.remove() != node.key){
                    throw new RuntimeException();
                }
            }else{
                int addr = node.key;
                //System.out.println("addr = " + addr);
                int k = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 );
                if(deque.remove() != k){
                    throw new RuntimeException();
                }
            }
            if(node.left != null) queue.add(node.left);
            if(node.right != null) queue.add(node.right);
            i++;
        }

        Queue<Integer> dpuTreeNodeAddressQueue = new ArrayDeque<>();
        dpuTreeNodeAddressQueue.add(8);
        while(dpuTreeNodeAddressQueue.size() > 0){
            int addr = dpuTreeNodeAddressQueue.remove();
            //System.out.println(addr);
            int left = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * 2);
            int right = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * 3);
            int key = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 + 4 * 0);
            if(left != 0) dpuTreeNodeAddressQueue.add(left);
            if(right != 0) dpuTreeNodeAddressQueue.add(right);
            if(left != 0){
                int leftKey = BytesUtils.readU4LittleEndian(heapMemory, left + 8 + 4 * 0);
                if(leftKey > key) throw new RuntimeException("exist a left node's key greater than current node");
            }
            if(right != 0){
                int rightKey = BytesUtils.readU4LittleEndian(heapMemory, right + 8 + 4 * 0);
                if(rightKey < key) throw new RuntimeException("exist a left node's key smaller than current node");
            }
        }
    }
    static void convertCPUTreeToPIMTree(TreeNode root, int totalTreeNodeCount, int nodeAmountInCPU, int classAddress){
        int nodeInDPU = 0;
        int nodeInCPU = 0;
        int heapPoint = DPUGarbageCollector.heapSpaceBeginAddr + 8;

        heapMemory = new byte[(totalTreeNodeCount - nodeAmountInCPU) * 24 + 8];
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

            // whether convert this node to DPUTreeNode or not
            if(nodeInCPU < nodeAmountInCPU){
                nodeInCPU++;
                continue;
            }


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
            thisNode.left = dpuNodeConverted;

            if(parent == null) continue;

            // already be forward
            if(parent.key == -1 && parent.val == -1){
                DPUTreeNode treeNode = (DPUTreeNode) parent.left;
                int parentAddress = treeNode.key;
                if(treeNode.left == thisNode){
                    treeNode.left = dpuNodeConverted;
                 //   System.out.println("write left = " + heapPoint + "to instance " + parentAddress);
                    writeLeft(heapPoint, heapMemory, parentAddress);
                }

                if(treeNode.right == thisNode){
                    treeNode.right = dpuNodeConverted;
                    //System.out.println("write right = " + heapPoint + "to instance " + parentAddress);
                   writeRight(heapPoint, heapMemory, parentAddress);
                }
            }else {
                // parent node not been forward
                if(parent.left == thisNode) parent.left = dpuNodeConverted;
                if(parent.right == thisNode) parent.right = dpuNodeConverted;
            }
            heapPoint += 24;
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
