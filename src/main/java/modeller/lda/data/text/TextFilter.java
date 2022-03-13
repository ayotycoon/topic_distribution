package modeller.lda.data.text;

import java.util.List;

public interface TextFilter {
    List<String> filter(List<String> words);
}
