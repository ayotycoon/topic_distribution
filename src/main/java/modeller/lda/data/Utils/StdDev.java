package modeller.lda.data.Utils;

public class StdDev {
    public static double apply(double[] values, double mu){
        return Math.sqrt(Variance.apply(values, mu));
    }
}
