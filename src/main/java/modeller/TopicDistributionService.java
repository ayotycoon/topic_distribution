package modeller;

import modeller.daos.FInput;
import modeller.daos.FOutput;
import modeller.lda.data.Utils.TupleTwo;
import modeller.lda.Lda;
import modeller.lda.LdaResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicDistributionService {
    // order 1
    public FOutput run(FInput blogPost) {
        Lda<FInput> method = new Lda();
        method.setTopicCount(10);
        method.setMaxVocabularySize(20000);
        method.setStemmerEnabled(true);
        // method.setRemoveNumbers(true);
        method.setRemoveXmlTag(true);
        // method.addStopWords(Arrays.asList("we", "they"));
        // LdaResult _result = method.fit(blogpostsArray);
        // data.topicCorrelations = _result.getTopicCorrelations();
        List<FInput> singleBlogAsArray = List.of(blogPost);
        LdaResult result1 = method.fit(singleBlogAsArray);
        // var s=  String.format("Doc: {}", result1.documents().get(0).getContent());
        // result1.topicSummary()
        List<TupleTwo<Integer, Double>> topTopics = result1.documents().get(0).topTopics(10);
        FOutput ldaTopicDistribution = new FOutput();
        ldaTopicDistribution.setLda(topTopics);
        FInput blog = (FInput) result1.documents().get(0).getBlogPost();
        ldaTopicDistribution.setTopKeyWords(result1.topKeyWords(10));

        ldaTopicDistribution.setTheta(topTopics.stream().sorted(Comparator.comparingInt(TupleTwo::getV1)).map(a -> a.getV2()).mapToDouble((a)-> a).toArray());
        return ldaTopicDistribution;
    }
}


