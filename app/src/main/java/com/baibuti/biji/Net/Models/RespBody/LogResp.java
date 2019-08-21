package com.baibuti.biji.Net.Models.RespBody;

import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Utils.OtherUtils.DateColorUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class LogResp {

    private String module;
    private Date ut;

    public LogResp(String module, Date ut) {
        this.module = module;
        this.ut = ut;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Date getUt() {
        return ut;
    }

    public void setUt(Date ut) {
        this.ut = ut;
    }

    // region LogResp <-> Json

    public String toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("module", module);
            jsonObject.put("ut", DateColorUtil.Date2Str(ut));
            return jsonObject.toString();
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Json Str -> LogResp
     * @param json
     * @return
     */
    public static LogResp toLogRespFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return toLogRespFromJson(jsonObject);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> LogResp
     * @param json
     * @return
     */
    public static LogResp toLogRespFromJson(JSONObject json) {
        try {
            return new LogResp(json.getString("module"), DateColorUtil.Str2Date(json.getString("ut")));
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json Str -> LogResp[]
     * @param json
     * @return
     */
    public static LogResp[] toLogRespsFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            return toLogRespsFromJson(jsonArray);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> LogResp[]
     * @param jsonArray
     * @return
     */
    public static LogResp[] toLogRespsFromJson(JSONArray jsonArray) {
        try {
            LogResp[] rets = new LogResp[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
                rets[i] = toLogRespFromJson(jsonArray.getJSONObject(i));
            return rets;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // endregion LogResp <-> Json

    // region LogResp <-> UtLog

    public UtLog toUtLog() {
        return new UtLog(this.module, this.ut);
    }

    public static UtLog[] toUtLogs(LogResp[] logResps) {
        UtLog[] utLogs = new UtLog[logResps.length];
        for (int i = 0; i < logResps.length; i++)
            utLogs[i] = logResps[i].toUtLog();
        return utLogs;
    }

    public static LogResp toLogResp(UtLog utLog) {
        if (utLog == null)
            return null;
        return new LogResp(utLog.getModule(), utLog.getUpdateTime());
    }

    // endregion LogResp <-> UtLog
}
