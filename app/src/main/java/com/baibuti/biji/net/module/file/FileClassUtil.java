package com.baibuti.biji.net.module.file;

import android.support.annotation.NonNull;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.FileClass;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.reqBody.FileClassReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.Urls;

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

    public static FileClass[] getAllFileClasses() throws ServerException {
        RespType resp = NetHelper.httpGetSync(AllFileClassUrl, NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                FileClassReqBody[] rets = FileClassReqBody.getFileClassRespsFromJson(resp.getBody());
                return FileClassReqBody.toFileClasses(rets);
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static FileClass updateFileClass(FileClass fileClass) throws ServerException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                UpdateFileClassUrl, NetHelper.PUT,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static FileClass insertFileClass(FileClass fileClass) throws ServerException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                InsertFileClassUrl, NetHelper.POST,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static FileClass deleteFileClass(FileClass fileClass) throws ServerException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                DeleteFileClassUrl, NetHelper.DELETE,
                FileClassReqBody.toFileClassReqBody(fileClass).toJson(),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                FileClassReqBody ret = FileClassReqBody.getFileClassRespFromJson(resp.getBody());
                return ret.toFileClass();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Deprecated
    public static void pushFileClassAsync(FileClass[] fileClasses, @NonNull IPushCallBack pushCallBack) throws ServerException {
        NetHelper.httpPostPutDeleteAsync(
                PushFileClassUrl, NetHelper.POST,
                FileClassReqBody.getJsonFromFileClassReqRodies(FileClassReqBody.toFileClassReqBodies(fileClasses)),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()),
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
                                AuthManager.getInstance().setToken(newToken);
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
    }

    public static File getShareCode(String fileClassName){

        return NetHelper.httpGetFileSync(
                GetShareCodeUrl + "?foldername=" + fileClassName,
                "Share",
                fileClassName + ".png",
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );
    }
}
