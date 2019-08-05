package com.baibuti.biji.Net.Models.RespBody;

import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Utils.OtherUtils.CommonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            return new LogResp(json.getString("module"), formatter.parse(json.getString("ut")));
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
        catch (ParseException ex) {
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

    // endregion LogResp <-> UtLog
}
