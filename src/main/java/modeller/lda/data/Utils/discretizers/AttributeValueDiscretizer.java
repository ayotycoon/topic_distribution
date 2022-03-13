package modeller.lda.data.Utils.discretizers;


import modeller.lda.data.frame.DataFrame;
import modeller.lda.data.frame.DataRow;

/**
 * Created by xschen on 18/8/15.
 */
public interface AttributeValueDiscretizer  {
    int discretize(double value, String index);
    DataRow transform(DataRow tuple);
    DataFrame fitAndTransform(DataFrame frame);
}
