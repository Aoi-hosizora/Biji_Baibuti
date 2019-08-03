package com.baibuti.biji.Net.Auth;

public class AuthMgr {

    private static AuthMgr Instance;
    private AuthMgr() {}

    public static AuthMgr getInstance() {
        if (Instance == null) {
            Instance = new AuthMgr();
            Instance.userName = "";
            Instance.token = "";
        }

        return Instance;
    }

    private String userName;
    private String token;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
