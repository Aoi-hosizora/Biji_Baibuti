package com.baibuti.biji.net;

import okhttp3.Headers;

public class RespType {

    private String body; // ResponseBody
    private Headers headers;
    private int code;

    public RespType(){}

    public RespType(int code, Headers headers, String body) {
        this.body = body;
        this.headers = headers;
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
