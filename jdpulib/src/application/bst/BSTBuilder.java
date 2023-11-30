package application.bst;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.logger.Logger;
import framework.pim.logger.PIMLoggers;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import static framework.pim.ExperimentConfigurator.totalNodeCount;
import static application.bst.TreeWriter.convertCPUTreeToPIMTree;

public class BSTBuilder {
    static Logger bstBuildingLogger = PIMLoggers.bstBuildingLogger;
    static int proxy = 0;
    static final int CONTEXT_POS = 0;
    static final int CONTEXT_READ_BYTES = 1;

    public static class Pair<K, V> {
        K key;
        V val;

        public Pair(K k, V v) {
            this.key = k;
            this.val = v;
        }

        public K getKey() {
            return key;
        }

        public V getVal() {
            return val;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public void setVal(V val) {
            this.val = val;
        }
    }

    static abstract class BaseKeyValuePairGenerator<K, V> {
        protected abstract Pair<K, V> generatePair();
        public ArrayList<Pair<K, V>> generatePairs(int pairCount) {
            HashSet<K> keys = new HashSet<>();
            ArrayList<Pair<K, V>> result = new ArrayList<>();
            while(result.size() < pairCount){
                Pair<K, V> kvPair = generatePair();
                if(keys.contains(kvPair.key))
                    continue;
                keys.add(kvPair.key);
                result.add(kvPair);
            }
            return result;
        }
    }

