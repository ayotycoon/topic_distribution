package modeller.lda.data.text;

import java.util.List;

public interface Tokenizer {
    List<String> tokenize(String text);
}
