package com.baibuti.biji.net.model.reqBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class LoginReqBody implements Serializable {

    private String username;
    private String password;
    private int expiration;

    public LoginReqBody(String username, String password, int expiration) {
        this.username = username;
        this.password = password;
        this.expiration = expiration;
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

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", username);
            obj.put("password", password);
            if (expiration != 0)
                obj.put("expiration", expiration);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }
}
