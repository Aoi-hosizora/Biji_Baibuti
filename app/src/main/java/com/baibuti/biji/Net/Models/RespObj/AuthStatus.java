package com.baibuti.biji.Net.Models.RespObj;

public class AuthStatus {

    private String token;
    private String username;
    private boolean isSuccess;
    private String errorMsg;
    private int errorCode;

    private AuthStatus(boolean isSuccess, String token, String username, int errorCode, String errorMsg) {
        this.token = token;
        this.username = username;
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }

    /**
     * 登陆成功
     * @param token
     * @param username
     */
    public AuthStatus(String token, String username) {
        this(true, token, username, 200, "");
    }

    /**
     * 登陆失败 / 注册失败
     * @param errorCode
     * @param errorMsg
     */
    public AuthStatus(int errorCode, String errorMsg) {
        this(false, "", "", errorCode, errorMsg);
    }

    /**
     * 注册成功
     * @param username
     */
    public AuthStatus(String username) {
        this(true, "", username, 200, "");
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
