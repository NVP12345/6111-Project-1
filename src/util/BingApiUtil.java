package util;

import domain.QueryResultItem;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class BingApiUtil {

    private static final String BING_URL = "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&$format=json";
    private static final String BING_ACCOUNT_KEY = "gBjyBpDpbFVENUIq/YsYR813f7PuEIkpcqAsqVq45eY";
    private static final URLCodec URL_CODEC = new URLCodec();

    public static List<QueryResultItem> getBingQueryResults(List<String> queryTerms) {
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
        return QueryResultItem.buildListFromApiResultJsonString(content);
    }

}
