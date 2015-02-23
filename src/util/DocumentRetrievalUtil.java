package util;

import org.jsoup.Jsoup;

public class DocumentRetrievalUtil {

    public static String getDocumentContentsIfPossible(String url) {
        try {
            return Jsoup.connect(url).get().body().text();
        } catch (Exception e) {
            return null;
        }
    }

}
