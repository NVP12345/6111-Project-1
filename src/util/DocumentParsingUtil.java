package util;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class DocumentParsingUtil {

    public static Map<String, Integer> getWordFrequenciesForContent(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, " \t\n\r\f,.:;?![]'()");
        Map<String, Integer> words = new HashMap<String, Integer>();
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            if ( ! words.containsKey(word) ) {
                words.put(word, 1);
            } else {
                words.put(word, words.get(word) + 1);
            }
        }
        return words;
    }

}
