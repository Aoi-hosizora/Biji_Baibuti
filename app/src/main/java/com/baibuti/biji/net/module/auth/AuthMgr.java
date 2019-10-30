package com.baibuti.biji.net.module.auth;

import android.util.Log;

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
        Log.e("", "setToken: " + token );
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

        try {
            if (onLoginChangeListeners != null)
                for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                    onLoginChangeListener.onLogout();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void login(String username, String token) {
        this.token = token;
        this.userName = username;

        try {
            if (onLoginChangeListeners != null)
                for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                    onLoginChangeListener.onLogin(userName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isLogin() {
        return token != null && !(token.isEmpty()) &&
                userName != null && !(userName.isEmpty());
    }
}
