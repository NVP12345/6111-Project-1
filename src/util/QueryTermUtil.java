package util;

import java.util.List;

public class QueryTermUtil {

    public static String buildQueryStringFromTerms(List<String> queryTerms) {
        String queryString = "";
        for (String term : queryTerms) {
            queryString += term + " ";
        }
        return queryString.trim();
    }

}
