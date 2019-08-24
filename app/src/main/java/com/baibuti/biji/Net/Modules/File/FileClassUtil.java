package com.baibuti.biji.Net.Modules.File;

import android.support.annotation.NonNull;

import com.baibuti.biji.Data.Models.FileClass;
import com.baibuti.biji.Interface.IPushCallBack;
import com.baibuti.biji.Net.Models.ReqBody.FileClassReqBody;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class FileClassUtil {

    private static final String AllFileClassUrl = Urls.FileClassUrl + "/all";
    private static final String OneFileClassUrl = Urls.FileClassUrl + "/one?id=%d";
    private static final String UpdateFileClassUrl = Urls.FileClassUrl + "/update";
    private static final String InsertFileClassUrl = Urls.FileClassUrl + "/insert";
    private static final String DeleteFileClassUrl = Urls.FileClassUrl + "/delete";
    private static final String PushFileClassUrl = Urls.FileClassUrl + "/push";

    public static FileClass[] getAllFileClasses() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllFileClassUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                FileClassReqBody[] rets = FileClassReqBody.getFileClassRespsFromJson(resp.getBody());
                return FileClassReqBody.toFileClasses(rets);
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

    public static FileClass getOneFileClass(int id) throws ServerErrorException {
        String url = String.format(Locale.CHINA, OneFileClassUrl, id);
        RespType resp = NetUtil.httpGetSync(url, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
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

    public static FileClass updateFileClass(FileClass fileClass) throws ServerErrorException {
        RespType resp = NetUtil.httpPostSync(
                UpdateFileClassUrl,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
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

    public static FileClass insertFileClass(FileClass fileClass) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                InsertFileClassUrl, NetUtil.PUT,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
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

    public static FileClass deleteFileClass(FileClass fileClass) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteFileClassUrl, NetUtil.DELETE,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
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

    public static void pushFileClassAsync(FileClass[] fileClasses, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        NetUtil.httpPostPutDeleteAsync(
                PushFileClassUrl, NetUtil.POST,
                FileClassReqBody.getJsonFromFileClassReqRodies(FileClassReqBody.toFileClassReqBodies(fileClasses)),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()),
                new Callback() {
                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call call, IOException e) { }

                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        if (code == 200) {
                            String newToken = response.headers().get("Authorization");
                            if (newToken != null && !(newToken.isEmpty()))
                                AuthMgr.getInstance().setToken(newToken);
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
    }
}
