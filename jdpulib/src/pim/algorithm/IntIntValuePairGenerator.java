package pim.algorithm;

import java.util.Random;

public class IntIntValuePairGenerator extends BSTBuilder.BaseKeyValuePairGenerator<Integer, Integer> {
    static Random random = new Random();
    int rangeUpper;

    public IntIntValuePairGenerator(int rangeUpper) {
        this.rangeUpper = rangeUpper;
    }

    @Override
    protected BSTBuilder.Pair<Integer, Integer> genPair() {
        return new BSTBuilder.Pair<>(random.nextInt(rangeUpper), random.nextInt(rangeUpper));
    }
}
