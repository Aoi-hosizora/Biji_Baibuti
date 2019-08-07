package com.baibuti.biji.Net.Modules.Note;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Net.Models.ReqBody.GroupReqBody;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.util.Locale;

public class GroupUtil {

    private static final String AllGroupUrl = Urls.GroupUrl + "/all";
    private static final String OneGroupUrl = Urls.GroupUrl + "/one?id=%d";
    private static final String UpdateGroupUrl = Urls.GroupUrl + "/update";
    private static final String InsertGroupUrl = Urls.GroupUrl + "/insert";
    private static final String DeleteGroupUrl = Urls.GroupUrl + "/delete";
    private static final String PushGroupUrl = Urls.GroupUrl + "/push";

    public static Group[] getAllGroups() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllGroupUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
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


    public static boolean pushGroups(Group[] groups) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                PushGroupUrl, NetUtil.POST,
                GroupReqBody.getJsonFromGroupReqRodies(GroupReqBody.toGroupReqBodies(groups)),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                return true;
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
}
