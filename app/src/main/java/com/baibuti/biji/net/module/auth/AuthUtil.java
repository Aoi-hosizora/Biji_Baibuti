package com.baibuti.biji.net.module.auth;

import com.baibuti.biji.net.model.reqBody.LoginReqBody;
import com.baibuti.biji.net.model.reqBody.RegisterReqBody;
import com.baibuti.biji.net.model.respBody.AuthResp;
import com.baibuti.biji.net.model.respObj.AuthStatus;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.NetHelper;
import com.baibuti.biji.net.Urls;


public class AuthUtil {

    private static final String LoginUrl = Urls.AuthUrl + "/login";
    private static final String RegisterUrl = Urls.AuthUrl + "/register";
    private static final String LogoutUrl = Urls.AuthUrl + "/logout";

    public static AuthStatus login(String username, String password) {
        return login(username, password, 0);
    }

    public static AuthStatus login(String username, String password, int expiration) {
        LoginReqBody header = new LoginReqBody(username, password, expiration);

        RespType resp = NetHelper.httpPostSync(LoginUrl, header.toJson());
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

    public static boolean logout() throws ServerErrorException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                LogoutUrl, NetHelper.POST,"{}",
                NetHelper.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                return true;
            }
            else if (code == 401) {
                return false;
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static AuthStatus register(String username, String password) {
        RegisterReqBody header = new RegisterReqBody(username, password);

        RespType resp = NetHelper.httpPostSync(RegisterUrl, header.toJson());
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
