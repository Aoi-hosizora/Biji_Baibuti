package com.baibuti.biji.net.module.file;

import android.support.annotation.NonNull;

import com.baibuti.biji.data.model.FileClass;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.reqBody.FileClassReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.NetUtil;
import com.baibuti.biji.net.Urls;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class FileClassUtil {

    private static final String AllFileClassUrl = Urls.FileClassUrl + "/all";
    private static final String UpdateFileClassUrl = Urls.FileClassUrl + "/update";
    private static final String InsertFileClassUrl = Urls.FileClassUrl + "/insert";
    private static final String DeleteFileClassUrl = Urls.FileClassUrl + "/delete";
    private static final String PushFileClassUrl = Urls.FileClassUrl + "/push";
    private static final String GetShareCodeUrl = Urls.FileClassUrl + "/share";

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

    public static FileClass updateFileClass(FileClass fileClass) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                UpdateFileClassUrl, NetUtil.PUT,
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
                InsertFileClassUrl, NetUtil.POST,
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

    @Deprecated
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

    public static File getShareCode(String fileClassName){

        return NetUtil.httpGetFileSync(
                GetShareCodeUrl + "?foldername=" + fileClassName,
                "Share",
                fileClassName + ".png",
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );
    }
}
