package util;

import conf.WeightConstants;
import domain.Document;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BingApiUtil {

    public static final int NUMBER_OF_RESULTS = 10;
    private static final String BING_URL = String.format("https://api.datamarket.azure.com/Bing/Search/Web?$top=%d&$format=json", NUMBER_OF_RESULTS);
    public static String BING_ACCOUNT_KEY; // = "gBjyBpDpbFVENUIq/YsYR813f7PuEIkpcqAsqVq45eY";
    private static final URLCodec URL_CODEC = new URLCodec();

    public static List<Document> getBingQueryResults(List<String> queryTerms, Map<String, Document> allDocumentsById) {
        byte[] accountKeyBytes = Base64.encodeBase64((BING_ACCOUNT_KEY + ":" + BING_ACCOUNT_KEY).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);

        URL url = null;
        try {
            url = new URL(BING_URL + "&Query=%27" + URL_CODEC.encode(QueryTermUtil.buildQueryStringFromTerms(queryTerms)) + "%27");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }

        System.out.println("URL: " + url);

        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

        InputStream inputStream = null;
        try {
            inputStream = (InputStream) urlConnection.getContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] rawContent = new byte[urlConnection.getContentLength()];
        try {
            inputStream.read(rawContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String content = new String(rawContent);
        List<Document> newDocuments = Document.buildListFromApiResultJsonString(content);

        System.out.println("Total no of results: " + newDocuments.size());

        List<Document> currentResultItems = new ArrayList<Document>();
        for (Document newDocument : newDocuments) {
            String queryResultItemId = newDocument.getId();
            if (allDocumentsById.containsKey(queryResultItemId)) {
                currentResultItems.add(allDocumentsById.get(queryResultItemId));
            } else {
                currentResultItems.add(newDocument);
                allDocumentsById.put(queryResultItemId, newDocument);
            }
        }
        return currentResultItems;
    }

}
