package com.baibuti.biji.service.auth;

import com.baibuti.biji.service.retrofit.RetrofitFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class AuthManager {

    private AuthManager() {}
    private static AuthManager Instance;
    public static AuthManager getInstance() {
        if (Instance == null)
            Instance = new AuthManager();
        return Instance;
    }

    @Getter private String username;
    @Getter private String token;

    public boolean isLogin() {
        return username != null && !username.isEmpty() &&
            token != null && !token.isEmpty();
    }

    public Map<String, String> getAuthorizationHead(String... otherHead) {
        if (isLogin()) {
            List<String> vararg = Arrays.asList(otherHead);
            vararg.add("Authorization");
            vararg.add(getToken());
            otherHead = vararg.toArray(new String[0]);
        }
        return RetrofitFactory.getHeader(otherHead);
    }

    /**
     * !! 注销，并通知订阅器
     */
    public void logout() {
        this.token = "";
        this.username = "";

        if (onLoginChangeListeners != null)
            for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                onLoginChangeListener.onLogout();
    }

    /**
     * !! 登录，并通知订阅器
     */
    public void login(String username, String token) {
        this.token = token;
        this.username = username;

        if (onLoginChangeListeners != null)
            for (OnLoginChangeListener onLoginChangeListener : onLoginChangeListeners)
                onLoginChangeListener.onLogin(username);
    }

    public interface OnLoginChangeListener {
        void onLogin(String username);
        void onLogout();
    }

    private List<OnLoginChangeListener> onLoginChangeListeners = new ArrayList<>();

    public void addLoginChangeListener(OnLoginChangeListener onLoginChangeListener) {
        this.onLoginChangeListeners.add(onLoginChangeListener);
    }
}
