package com.baibuti.biji.Net.Auth;

import com.baibuti.biji.Net.Models.ReqHeader.LoginHeader;
import com.baibuti.biji.Net.Models.RespBody.RegLogResp;
import com.baibuti.biji.Net.Models.RespObj.LoginStatus;
import com.baibuti.biji.Net.Models.RespBody.Message;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;


public class AuthUtil {

    private static final String LoginUrl = Urls.AuthUrl + "/login";
    private static final String RegisterUrl = Urls.AuthUrl + "/register";

    public static LoginStatus login(String username, String password) {
        return login(username, password, 0);
    }

    public static LoginStatus login(String username, String password, int expiration) {
        LoginHeader header = new LoginHeader(username, password, expiration);

        RespType resp = NetUtil.httpPostSync(LoginUrl, header.toJson());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String token = resp.getHeaders().get("Authorization");
                RegLogResp body = RegLogResp.getRegLogRespFromJson(resp.getBody());
                return new LoginStatus(token, body.getUsername());
            }
            else {
                String body = resp.getBody();
                Message msg = Message.getMessageFromJson(body);
                return new LoginStatus(code, parseErrorMsg(msg));
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String parseErrorMsg(Message msg) {
        return "Error";
    }
}
