package pim.algorithm;

import com.upmem.dpu.DpuException;
import pim.ExperimentConfigurator;
import pim.UPMEM;
import pim.logger.Logger;
import pim.logger.PIMLoggers;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import static pim.algorithm.TreeWriter.convertCPUTreeToPIMTree;

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
    public static void serializeTreeToFile(TreeNode root, String filePath){
       try {
           FileWriter fw = new FileWriter(filePath);
           BufferedWriter bw = new BufferedWriter(fw);
           serialize(root, bw);
           bw.close();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }

    public static TreeNode buildCpuPartTreeFromFile(String filePath) throws IOException {
        TreeNode root = deserialize(filePath);

        return root;
    }
    public static TreeNode deserialize(String filePath) throws IOException {
        FileReader fr;
        try {
            fr = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedReader br = new BufferedReader(fr);
        char[] buffer = new char[4096];
        TreeNode result = (TreeNode) deserialize(br, buffer, 0)[0];
        br.close();
        fr.close();
        return result;
    }


    public static void increasePosition(BufferedReader br, char[] buffer, int[] context, int pos) throws IOException {
        if(pos + 1 >= context[CONTEXT_READ_BYTES] ){
            context[CONTEXT_READ_BYTES]  = br.read(buffer);
            context[CONTEXT_POS] = 0;
        }else {
            context[CONTEXT_POS] ++;
        }
    }
    public static Object[] deserialize(BufferedReader br, char[] buffer, int pos) throws IOException {
        int[] context = new int[2];
        context[CONTEXT_READ_BYTES] = br.read(buffer);
        TreeNode newNode = null;
        while(context[CONTEXT_READ_BYTES] > 0){
            pos = 0;
            while(buffer[pos] == '\r' || buffer[pos] == '\n'){
                increasePosition(br, buffer, context, pos);
            }
            if(buffer[pos] == '#'){
                increasePosition(br, buffer, context, pos);
                return new Object[]{null, pos, buffer};

            }

            // process data
            increasePosition(br, buffer, context, pos);
            char type = buffer[pos];
            StringBuilder keyString = new StringBuilder();
            StringBuilder valueString = new StringBuilder();
            increasePosition(br, buffer, context, pos);

            while(buffer[pos] != ','){
                keyString.append(buffer[pos]);
                increasePosition(br, buffer, context, pos);
            }

            increasePosition(br, buffer, context, pos);

            while(buffer[pos] != ' '){
                valueString.append(buffer[pos]);
                increasePosition(br, buffer, context, pos);
            }
            int key = Integer.parseInt(keyString.toString());
            int value = Integer.parseInt(valueString.toString());
            newNode = (type == '-' ? new CPUTreeNode(key, value) : new DPUTreeNodeProxyAutoGen(key, value));
            if(type != '-'){
                proxy ++;
            }
            Object[] left;
            Object[] right;

            increasePosition(br, buffer, context, pos);

            left = deserialize(br, buffer, pos);
            pos = (Integer) left[1];
            newNode.left = (TreeNode) left[0];

            increasePosition(br, buffer, context, pos);

            right = deserialize(br, buffer, pos);
            pos = (Integer) right[1];
            newNode.right = (TreeNode) right[0];
        }
        return new Object[]{newNode, pos, buffer};
    }


    public static void serialize(TreeNode root, BufferedWriter bw) throws IOException {
        String data;
        if (root == null) {
            bw.write("#");
            return;
        }
        if(root instanceof  DPUTreeNodeProxyAutoGen){
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

    /* build cpu tree from key-value pairs */
    public static TreeNode buildCPUTree(ArrayList<Pair<Integer, Integer>> pairs){
        if(pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            insertNewCPUNode(root, pairs.get(i).key, pairs.get(i).val);
        }
        System.out.println("build cpu tree finished");
        return root;
    }




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

    /* build cpu tree from file */
    public static TreeNode buildCPUTree(String filePath) {
        TreeNode root = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new ArrayList<BSTBuilder.Pair<Integer, Integer>>();
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


    public static TreeNode buildPIMTree(String filePath, int cpuLayerCount){
        try {
            for(int i = 0; i < UPMEM.dpuInUse; i++){
                UPMEM.getInstance().getDPUManager(i).dpuClassFileManager.loadClassForDPU(DPUTreeNode.class);
            }
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        TreeNode root = BSTBuilder.buildCPUTree(filePath);
        if(ExperimentConfigurator.serializeToFile){
            serializeTreeToFile(root, "CPU_TREE_" + ExperimentConfigurator.totalNodeCount);
            System.out.println("serialize CPU TREE");
        }
        convertCPUTreeToPIMTree(root, cpuLayerCount);

        return root;
    }

    public static TreeNode buildPIMTree(ArrayList<Pair<Integer, Integer>> pairs){
        try {
            for(int i = 0; i < ExperimentConfigurator.dpuInUse; i++){
                UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
            }
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, ExperimentConfigurator.cpuLayerCount);

        return root;
    }

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
