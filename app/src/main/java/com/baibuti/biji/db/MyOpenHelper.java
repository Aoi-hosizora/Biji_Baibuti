package com.baibuti.biji.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.baibuti.biji.util.CommonUtil;

import java.util.Date;


public class MyOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "note.db";// 数据库文件名

    /**
     * 数据库版本号更新记录：
     *      1：
     *      create table db_group
     *      create table db_note
     *
     *      2：
     *      create table db_file_class
     */

    // private final static int DB_VERSION = 1;
    private final static int DB_VERSION = 2;// 数据库版本

    public MyOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private void Create_Db_group(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_group(" +
                "g_id integer primary key autoincrement, " +
                "g_name varchar, " +
                "g_order integer, " +
                "g_color varchar)");
    }

    private void Create_Db_note(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_note(" +
                "n_id integer primary key autoincrement, " +
                "n_title varchar, " +
                "n_content varchar, " +
                "n_group_id integer, " +
                "n_create_time datetime, " +
                "n_update_time datetime )");
    }

    private void Create_Db_file_class(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_file_class(" +
                "f_id integer primary key autoincrement, " +
                "f_name varchar, " +
                "f_order integer )");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建分类表
        Create_Db_group(db);

        // 创建笔记表
        Create_Db_note(db);

        // 创建文件分类表
        Create_Db_file_class(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion >= newVersion)
            return;

        // 相邻版本间数据库的更新
        int version = oldVersion;

        if (version == 1) {
            Create_Db_file_class(db);
            version = 2;
        }
    }

    /**
     * 判断表格是否存在
     * @param table 表名
     * @return
     */
    public boolean getTblExists(String table) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='" + table + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.moveToNext())
            if (cursor.getInt(0) > 0)
                return true;

        return false;
    }
}