    /** serialize tree to file **/
    public static void serializeTreeToFile(TreeNode root, String filePath){
       try {
           FileWriter fw = new FileWriter(filePath);
           BufferedWriter bw = new BufferedWriter(fw);
           serialize(root, bw);
           bw.close();
           fw.close();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }


    public static TreeNode buildCpuPartTreeFromFile(String filePath) throws IOException {
        TreeNode root = deserialize(filePath);
        return root;
    }


    /** deserialize tree from file **/
    public static TreeNode deserialize(String filePath) throws IOException {
        FileReader fr;
        try {
            fr = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedReader br = new BufferedReader(fr);
        char[] buffer = new char[4096];
        int[] context = new int[2];
        TreeNode result = (TreeNode) deserialize(br, buffer, context)[0];
        br.close();
        fr.close();
        return result;
    }


    /** helper function in the process of serializing tree **/
    private static void increasePosition(BufferedReader br, char[] buffer, int[] context) throws IOException {
        if(context[CONTEXT_POS] + 1 >= context[CONTEXT_READ_BYTES]){
            context[CONTEXT_READ_BYTES]  = br.read(buffer);
            context[CONTEXT_POS] = 0;
        }else {
            context[CONTEXT_POS] ++;
        }
    }

    public static int nodes = 0;


    /** deserialize a tree to buffer **/
    public static Object[] deserialize(BufferedReader br, char[] buffer, int[] context) throws IOException {
        TreeNode newNode;
        if(context[CONTEXT_POS] >= context[CONTEXT_READ_BYTES]){
            context[CONTEXT_READ_BYTES] = br.read(buffer);
            context[CONTEXT_POS] = 0;
        }

        if(context[CONTEXT_READ_BYTES] == 0) return null;
            while(buffer[context[CONTEXT_POS]] == '\r' || buffer[context[CONTEXT_POS]] == '\n'){
                increasePosition(br, buffer, context);
            }
            if(buffer[context[CONTEXT_POS]] == '#'){
                increasePosition(br, buffer, context);
                return new Object[]{null, context[CONTEXT_POS], buffer};
            }

            // process data
            char type = buffer[context[CONTEXT_POS]];

            increasePosition(br, buffer, context);
            StringBuilder keyString = new StringBuilder();
            StringBuilder valueString = new StringBuilder();
            StringBuilder dpuIDString = new StringBuilder();
            StringBuilder mramAddressString = new StringBuilder();
            increasePosition(br, buffer, context);

            while(buffer[context[CONTEXT_POS]] != ','){
                keyString.append(buffer[context[CONTEXT_POS]]);
                increasePosition(br, buffer, context);
            }

            increasePosition(br, buffer, context);


            /** meet a proxy node **/
            if(type == 'p'){
                while(buffer[context[CONTEXT_POS]] != ','){
                    valueString.append(buffer[context[CONTEXT_POS]]);
                    increasePosition(br, buffer, context);
                }
                increasePosition(br, buffer, context); // ,
                while(buffer[context[CONTEXT_POS]] != ','){
                    dpuIDString.append(buffer[context[CONTEXT_POS]]);
                    increasePosition(br, buffer, context);
                }
                increasePosition(br, buffer, context); //,
                while(buffer[context[CONTEXT_POS]] != ' '){
                    mramAddressString.append(buffer[context[CONTEXT_POS]]);
                    increasePosition(br, buffer, context);
                }
            }else{
                while(buffer[context[CONTEXT_POS]] != ' '){
                    valueString.append(buffer[context[CONTEXT_POS]]);
                    increasePosition(br, buffer, context);
                }
            }

            int key = Integer.parseInt(keyString.toString());
            int value = Integer.parseInt(valueString.toString());

            if(type == '-'){
                newNode = new CPUTreeNode(key, value);
            }else{
                int dpuID = Integer.parseInt(dpuIDString.toString());
                int address = Integer.parseInt(mramAddressString.toString());

                newNode = new DPUTreeNodeProxyAutoGen(key, value, dpuID, address);
            }

            nodes++;
            if(type != '-'){
                proxy ++;
            }

            Object[] left;
            Object[] right;

            increasePosition(br, buffer, context); // space ' '

            left = deserialize(br, buffer, context);
            newNode.left = (TreeNode) left[0];

            increasePosition(br, buffer, context); // space ' '

            right = deserialize(br, buffer, context);
            newNode.right = (TreeNode) right[0];

        return new Object[]{newNode, context[CONTEXT_POS], buffer};
    }



    /** serialize a tree to buffer **/
    public static void serialize(TreeNode root, BufferedWriter bw) throws IOException {
        String data;
        if (root == null) {
            bw.write("#");
            return;
        }
        if(root instanceof DPUTreeNodeProxyAutoGen){
            data = "p" + "," + root.key + ","  + root.val + "," +
                    ((DPUTreeNodeProxyAutoGen) root).dpuID + "," + ((DPUTreeNodeProxyAutoGen) root).address;
        }else{
            data = "-" + "," + root.key + ","  + root.val;
        }
        bw.write(data);
        bw.write(" ");
        serialize(root.left, bw);
        bw.write(" ");
        serialize(root.right, bw);
    }

    /** build CPU Tree from pairs **/
    public static TreeNode buildCPUTree(ArrayList<Pair<Integer, Integer>> pairs){
        if(pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            insertNewCPUNode(root, pairs.get(i).key, pairs.get(i).val);
        }
        System.out.println("build cpu tree finished");
        return root;
    }




    /** insert CPU TreeNode **/
    public static void insertNewCPUNode(TreeNode node, int k, int v){
        if(k < node.getKey()){
            if (node.getLeft() == null)
                node.setLeft(new CPUTreeNode(k, v));
            else
                insertNewCPUNode(node.getLeft(), k, v);
        }else{
            if (node.getRight() == null)
                node.setRight(new CPUTreeNode(k, v));
            else
                insertNewCPUNode(node.getRight(), k, v);
        }
    }

    /** build CPU tree from file **/
    public static TreeNode buildCPUTree(String filePath) {
        TreeNode root = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new ArrayList<>();
            String s = br.readLine();
            while(s != null){
                int k = Integer.parseInt(s.split(" ")[0]);
                int v = Integer.parseInt(s.split(" ")[1]);
                if(root == null) {
                    root = new CPUTreeNode(k, v);
                }
                else{
                    insertNewCPUNode(root, k, v);
                }
                pairs.add(new BSTBuilder.Pair<>(k, v));
                s = br.readLine();
            }
            System.out.println("build cpu tree finished");
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }


    /** build PIM BST from file, distributing cpuLayerCount layers in CPU side at most **/
    public static TreeNode buildPIMTree(String filePath, int cpuLayerCount){
        for(int i = 0; i < UPMEM.dpuInUse; i++){
            UPMEM.getInstance().getDPUManager(i).dpuClassFileManager.loadClassForDPU(DPUTreeNode.class);
        }

        TreeNode root = BSTBuilder.buildCPUTree(filePath);
        if(ExperimentConfigurator.serializeToFile){
            serializeTreeToFile(root, "CPU_TREE_" + totalNodeCount + ".txt");
        }
        convertCPUTreeToPIMTree(root, cpuLayerCount);

        return root;
    }


    /** build BST from pairs **/
    public static TreeNode buildPIMTree(ArrayList<Pair<Integer, Integer>> pairs){
        try {
            for(int i = 0; i < ExperimentConfigurator.dpuInUse; i++){
                // init each DPU
                UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, 0, 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, ExperimentConfigurator.cpuLayerCount);

        return root;
    }



    /** build BST by inserting nodes one by one **/
    public static TreeNode buildPIMTreeByInsert(ArrayList<Pair<Integer, Integer>> pairs) {
        if (pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        bstBuildingLogger.logf("(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", 1, pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            bstBuildingLogger.logf( "(TreeBuilder) ===> insert %d 'th node, key = %d, val = %d\n", i + 1, pairs.get(i).key, pairs.get(i).val);
            root.insert(pairs.get(i).key, pairs.get(i).val);
        }
        return root;
    }
}
