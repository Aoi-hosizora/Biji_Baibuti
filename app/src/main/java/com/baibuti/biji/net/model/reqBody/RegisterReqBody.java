package com.baibuti.biji.net.model.reqBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class RegisterReqBody implements Serializable {

    private String username;
    private String password;

    public RegisterReqBody(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", username);
            obj.put("password", password);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }
}
