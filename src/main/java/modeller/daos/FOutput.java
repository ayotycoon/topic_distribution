package modeller.daos;



import modeller.lda.Doc;
import modeller.lda.data.Utils.TupleTwo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FOutput {
    private String topicSummary;
    private List<TupleTwo<Integer, Double>> lda;
    private Hashtable<String,List<HashMap<String, Integer>>> topKeyWords;

    private double[] theta = new double[1];

}
