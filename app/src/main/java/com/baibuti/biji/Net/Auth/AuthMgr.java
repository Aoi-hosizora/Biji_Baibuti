package com.baibuti.biji.Net.Auth;

public class AuthMgr {

    private static AuthMgr Instance;
    private AuthMgr() {}

    public static AuthMgr getInstance() {
        if (Instance == null) {
            Instance = new AuthMgr();
        }

        return Instance;
    }
}
