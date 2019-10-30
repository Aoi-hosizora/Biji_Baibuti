package com.baibuti.biji.net.model.reqBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DelImgReqBody {

    private String username;
    private String filename;

    public DelImgReqBody(String username, String filename) {
        this.username = username;
        this.filename = filename;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * DelImgReqBody -> Json Str
     * @return
     */
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("filename", filename);
            return jsonObject.toString();
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * DelImgReqBody[] -> Json Str
     * @return
     */
    public static String toJsons(DelImgReqBody[] delImgReqBodies) {
        JSONArray jsonArray = new JSONArray();
        for (DelImgReqBody delImgReqBody : delImgReqBodies) {
            jsonArray.put(delImgReqBody.toJson());
        }
        return jsonArray.toString();
    }


    /**
     * url String -> DelImgReqBody
     * @param url
     * @return
     */
    public static DelImgReqBody toReqBodyFromUrls(String url) {
        String[] us = url.split("/");
        if (us.length <= 1)
            return null;
        return new DelImgReqBody(us[us.length - 2], us[us.length - 1]);
    }

    /**
     * url String[] -> DelImgReqBody[]
     * @param urls
     * @return
     */
    public static DelImgReqBody[] toReqBodiesFromUrls(String[] urls) {
        DelImgReqBody[] rets = new DelImgReqBody[urls.length];
        for (int i = 0; i < urls.length; i++) {
            DelImgReqBody ret = toReqBodyFromUrls(urls[i]);
            if (ret == null) continue;
            rets[i] = ret;
        }
        return rets;
    }
}
