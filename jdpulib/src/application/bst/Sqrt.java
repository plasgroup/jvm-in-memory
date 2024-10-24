package application.bst;

public class Sqrt {

    public static float compute(float number) {

        float guess = number / (float) 2.0;
        float epsilon = (float) 1e-3; // 許容誤差

        while (absolute(guess * guess - number) > epsilon) {
            guess = (guess + number / guess) / (float) 2.0;
        }

        return guess;
    }

    private static float absolute(float value) {
        return value < (float) 0.0 ? -value : value;
    }
}
