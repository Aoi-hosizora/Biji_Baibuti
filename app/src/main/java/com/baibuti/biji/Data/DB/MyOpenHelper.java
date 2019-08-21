package com.baibuti.biji.Data.DB;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;


public class MyOpenHelper extends SQLiteOpenHelper {

    private final static String DEF_DB_NAME = "note.db";
    private final static String DB_USR_NAME = "biji_%s.db";

    /**
     * 数据库版本号更新记录：
     *
     * 1：
     * create table db_group
     * create table db_note
     *
     * 2：
     * create table db_file_class
     *
     * 3:
     * create table db_document
     *
     * 4:
     * create table db_search_item_star
     *
     * 5:
     * create table db_log
     *
     */

    private final static int DB_VERSION = 5;// 数据库版本



    public MyOpenHelper(Context context, String UsrName) {
        super(context, (UsrName == null || UsrName.isEmpty()) ?
            DEF_DB_NAME : (String.format(Locale.CHINA, DB_USR_NAME, UsrName)),
            null, DB_VERSION
        );
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

    private void Create_Db_document(SQLiteDatabase db) {
        db.execSQL("create table db_document(" +
                "doc_id integer primary key autoincrement, " +
                "doc_path varchar, " +
                "doc_class_name varchar )");
    }

    private void Create_Db_searchItemStar(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_search_item_star (" +
                "sis_url varchar primary key, " +
                "sis_title varchar, " +
                "sis_content varchar)");
    }

    private void Create_Db_log(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_log (" +
                "log_module varchar primary key, " +
                "log_ut datetime)");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建分类表
        Create_Db_group(db);

        // 创建笔记表
        Create_Db_note(db);

        // 创建文件分类表
        Create_Db_file_class(db);

        // 创建文件列表
        Create_Db_document(db);

        // 创建搜索收藏表
        Create_Db_searchItemStar(db);

        // 创建日志表
        Create_Db_log(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion >= newVersion)
            return;

        // 相邻版本间数据库的更新
        int version = oldVersion;

        if (version == 0) {
            Create_Db_note(db);
            Create_Db_group(db);
            version = 1;
        }

        if (version == 1) {
            Create_Db_file_class(db);
            version = 2;
        }

        if (version == 2) {
            Create_Db_document(db);
            version = 3;
        }

        if (version == 3) {
            Create_Db_searchItemStar(db);
            version = 4;
        }

        if (version == 4) {
            Create_Db_log(db);
            version = 5;
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
        if (cursor.moveToNext())
            if (cursor.getInt(0) > 0)
                return true;

        return false;
    }
}
