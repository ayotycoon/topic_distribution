package modeller.lda.data.Utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by xschen on 1/5/2017.
 */
public class CollectionUtils {
    public static <T> List<T> clone(List<T> that, Function<T, T> transformer) {
        List<T> result = new ArrayList<>();
        for(int i=0; i < that.size(); ++i){
            result.add(transformer.apply(that.get(i)));
        }
        return result;
    }


    public static <T> List<T> toList(T[] that, Function<T, T> transformer) {
        List<T> result = Arrays.stream(that).map(transformer::apply).collect(Collectors.toList());
        return result;
    }

    public static List<Double> toList(double[] that) {
        List<Double> result = Arrays.stream(that).boxed().collect(Collectors.toList());
        return result;
    }

    public static <T> void exchange(List<T> a, int i, int j) {
        T temp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, temp);
    }


    public static double[] toDoubleArray(List<Double> list) {
        double[] result = new double[list.size()];
        for(int i=0; i < list.size(); ++i) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static String[] toArray(List<String> list) {
        String[] result = new String[list.size()];
        for(int i=0; i < list.size(); ++i) {
            result[i] = list.get(i);
        }
        return result;
    }
}
