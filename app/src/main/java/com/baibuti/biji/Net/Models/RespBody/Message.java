package com.baibuti.biji.Net.Models.RespBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Message implements Serializable {

    private String message;
    private String detail;

    public Message(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    public Message(String message) {
        this(message, "");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static Message getMessageFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getMessageFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Message getMessageFromJson(JSONObject obj) {
        try {
            return new Message(
                obj.getString("message"),
                obj.getString("detail")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
