package pim.algorithm;

import pim.dpu.DPUGarbageCollector;
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
                if(deque.remove() != node.getKey()){
                    throw new RuntimeException();
                }
            }else{
                int addr = node.getKey();
                //System.out.println("addr = " + addr);
                int k = BytesUtils.readU4LittleEndian(heapMemory, addr + 8 );
                if(deque.remove() != k){
                    throw new RuntimeException();
                }
            }
            if(node.getLeft() != null) queue.add(node.getLeft());
            if(node.getRight() != null) queue.add(node.getRight());
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
            if(thisNode.getLeft() != null) queue.add(new TreeNode[]{thisNode, thisNode.getLeft()});
            if(thisNode.getRight() != null) queue.add(new TreeNode[]{thisNode, thisNode.getRight()});

            deque.add(thisNode.getKey());

            // whether convert this node to DPUTreeNode or not
            if(nodeInCPU < nodeAmountInCPU){
                nodeInCPU++;
                continue;
            }

            DPUTreeNode dpuNodeConverted = new DPUTreeNode(thisNode.getKey(), thisNode.getVal());
            dpuNodeConverted.setLeft(thisNode.getLeft());
            dpuNodeConverted.setRight(thisNode.getRight());
            dpuNodeConverted.setKey(heapPoint); // set dpu MRAM pt in key field

            writeKey(thisNode.getKey(), heapMemory, heapPoint); // write key
            writeValue(thisNode.getVal(), heapMemory, heapPoint); // write value
            writeClassReference(classAddress, heapMemory, heapPoint);

            // mark as forward
            thisNode.setKey(-1);
            thisNode.setVal(-1);
            thisNode.setLeft(dpuNodeConverted);

            if(parent == null) continue;

            // already be forward
            if(parent.getKey() == -1 && parent.getVal() == -1){
                DPUTreeNode treeNode = (DPUTreeNode) parent.getLeft();
                int parentAddress = treeNode.getKey();
                if(treeNode.getLeft() == thisNode){
                    treeNode.setLeft(dpuNodeConverted);
                 //   System.out.println("write left = " + heapPoint + "to instance " + parentAddress);
                    writeLeft(heapPoint, heapMemory, parentAddress);
                }

                if(treeNode.getRight() == thisNode){
                    treeNode.setRight(dpuNodeConverted);
                    //System.out.println("write right = " + heapPoint + "to instance " + parentAddress);
                   writeRight(heapPoint, heapMemory, parentAddress);
                }
            }else {
                // parent node not been forward
                if(parent.getVal() == -1){
                    System.out.println(parent.getKey());
                }
                if(parent.getLeft() == thisNode) parent.setLeft(dpuNodeConverted);
                if(parent.getRight() == thisNode) parent.setRight(dpuNodeConverted);
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
            if(node.getVal() == -1){
                TreeNode realNode = node.getLeft();
                if(parent.getLeft() == node) parent.setLeft(realNode);
                if(parent.getRight() == node) parent.setRight(realNode);
            }
            if(node.getLeft() != null) queue.add(new TreeNode[]{node, node.getLeft()});
            if(node.getRight() != null) queue.add(new TreeNode[]{node, node.getRight()});
        }
    }


}
