package com.baibuti.biji.net.module.schedule;

import android.util.Log;

import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.NetHelper;
import com.baibuti.biji.service.Urls;

import java.io.IOException;

import io.reactivex.annotations.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class ScheduleUtil {

    private static final String InsertScheduleUrl = Urls.Schedule + "/upload";
    private static final String DeleteScheduleUrl = Urls.Schedule + "/delete";
    private static final String UpdateScheduleUrl = Urls.Schedule + "/update";
    private static final String GetScheduleUrl = Urls.Schedule + "/download";
    private static final String PushScheduleUrl = Urls.Schedule + "/push";

    public static boolean insertSchedule(String scheduleJson) throws ServerErrorException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                InsertScheduleUrl, NetHelper.POST,
                "{\"schedulejson\":" + scheduleJson + "}",
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

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

    public static boolean deleteSchedule() throws ServerErrorException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                DeleteScheduleUrl, NetHelper.DELETE,
                "{}",
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

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

    public static boolean updateSchedule(String scheduleJson) throws ServerErrorException {
        RespType resp = NetHelper.httpPostPutDeleteSync(
                UpdateScheduleUrl, NetHelper.PUT,
                "{\"schedulejson\":" + scheduleJson + "}",
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

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

    public static String getSchedule() throws ServerErrorException {
        RespType resp = NetHelper.httpGetSync(GetScheduleUrl, NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                Log.e("测试", "getSchedule from backend: " + resp.getBody());
                return resp.getBody();
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
    public static void pushSchedule(String scheduleJson, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        NetHelper.httpPostPutDeleteAsync(
                PushScheduleUrl, NetHelper.POST,
                "{\"schedulejson\":" + scheduleJson + "}",
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



}
