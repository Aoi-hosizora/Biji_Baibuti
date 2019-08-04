package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
     * 查询是否存在所有日志
     */
    private void buildAllModuleLog() {
        for (String module : UtLog.Log_Modules) {
            buildModuleLog(module);
        }
    }

    /**
     * 查询存在日志，并创建
     * @param module
     */
    private void buildModuleLog(String module) {
        if (queryOneModuleLog(module) == null) {

            SQLiteDatabase db = helper.getWritableDatabase();
            long ret = 0;

            String sql = "insert into " + TBL_NAME + " (" + COL_MODULE + ", " + COL_UT + ") values (?, ?)";

            SQLiteStatement stat = db.compileStatement(sql);

            db.beginTransaction();
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                stat.bindString(1, module); // COL_MODULE
                stat.bindString(2, formatter.format(new Date())); // COL_UT

                ret = stat.executeInsert();
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

    /**
     * 查询日志项
     *
     * @param module
     * @return UtLog
     */
    private UtLog queryOneModuleLog(String module) {
        buildAllModuleLog();

        SQLiteDatabase db = helper.getWritableDatabase();
        UtLog utLog = null;
        Cursor cursor = null;
        try {
            String sql = "select * from " + TBL_NAME + " where " + COL_MODULE + " = \"" + module + "\"";
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                Date ut = formatter.parse(cursor.getString(cursor.getColumnIndex(COL_UT)));
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
    private void updateOneModuleLog(String module) {
        buildModuleLog(module);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

            values.put(COL_MODULE, module);
            values.put(COL_UT, formatter.format(new Date()));
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
     * 更新笔记日志
     */
    public void updateNoteLog() {
        updateOneModuleLog(UtLog.Log_Note);
    }

    /**
     * 更新分组日志
     */
    public void updateGroupLog() {
        updateOneModuleLog(UtLog.Log_Group);
    }

    /**
     * 更新收藏日志
     */
    public void updateStarLog() {
        updateOneModuleLog(UtLog.Log_Star);
    }

    /**
     * 更新文件日志
     */
    public void updateFileLog() {
        updateOneModuleLog(UtLog.Log_File);
    }

    /**
     * 更新课程日志
     */
    public void updateScheduleLog() {
        updateOneModuleLog(UtLog.Log_Schedule);
    }
}
