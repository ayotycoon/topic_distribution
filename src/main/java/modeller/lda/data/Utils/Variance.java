package modeller.lda.data.Utils;

public class Variance {
    public static double apply(double[] values, double mu) {
        int length = values.length;
        if(length <= 1) return 0;

        double num1;
        double sum = 0;
        for(int i=0; i < length; ++i){
            num1 = (values[i] - mu);
            sum += num1 * num1;
        }

        return sum / (length - 1);
    }
}
