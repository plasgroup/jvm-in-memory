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
       StringBuilder sb = new StringBuilder();


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
        FileReader fr = null;
        try {
            fr = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedReader br = new BufferedReader(fr);
        TreeNode root = deserialize(br);

        return root;
    }
    static int proxy = 0;
    public static TreeNode deserialize(BufferedReader br) throws IOException {
        // TODO, BUG
        char ch = (char) br.read();
        if(ch == -1) return null;
        while(ch == '\r' || ch == '\n'){
            ch = (char) br.read();
        }
        if(ch == '#'){
            // currently in a space
            br.read();
            // to next
            return null;
        }

        if(ch == 'p'){
            System.out.println();
        }
        // process data
        char type = ch;
        StringBuilder keyString = new StringBuilder();
        StringBuilder valueString = new StringBuilder();
        StringBuilder dpuIDString = new StringBuilder();
        StringBuilder mramAddressString = new StringBuilder();

        br.read();

        while((ch = (char) br.read()) != ','){
            keyString.append(ch);
        }

        if(type == 'p'){
            while((ch = (char) br.read())  != ','){
                valueString.append(ch);
            }
            while((ch = (char) br.read())  != ','){
                dpuIDString.append(ch);
            }
            while ((ch = (char) br.read())   != ' '){
                mramAddressString.append(ch);
            }
        }else{
            while((ch = (char) br.read())  != ' '){
                valueString.append(ch);
            }
        }

        // currently pt in the front 1 step of space
        int key = Integer.parseInt(keyString.toString());
        int value = Integer.parseInt(valueString.toString());


        TreeNode newNode;
        if(type == '-'){
            newNode = new CPUTreeNode(key, value);
        }else{
            newNode = new DPUTreeNodeProxyAutoGen(key, value, Integer.parseInt(dpuIDString.toString()), Integer.parseInt(mramAddressString.toString()));

        }

        if(type != '-'){
            proxy ++;
        }
        TreeNode left;
        TreeNode right;

        //br.read();

        // in the latter of space
        left = deserialize(br);
        newNode.left = left;
        right = deserialize(br);
        newNode.right = right;

        return newNode;
    }
    public static void serialize(TreeNode root, BufferedWriter bw) throws IOException {
        if (root == null) {
            bw.write("#");
            return;
        }
        String data = "";
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
    public static TreeNode buildCPUTree(ArrayList<Pair<Integer, Integer>> pairs){
        if(pairs.size() == 0) return null;
        TreeNode root = new CPUTreeNode(pairs.get(0).key, pairs.get(0).val);
        for (int i = 1; i < pairs.size(); i++) {
            ((CPUTreeNode)root).insertNewCPUNode(pairs.get(i).key, pairs.get(i).val);
        }
        System.out.println("build cpu tree finished");
        return root;
    }
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
                    ((CPUTreeNode)root).insertNewCPUNode(k, v);

                }
                pairs.add(new BSTBuilder.Pair<>(k, v));
                s = br.readLine();
            }
            System.out.println("build cpu tree finished");
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }

    public static TreeNode buildLargePIMTree(String filePath, int cpuLayerCount){
        try {
            for(int i = 0; i < UPMEM.dpuInUse; i++){
                UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
            }
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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

    public static TreeNode buildLargePIMTree(ArrayList<Pair<Integer, Integer>> pairs){
        try {
            for(int i = 0; i < 1024; i++){
                UPMEM.getInstance().getDPUManager(i).createObject(DPUTreeNode.class, new Object[]{0, 0});
            }
        } catch (DpuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TreeNode root = BSTBuilder.buildCPUTree(pairs);
        convertCPUTreeToPIMTree(root, 18);

        return root;
    }


    public static TreeNode buildPIMTree(ArrayList<Pair<Integer, Integer>> pairs) {
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
