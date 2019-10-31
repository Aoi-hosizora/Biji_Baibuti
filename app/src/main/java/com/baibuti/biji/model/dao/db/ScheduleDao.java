package com.baibuti.biji.model.dao.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.module.schedule.ScheduleUtil;

public class ScheduleDao {

    private DbOpenHelper helper;
    private Context context;

    private final static String TBL_NAME = "db_schedule";

    private final static String COL_JSON = "schedule_json";

    public ScheduleDao(Context context) {
        this(context, (!(AuthManager.getInstance().getUserName().isEmpty())) ? AuthManager.getInstance().getUserName() : "");
    }

    @Deprecated
    public ScheduleDao(Context context, String username) {
        helper = new DbOpenHelper(context, username);
        this.context = context;
    }

    /**
     * 更新课表日志
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_Schedule);
    }

    /**
     * 进行 push pull
     */
    private void pushpull() {
        if (AuthManager.getInstance().isLogin()) {
            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_Schedule)) { // 本地新
                // TODO 异步
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Schedule);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_Schedule)) { // 服务器新
                // TODO 同步
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Schedule);
            }
        }
    }

    public String queryScheduleJson(){
        return queryScheduleJson(true);
    }

    /**
     * 查询课表json
     */
    @Deprecated
    public String queryScheduleJson(boolean isLogCheck) {

        if(isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        String scheduleJson = "";

        Cursor cursor = null;

        try {
            String sql = "select * from " + TBL_NAME ;
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                scheduleJson = cursor.getString(cursor.getColumnIndex(COL_JSON));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return scheduleJson;
    }

    public long insertScheduleJson(String scheduleJson) {
        return insertScheduleJson(scheduleJson, true);
    }

    /**
     * 插入课表json
     */
    @Deprecated
    public long insertScheduleJson(String scheduleJson, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();
        long ret = 0;

        String sql = "insert into " + TBL_NAME + " (" + COL_JSON  + ") values (?)";

        SQLiteStatement stat = db.compileStatement(sql);

        db.beginTransaction();
        try {
            stat.bindString(1, scheduleJson); // COL_JSON

            ret = stat.executeInsert();

            db.setTransactionSuccessful();

        }
        catch (Exception ex) {
            Log.e("测试", "insertScheduleJson.catch: 调用");
            ex.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }
        updateLog();

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (ScheduleUtil.insertSchedule(scheduleJson))
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Schedule);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public long deleteScheduleJson() {
        return deleteScheduleJson(true);
    }

    /**
     * 删除课表json
     */
    @Deprecated
    public long deleteScheduleJson(boolean isLogCheck) {


        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;

        db.beginTransaction();
        try {
            ret = db.delete(TBL_NAME, null, null);

            db.setTransactionSuccessful();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }

        updateLog();

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (ScheduleUtil.deleteSchedule())
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Schedule);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 更新课表json
     */
    public void updateScheduleJson(String scheduleJson) {
        updateScheduleJson(scheduleJson, true);
    }

    /**
     * 更新课表json
     */
    @Deprecated
    public void updateScheduleJson(String scheduleJson, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("schedule_json", scheduleJson);

        db.update(TBL_NAME, values, null, null);
        db.close();
        updateLog();

        if (AuthManager.getInstance().isLogin()) {
            try {
                if (ScheduleUtil.updateSchedule(scheduleJson))
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Schedule);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
    }

}
