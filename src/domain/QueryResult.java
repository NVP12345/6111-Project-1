package domain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {

    private final String id;
    private final String title;
    private final String description;
    private final String displayUrl;
    private final String url;

    private QueryResult(JSONObject queryResultJson) throws JSONException {
        this.id = queryResultJson.getString("ID");
        this.title = queryResultJson.getString("Title");
        this.description = queryResultJson.getString("Description");
        this.displayUrl = queryResultJson.getString("DisplayUrl");
        this.url = queryResultJson.getString("Url");
    }

    public static List<QueryResult> buildListFromApiResultJsonString(String resultJsonString) {
        List<QueryResult> queryResults = new ArrayList<QueryResult>();
        try {
            JSONObject resultJsonWrapper = new JSONObject(resultJsonString);
            JSONArray resultJsonArray = resultJsonWrapper.getJSONObject("d").getJSONArray("results");
            for (int i = 0; i < resultJsonArray.length(); i++) {
                queryResults.add(new QueryResult(resultJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return queryResults;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s\n" +
                "Title: %s\n " +
                "Description: %s\n " +
                "DisplayUrl: %s\n " +
                "Url: %s ",
                id, title, description, displayUrl, url
        );
    }
}
