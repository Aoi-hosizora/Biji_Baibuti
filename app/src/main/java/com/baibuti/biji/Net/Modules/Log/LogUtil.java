package com.baibuti.biji.Net.Modules.Log;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Net.Models.RespBody.LogResp;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.util.Locale;

public class LogUtil {

    private final static String OneLogUrl = Urls.LogUrl + "/one/%s";
    private final static String AllLogUrl = Urls.LogUrl + "/all";

    public static UtLog[] getAllLogs() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllLogUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
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
}
