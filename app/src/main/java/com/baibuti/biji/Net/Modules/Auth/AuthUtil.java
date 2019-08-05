package com.baibuti.biji.Net.Modules.Auth;

import com.baibuti.biji.Net.Models.ReqBody.LoginReqBody;
import com.baibuti.biji.Net.Models.ReqBody.RegisterReqBody;
import com.baibuti.biji.Net.Models.RespBody.AuthResp;
import com.baibuti.biji.Net.Models.RespObj.AuthStatus;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;


public class AuthUtil {

    private static final String LoginUrl = Urls.AuthUrl + "/login";
    private static final String RegisterUrl = Urls.AuthUrl + "/register";

    public static AuthStatus login(String username, String password) {
        return login(username, password, 0);
    }

    public static AuthStatus login(String username, String password, int expiration) {
        LoginReqBody header = new LoginReqBody(username, password, expiration);

        RespType resp = NetUtil.httpPostSync(LoginUrl, header.toJson());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String token = resp.getHeaders().get("Authorization");
                AuthResp body = AuthResp.getAuthRespFromJson(resp.getBody());
                return new AuthStatus(token, body.getUsername());
            }
            else {
                String body = resp.getBody();
                MessageResp msg = MessageResp.getMsgRespFromJson(body);
                return new AuthStatus(code, parseErrorMsg(msg));
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return new AuthStatus(404, "未收到响应，请检查网络连接。");
        }
    }

    public static AuthStatus register(String username, String password) {
        RegisterReqBody header = new RegisterReqBody(username, password);

        RespType resp = NetUtil.httpPostSync(RegisterUrl, header.toJson());
        try {
            int code = resp.getCode();
            if (code == 200) {
                AuthResp body = AuthResp.getAuthRespFromJson(resp.getBody());
                return new AuthStatus(body.getUsername());
            }
            else {
                String body = resp.getBody();
                MessageResp msg = MessageResp.getMsgRespFromJson(body);
                return new AuthStatus(code, parseErrorMsg(msg));
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return new AuthStatus(404, "未收到响应，请检查网络连接。");
        }
    }

    private static String parseErrorMsg(MessageResp msg) {
        return msg.getMessage() + ": \n" + msg.getDetail();
    }
}
