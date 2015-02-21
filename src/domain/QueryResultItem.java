package domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueryResultItem {

    private final String id;
    private final String title;
    private final String description;
    private final String displayUrl;
    private final String url;
    private Boolean relevant;

    private QueryResultItem(JSONObject queryResultJson) throws JSONException {
        this.id = queryResultJson.getString("ID");
        this.title = queryResultJson.getString("Title");
        this.description = queryResultJson.getString("Description");
        this.displayUrl = queryResultJson.getString("DisplayUrl");
        this.url = queryResultJson.getString("Url");
    }

    public static List<QueryResultItem> buildListFromApiResultJsonString(String resultJsonString) {
        List<QueryResultItem> queryResultItems = new ArrayList<QueryResultItem>();
        try {
            JSONObject resultJsonWrapper = new JSONObject(resultJsonString);
            JSONArray resultJsonArray = resultJsonWrapper.getJSONObject("d").getJSONArray("results");
            for (int i = 0; i < resultJsonArray.length(); i++) {
                queryResultItems.add(new QueryResultItem(resultJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return queryResultItems;
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

    public void setRelevant(Boolean relevant) {
        this.relevant = relevant;
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
