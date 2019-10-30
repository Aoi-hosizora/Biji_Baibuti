package com.baibuti.biji.net.model.respBody;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthResp {

    private String username;
    private String status;

    public AuthResp(String username, String status) {
        this.username = username;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static AuthResp getAuthRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getAuthRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static AuthResp getAuthRespFromJson(JSONObject obj) {
        try {
            return new AuthResp(
                obj.getString("username"),
                obj.getString("status")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
