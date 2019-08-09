package com.baibuti.biji.Net.Modules.Log;

import android.support.annotation.NonNull;
import android.util.Log;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Interface.IPushCallBack;
import com.baibuti.biji.Net.Models.RespBody.LogResp;
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

public class LogUtil {

    private final static String OneLogUrl = Urls.LogUrl + "/one/%s";
    private final static String AllLogUrl = Urls.LogUrl + "/all";
    private final static String UpdateLogUrl = Urls.LogUrl + "/update";

    public static UtLog[] getAllLogs() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllLogUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                LogResp[] rets = LogResp.toLogRespsFromJson(resp.getBody());
                return LogResp.toUtLogs(rets);
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

    /**
     * 获得服务器上一个日志
     * @param logModule
     * @return
     * @throws ServerErrorException
     */
    public static UtLog getOneLog(LogModule logModule) throws ServerErrorException {
        String module = logModule.toString();

        String url = String.format(Locale.CHINA, OneLogUrl, module);
        RespType resp = NetUtil.httpGetSync(url, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                LogResp ret = LogResp.toLogRespFromJson(resp.getBody());
                return ret.toUtLog();
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

    /**
     * 异步更新指定模块的日志
     * @param utLog
     * @return
     * @throws ServerErrorException
     */
    public static void updateModuleLogAsync(UtLog utLog, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        Log.e("", "updateModuleLog: " + utLog.getModule());
        Log.e("", "updateModuleLog: " + LogResp.toLogResp(utLog).toJson() );
        NetUtil.httpPostPutDeleteAsync(
            UpdateLogUrl, NetUtil.POST,
            LogResp.toLogResp(utLog).toJson(),
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
