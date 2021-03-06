package com.baibuti.biji.common.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.baibuti.biji.common.retrofit.RetrofitFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import static android.content.Context.MODE_PRIVATE;

public class AuthManager {

    private AuthManager() {}
    private static AuthManager Instance;

    public static AuthManager getInstance() {
        if (Instance == null)
            Instance = new AuthManager();
        return Instance;
    }

    @Getter
    private String username;

    @Getter @Setter
    private String token; // 不加 Bearer

    public boolean isLogin() {
        return username != null && !username.isEmpty() &&
            token != null && !token.isEmpty();
    }

    public Map<String, String> getAuthorizationHead(String... otherHead) {
        // java.lang.UnsupportedOperationException
        List<String> vararg = new ArrayList<>();
        Collections.addAll(vararg, otherHead);
        vararg.add("Authorization");
        vararg.add("Bearer " + token);
        otherHead = vararg.toArray(new String[0]);
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
        if (isLogin())
            onLoginChangeListener.onLogin(username);
        else
            onLoginChangeListener.onLogout();
    }

    private static final String AuthSP = "biji_auth";
    private static final String TokenKey = "login_token";


    /**
     * 从 SP 中获取 Token
     */
    public String getSpToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(AuthSP, MODE_PRIVATE);
        return sp.getString(TokenKey, "");
    }

    /**
     * 设置 SP Token
     */
    public void setSpToken(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences(AuthSP, MODE_PRIVATE);
        SharedPreferences.Editor edt = sp.edit();
        edt.putString(TokenKey, token);
        edt.apply();
    }
}
