package modeller.lda;


import modeller.lda.data.Utils.TupleTwo;
import modeller.lda.data.text.VocabularyTableCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


/**
 * Created by xschen on 11/4/15.
 */
public class LdaResult {
    private LdaModel model;
    private List<Doc> documents;
    private long docCount;

    public LdaResult(LdaModel model, List<Doc> documents){
        this.model = model;
        this.documents = documents;
        this.docCount = documents.size();
    }

    public List<Doc> documents(){
        return documents;
    }

    public long docCount() { return docCount; }

    public List<TupleTwo<Doc, Double>> scoreDocuments(int selectedTopicIndex){
        if (selectedTopicIndex == -1) {
            return null;
        }

        double docSortSmoothing_local = model.getDocSortSmoothing();
        double sumDocSortSmoothing_local = model.sumDocSortSmoothing();

        List<TupleTwo<Doc, Double>> scores = new ArrayList<>();

        documents.forEach(doc -> {
            double score = (doc.topicCounts(selectedTopicIndex) + docSortSmoothing_local) / (doc.getTokens().size() + sumDocSortSmoothing_local);
            scores.add(new TupleTwo<>(doc, score));
        });

        return scores;
    }

    public String topicSummary(int selectedTopicIndex){
        return model.getTopicSummary(selectedTopicIndex);
    }

    public List<HashMap<String, Integer>> topKeyWords(int selectedTopicIndex, int limits) {
        return model.topicWords(selectedTopicIndex, limits);
    }
    public Hashtable<String,List<HashMap<String, Integer>>> topKeyWords(int limits){
        int topicCount = model.getTopicCount();
        Hashtable<String,List<HashMap<String, Integer>>> topicKeyWordTable = new Hashtable<>();
        for (int i = 0; i < topicCount; i++) {
            List<HashMap<String, Integer>> topwords =model.topicWords(i, limits);
            String topicNumber = String.format("Topic %s",i);
            topicKeyWordTable.putIfAbsent(topicNumber,topwords);
        }
        return topicKeyWordTable;
    }

    public int topicCount(){
        return model.getTopicCount();
    }

    public List<TupleTwo<Doc, Double>> topDocuments(int selectedTopicIndex, int limits) {

        if(selectedTopicIndex==-1) return null;

        List<TupleTwo<Doc, Double>> ranked = scoreDocuments(selectedTopicIndex);
        ranked.sort((s1, s2)-> - Double.compare(s1._2(), s2._2())); //sort descendingly
        int count = Math.min(limits, ranked.size());

        List<TupleTwo<Doc, Double>> result = new ArrayList<>();
        for(int i=0; i < count; ++i){
            result.add(ranked.get(i));
        }
        return result;
    }

    public List<VocabularyTableCell> vocabularyTable(int size){
        return model.vocabularyTable(size);
    }

    public double[][] getTopicCorrelations() {
        int topicCount = model.getTopicCount();

        double[][] correlationMatrix = new double[topicCount][];
        for(int i=0; i < topicCount; ++i){
            correlationMatrix[i] = new double[topicCount];
        }
        double[] topicProbabilities = new double[topicCount];

        double correlationMinTokens_local = model.getCorrelationMinTokens();
        double correlationMinProportion_local = model.getCorrelationMinProportion();

        // iterate once to get mean log topic proportions
        documents.forEach(doc -> {

            // We want to find the subset of topics that occur with non-trivial concentration in this document.
            // Only consider topics with at least the minimum number of tokens that are at least 5% of the doc.
            List<Integer> documentTopics = new ArrayList<>();
            double tokenCutoff = Math.max(correlationMinTokens_local, correlationMinProportion_local * doc.getTokens().size());

            for (int topicIndex = 0; topicIndex < topicCount; topicIndex++) {
                if (doc.topicCounts(topicIndex) >= tokenCutoff) {
                    documentTopics.add(topicIndex);
                    topicProbabilities[topicIndex]++; // Count the number of docs with this topic
                }
            }

            // Look at all pairs of topics that occur in the document.
            for (int i = 0; i < documentTopics.size() - 1; i++) {
                for (int j = i + 1; j < documentTopics.size(); j++) {
                    correlationMatrix[documentTopics.get(i)][documentTopics.get(j)]++;
                    correlationMatrix[documentTopics.get(j)][documentTopics.get(i)]++;
                }
            }
        });



        for (int t1 = 0; t1 < topicCount - 1; t1++) {
            for (int t2 = t1 + 1; t2 < topicCount; t2++) {
                double value = (docCount * correlationMatrix[t1][t2]) /
                        (topicProbabilities[t1] * topicProbabilities[t2]);
                correlationMatrix[t1][t2] = value == 0 ? 0 : Math.log(value);
                correlationMatrix[t2][t1] = value == 0 ? 0 : Math.log(value);
            }
        }

        return correlationMatrix;
    }
    //  Use this when you are only running it against a Single blogpost
    public Doc getSingleDocument(){
        return documents().get(0);
    }

}
