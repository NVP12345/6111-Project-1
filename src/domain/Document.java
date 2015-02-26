package domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.DocumentParsingUtil;
import util.DocumentRetrievalUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Document {

    private final String id;
    private final String title;
    private final String description;
    private final String displayUrl;
    private final String url;
    private final String content;
    private final boolean valid;
    private Boolean relevant;

    private final Map<String, Integer> titleTermFrequencies;
    private final Map<String, Integer> contentTermFrequencies;
    private final Set<String> allWords;

    private Document(JSONObject queryResultJson) throws JSONException {
        id = queryResultJson.getString("ID");
        title = queryResultJson.getString("Title");
        description = queryResultJson.getString("Description");
        displayUrl = queryResultJson.getString("DisplayUrl");
        url = queryResultJson.getString("Url");

        //content = DocumentRetrievalUtil.getDocumentContentsIfPossible(url);
        content = description;

        valid = content != null;

        if (valid) {
            titleTermFrequencies = DocumentParsingUtil.getWordFrequenciesForContent(title);
            contentTermFrequencies = DocumentParsingUtil.getWordFrequenciesForContent(content);

            allWords = new HashSet<String>(titleTermFrequencies.keySet());
            allWords.addAll(contentTermFrequencies.keySet());
        } else {
            titleTermFrequencies = null;
            contentTermFrequencies = null;
            allWords = null;
        }
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

    public String getContent() {
        return content;
    }

    public Boolean getRelevant() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public boolean isValid() {
        return valid;
    }

    public Map<String, Integer> getTitleTermFrequencies() {
        return titleTermFrequencies;
    }

    public Map<String, Integer> getContentTermFrequencies() {
        return contentTermFrequencies;
    }

    public Set<String> getAllWords() {
        return allWords;
    }

    @Override
    public String toString() {
        String output = String.format(
                "[\n" +
                " URL: %s\n" +
                " Title: %s\n" +
                " Summary: %s\n" +
                "]\n",
                url, title, description
        );

        if (relevant != null) {
            output += "\nRELEVANT: " + (relevant ? "YES" : "NO");
        }

        return output;
    }
}
