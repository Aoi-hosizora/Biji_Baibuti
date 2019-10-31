package com.baibuti.biji.net.module.log;

import android.support.annotation.NonNull;

import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.respBody.LogResp;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.NetHelper;
import com.baibuti.biji.net.Urls;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

@Deprecated
public class LogUtil {

    private final static String OneLogUrl = Urls.LogUrl + "/one/%s";
    private final static String AllLogUrl = Urls.LogUrl + "/all";
    private final static String UpdateLogUrl = Urls.LogUrl + "/update";

    @Deprecated
    public static UtLog[] getAllLogs() throws ServerErrorException {
        RespType resp = NetHelper.httpGetSync(AllLogUrl, NetHelper.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
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
    @Deprecated
    public static UtLog getOneLog(LogModule logModule) throws ServerErrorException {
        String module = logModule.toString();

        String url = String.format(Locale.CHINA, OneLogUrl, module);
        RespType resp = NetHelper.httpGetSync(url, NetHelper.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
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
    @Deprecated
    public static void updateModuleLogAsync(UtLog utLog, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        NetHelper.httpPostPutDeleteAsync(
            UpdateLogUrl, NetHelper.POST,
            LogResp.toLogResp(utLog).toJson(),
            NetHelper.getOneHeader("Authorization", AuthMgr.getInstance().getToken()),
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
