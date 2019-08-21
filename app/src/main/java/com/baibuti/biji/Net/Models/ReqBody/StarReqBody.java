package com.baibuti.biji.Net.Models.ReqBody;

import android.util.Log;

import com.baibuti.biji.Data.Models.SearchItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class StarReqBody implements Serializable {

    private String title;
    private String url;
    private String content;

    public StarReqBody(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content.replaceAll("[\n|\r]", " ");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    // region SearchItem <-> StarReqBody

    /**
     * StarReqBody -> SearchItem
     * @return
     */
    public SearchItem toSearchItem() {
        // TODO
        return new SearchItem(title, content, url);
    }

    /**
     * SearchItem -> StarReqBody
     * @param star
     * @return
     */
    public static StarReqBody toStarReqBody(SearchItem star) {
        return new StarReqBody(star.getTitle(), star.getUrl(), star.getContent());
    }

    /**
     * StarReqBody[] -> SearchItem[]
     * @return
     */
    public static SearchItem[] toSearchItems(StarReqBody[] starReqBodies) {
        if (starReqBodies == null)
            return null;
        SearchItem[] rets = new SearchItem[starReqBodies.length];
        for (int i = 0; i < starReqBodies.length; i++)
            rets[i] = starReqBodies[i].toSearchItem();
        return rets;
    }

    /**
     * SearchItem[] -> StarReqBody[]
     * @return
     */
    public static StarReqBody[] toStarReqBodies(SearchItem[] searchItems) {
        if (searchItems == null)
            return null;
        StarReqBody[] rets = new StarReqBody[searchItems.length];
        for (int i = 0; i < searchItems.length; i++)
            rets[i] = toStarReqBody(searchItems[i]);
        return rets;
    }

    /**
     * StarReqBody[] -> Json str
     * @return
     */
    public static String toStarReqBodiesJson(StarReqBody[] starReqBodies) {
        if (starReqBodies == null)
            return "";
        JSONArray jsonArray = new JSONArray();
        for (StarReqBody starReqBody : starReqBodies) {
            jsonArray.put(starReqBody.toJson());
        }
        return jsonArray.toString();
    }
    
    // endregion SearchItem <-> StarReqBody

    // region StarReqBody <-> Json
    
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", title);
            obj.put("url", url);
            obj.put("content", content);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    /**
     * Json str -> StarReqBody
     * @param json
     * @return
     */
    public static StarReqBody getStarRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getStarRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> StarReqBody
     * @param obj
     * @return
     */
    public static StarReqBody getStarRespFromJson(JSONObject obj) {
        try {
            Log.e("", "getStarRespFromJson: " + obj.toString() );
            Log.e("", "getStarRespFromJson: " + obj.getString("url"));
            return new StarReqBody(
                    obj.getString("title"),
                    obj.getString("url"),
                    obj.getString("content")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json str -> StarReqBody[]
     * @param json
     * @return
     */
    public static StarReqBody[] getStarRespsFromJson(String json) {
        try {
            JSONArray objs = new JSONArray(json);
            return getStarRespsFromJson(objs);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> StarReqBody[]
     * @param objs
     * @return
     */
    public static StarReqBody[] getStarRespsFromJson(JSONArray objs) {
        try {
            StarReqBody[] ret = new StarReqBody[objs.length()];
            for (int i = 0; i < objs.length(); i++) {
                ret[i] = getStarRespFromJson(objs.getJSONObject(i));
            }
            return ret;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    // endregion StarReqBody <-> Json
}
