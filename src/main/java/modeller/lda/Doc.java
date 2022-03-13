package modeller.lda;


import modeller.lda.data.Utils.TupleTwo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Created by xschen on 11/3/15.
 */
@Getter
@Setter
public class Doc<B> {
    private int docIndex;
    private List<Token> tokens = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final int[] topicCounts;
    private B blogPost;
    private String content;
    private long timestamp;

    public Doc(int topicCount) {
        topicCounts = new int[topicCount];
    }

    public void addToken(int wordIndex, int topicIndex) {
        topicCounts[topicIndex]++;
        tokens.add(new Token(wordIndex, topicIndex));
    }


    public void decTopicCount(int topicIndex) {
        topicCounts[topicIndex]--;
    }


    public double topicCounts(int topicIndex) {
        return topicCounts[topicIndex];
    }


    public void incTopicCount(int topicIndex) {
        topicCounts[topicIndex]++;
    }

    public List<TupleTwo<Integer, Double>> topTopics(int limits) {
        double sum = Arrays.stream(topicCounts).asDoubleStream().sum();

        List<TupleTwo<Integer, Double>> ranked = new ArrayList<>();
        for (int topicIndex = 0; topicIndex < topicCounts.length; ++topicIndex) {
            ranked.add(new TupleTwo<>(topicIndex, topicCounts[topicIndex] / sum));
        }
        ranked.sort((a, b) -> -Double.compare(a._2(), b._2()));

        return IntStream.range(0, limits).mapToObj(ranked::get).collect(Collectors.toList());
    }
}
