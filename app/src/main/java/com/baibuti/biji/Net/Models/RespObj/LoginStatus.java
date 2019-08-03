package com.baibuti.biji.Net.Models.RespObj;

public class LoginStatus {

    private String token;
    private String username;
    private boolean isSuccess;
    private String errorMsg;
    private int errorCode;

    private LoginStatus(boolean isSuccess, String token, String username, int errorCode, String errorMsg) {
        this.token = token;
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }

    public LoginStatus(String token, String username) {
        this(true, token, username, 200, "");
    }

    public LoginStatus(int errorCode, String errorMsg) {
        this(false, "", "", errorCode, errorMsg);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
