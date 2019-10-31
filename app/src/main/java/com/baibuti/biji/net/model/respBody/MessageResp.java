package com.baibuti.biji.net.model.respBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@Deprecated
public class MessageResp implements Serializable {

    private String message;
    private String detail;

    public MessageResp(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    public MessageResp(String message) {
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

    public static MessageResp getMsgRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getMsgRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static MessageResp getMsgRespFromJson(JSONObject obj) {
        try {
            return new MessageResp(
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
