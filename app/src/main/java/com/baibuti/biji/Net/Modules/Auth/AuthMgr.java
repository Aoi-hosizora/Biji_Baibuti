package com.baibuti.biji.Net.Modules.Auth;

import java.util.ArrayList;

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

    public interface OnLoginChangeListener {
        void onLogin(String UserName);
        void onLogout();
    }

    private ArrayList<OnLoginChangeListener> onLoginChangeListeners = new ArrayList<>();

    public void addLoginChangeListener(OnLoginChangeListener onLoginChangeListener) {
        this.onLoginChangeListeners.add(onLoginChangeListener);
    }

    public void logout() {
        this.token = "";
        this.userName = "";

        if (onLoginChangeListeners != null)
            for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                onLoginChangeListener.onLogout();
    }

    public void login(String username, String token) {
        this.token = token;
        this.userName = username;

        if (onLoginChangeListeners != null)
            for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                onLoginChangeListener.onLogin(userName);
    }

    public boolean isLogin() {
        return token != null && !(token.isEmpty()) &&
                userName != null && !(userName.isEmpty());
    }
}
