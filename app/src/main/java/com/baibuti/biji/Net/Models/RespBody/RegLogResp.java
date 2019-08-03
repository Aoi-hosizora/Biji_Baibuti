package com.baibuti.biji.Net.Models.RespBody;

import org.json.JSONException;
import org.json.JSONObject;

public class RegLogResp {

    private String username;
    private String status;

    public RegLogResp(String username, String status) {
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

    public static RegLogResp getRegLogRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getRegLogRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static RegLogResp getRegLogRespFromJson(JSONObject obj) {
        try {
            return new RegLogResp(
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
