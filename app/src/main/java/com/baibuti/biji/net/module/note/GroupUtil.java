package com.baibuti.biji.net.module.note;

import android.support.annotation.NonNull;

import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.reqBody.GroupReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.NetUtil;
import com.baibuti.biji.net.Urls;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class GroupUtil {

    private static final String AllGroupUrl = Urls.GroupUrl + "/all";
    private static final String OneGroupUrl = Urls.GroupUrl + "/one?id=%d";
    private static final String UpdateGroupUrl = Urls.GroupUrl + "/update";
    private static final String InsertGroupUrl = Urls.GroupUrl + "/insert";
    private static final String DeleteGroupUrl = Urls.GroupUrl + "/delete";

    public static Group[] getAllGroups() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllGroupUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                GroupReqBody[] rets = GroupReqBody.getGroupRespsFromJson(resp.getBody());
                return GroupReqBody.toGroups(rets);
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Group getOneGroup(int id) throws ServerErrorException {
        String url = String.format(Locale.CHINA, OneGroupUrl, id);
        RespType resp = NetUtil.httpGetSync(url, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                GroupReqBody ret = GroupReqBody.getGroupRespFromJson(resp.getBody());
                return ret.toGroup();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Group updateGroup(Group group) throws ServerErrorException {
        RespType resp = NetUtil.httpPostSync(
            UpdateGroupUrl,
            GroupReqBody.toGroupReqBody(group).toJson(),
            NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                GroupReqBody ret = GroupReqBody.getGroupRespFromJson(resp.getBody());
                return ret.toGroup();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Group insertGroup(Group group) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                InsertGroupUrl, NetUtil.PUT,
                GroupReqBody.toGroupReqBody(group).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                GroupReqBody ret = GroupReqBody.getGroupRespFromJson(resp.getBody());
                return ret.toGroup();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Group deleteGroup(Group group) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteGroupUrl, NetUtil.DELETE,
                GroupReqBody.toGroupReqBody(group).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                GroupReqBody ret = GroupReqBody.getGroupRespFromJson(resp.getBody());
                return ret.toGroup();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
