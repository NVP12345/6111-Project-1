package domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.DocumentParsingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Document {

    private final String id;
    private final String title;
    private final String description;
    private final String displayUrl;
    private final String url;
    private Boolean relevant;

//    private final Map<String, Integer> titleTermFrequencies;
//    private final Map<String, Integer> contentTermFrequencies;
    private final Map<String, Integer> termFrequencies;

    private Document(JSONObject queryResultJson) throws JSONException {
        id = queryResultJson.getString("ID");
        title = queryResultJson.getString("Title");
        description = queryResultJson.getString("Description");
        displayUrl = queryResultJson.getString("DisplayUrl");
        url = queryResultJson.getString("Url");

//        titleTermFrequencies = DocumentParsingUtil.getWordFrequenciesForContent(title);
//        contentTermFrequencies = DocumentParsingUtil.getWordFrequenciesForContent(description);
        termFrequencies = DocumentParsingUtil.getWordFrequenciesForContent(title + " " + description);
    }

    public static List<Document> buildListFromApiResultJsonString(String resultJsonString) {
        List<Document> documents = new ArrayList<Document>();
        try {
            JSONObject resultJsonWrapper = new JSONObject(resultJsonString);
            JSONArray resultJsonArray = resultJsonWrapper.getJSONObject("d").getJSONArray("results");
            for (int i = 0; i < resultJsonArray.length(); i++) {
                documents.add(new Document(resultJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return documents;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getRelevant() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public Map<String, Integer> getTermFrequencies() {
        return termFrequencies;
    }

    @Override
    public String toString() {
        String output = String.format(
                "ID: %s\n" +
                "Title: %s\n " +
                "Description: %s\n " +
                "DisplayUrl: %s\n " +
                "Url: %s ",
                id, title, description, displayUrl, url
        );

        if (relevant != null) {
            output += "\nRELEVANT: " + (relevant ? "YES" : "NO");
        }

        return output;
    }
}
