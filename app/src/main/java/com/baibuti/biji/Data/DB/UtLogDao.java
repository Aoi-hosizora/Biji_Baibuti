package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Utils.OtherUtils.DateColorUtil;

import java.util.Date;


public class UtLogDao {

    private MyOpenHelper helper;

    private final static String TBL_NAME = "db_log";

    private final static String COL_MODULE = "log_module";
    private final static String COL_UT = "log_ut";

    public UtLogDao(Context context) {
        this(context, (!(AuthMgr.getInstance().getUserName().isEmpty())) ? AuthMgr.getInstance().getUserName() : "");
    }

    public UtLogDao(Context context, String username) {
        helper = new MyOpenHelper(context, username);
    }

    /**
     * 查询存在日志，并创建
     * @param module
     */
    private void buildModuleLog(String module) {
        if (queryOneModuleLog(module, false) == null) {
            SQLiteDatabase db = helper.getWritableDatabase();

            String sql = "insert into " + TBL_NAME + " (" + COL_MODULE + ", " + COL_UT + ") values (?, ?)";

            SQLiteStatement stat = db.compileStatement(sql);

            db.beginTransaction();
            try {
                stat.bindString(1, module); // COL_MODULE
                stat.bindString(2, DateColorUtil.Date2Str(new Date())); // COL_UT

                stat.executeInsert();
                db.setTransactionSuccessful();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            finally {
                db.endTransaction();
                db.close();
            }
        }
    }

    private UtLog queryOneModuleLog(String module) {
        return queryOneModuleLog(module, true);
    }

    /**
     * 查询日志项
     *
     * @param module
     * @param isCheck
     * @return UtLog
     */
    private UtLog queryOneModuleLog(String module, boolean isCheck) {
        if (isCheck)
            buildModuleLog(module);

        SQLiteDatabase db = helper.getWritableDatabase();
        UtLog utLog = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + TBL_NAME + " where " + COL_MODULE + "=\"" + module + "\"";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                Date ut = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UT)));
                utLog = new UtLog(module, ut);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return utLog;
    }

    /**
     * 更新日志项
     *
     * @param module
     */
    private void updateOneModuleLog(String module, Date update_time) {
        buildModuleLog(module);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_MODULE, module);
            values.put(COL_UT, DateColorUtil.Date2Str(update_time));
            db.update(TBL_NAME, values, COL_MODULE + " = ?", new String[] { module });

            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * 获得本地日志
     * @param logModule
     * @return
     */
    public UtLog getLog(LogModule logModule) {
        UtLog ret = null;
        switch (logModule) {
            case Mod_Note:
                ret = queryOneModuleLog(UtLog.Log_Note);
                break;
            case Mod_Group:
                ret = queryOneModuleLog(UtLog.Log_Group);
                break;
            case Mod_Star:
                ret = queryOneModuleLog(UtLog.Log_Star);
                break;
            case Mod_FileClass:
                ret = queryOneModuleLog(UtLog.Log_FileClass);
                break;
            case Mod_Document:
                ret = queryOneModuleLog(UtLog.Log_Document);
                break;
            case Mod_Schedule:
                ret = queryOneModuleLog(UtLog.Log_Schedule);
                break;
        }
        return ret;
    }

    /**
     * 更新本地日志
     * @param logModule
     */
    public void updateLog(LogModule logModule) {
        Log.e("测试", "updateLog: " + logModule.toString());
        switch (logModule) {
            case Mod_Note:
                updateOneModuleLog(UtLog.Log_Note, new Date());
                break;
            case Mod_Group:
                updateOneModuleLog(UtLog.Log_Group, new Date());
                break;
            case Mod_Star:
                updateOneModuleLog(UtLog.Log_Star, new Date());
                break;
            case Mod_FileClass:
                updateOneModuleLog(UtLog.Log_FileClass, new Date());
                break;
            case Mod_Document:
                updateOneModuleLog(UtLog.Log_Document, new Date());
                break;
            case Mod_Schedule:
                updateOneModuleLog(UtLog.Log_Schedule, new Date());
                break;
        }
    }

    /**
     * 更新本地日志为服务器日志
     * @param utLog
     */
    public void updateLog(UtLog utLog) {
        switch (utLog.getModule()) {
            case UtLog.Log_Note:
                updateOneModuleLog(UtLog.Log_Note, utLog.getUpdateTime());
                break;
            case UtLog.Log_Group:
                updateOneModuleLog(UtLog.Log_Group, utLog.getUpdateTime());
                break;
            case UtLog.Log_Star:
                updateOneModuleLog(UtLog.Log_Star, utLog.getUpdateTime());
                break;
            case UtLog.Log_FileClass:
                updateOneModuleLog(UtLog.Log_FileClass, utLog.getUpdateTime());
                break;
            case UtLog.Log_Document:
                updateOneModuleLog(UtLog.Log_Document, utLog.getUpdateTime());
                break;
            case UtLog.Log_Schedule:
                updateOneModuleLog(UtLog.Log_Schedule, utLog.getUpdateTime());
                break;
        }
    }
}
