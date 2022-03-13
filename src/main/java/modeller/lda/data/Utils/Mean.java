package modeller.lda.data.Utils;


import java.util.Arrays;

/**
 * Created by xschen on 14/8/15.
 */
public class Mean {
    public static double apply(double[] values){
        int length = values.length;
        if(length==0) return 0;
        double sum = Arrays.stream(values).sum();
        return sum / length;
    }
}
