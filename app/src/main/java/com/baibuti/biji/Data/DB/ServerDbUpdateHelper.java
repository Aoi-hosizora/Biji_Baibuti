package com.baibuti.biji.Data.DB;

import android.content.Context;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Log.LogUtil;

import java.util.Date;

public class ServerDbUpdateHelper {

    /**
     * 返回是否本地比服务器日志新
     * @param logModule
     * @return
     */
    public static boolean isLocalNewer(Context context, LogModule logModule) {
        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        Date server;
        try {
            server = LogUtil.getOneLog(logModule).getUpdateTime();
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
            return true;
        }
        return local.after(server);
    }
}
