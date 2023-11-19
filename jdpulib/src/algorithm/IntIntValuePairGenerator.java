package algorithm;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class IntIntValuePairGenerator extends BSTBuilder.BaseKeyValuePairGenerator<Integer, Integer> {
    static Random random = new Random();
    int rangeLower;
    int rangeUpper;

    public IntIntValuePairGenerator(int rangeLower, int rangeUpper) {
        this.rangeLower = rangeLower;
        this.rangeUpper = rangeUpper;
    }

    public static ArrayList<BSTBuilder.Pair<Integer, Integer>> fromFile(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            ArrayList<BSTBuilder.Pair<Integer, Integer>> pairs = new ArrayList<BSTBuilder.Pair<Integer, Integer>>();
            String s = br.readLine();
            while(s != null){

                int k = Integer.parseInt(s.split(" ")[0]);
                int v = Integer.parseInt(s.split(" ")[1]);
                pairs.add(new BSTBuilder.Pair<>(k, v));
                s = br.readLine();
            }
            br.close();
            return pairs;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected BSTBuilder.Pair<Integer, Integer> generatePair() {
        int keyRandom = random.ints(rangeLower, rangeUpper).findFirst().getAsInt();
        int valRandom = random.ints(rangeLower, rangeUpper).findFirst().getAsInt();

        return new BSTBuilder.Pair<>(keyRandom, valRandom);
    }
}
