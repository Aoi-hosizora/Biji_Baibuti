package com.baibuti.biji.model.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class DbOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "biji.db";
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
     * 6:
     * create table db_schedule
     *
     * 7:
     * drop table db_schedule
     * drop table db_log
     * alter table db_xx rename to tbl_xxx
     */

    private final static int DB_VERSION = 6;// 数据库版本

    /**
     * 本地数据库访问
     */
    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Deprecated
    public DbOpenHelper(Context context, String UsrName) {
        super(context, (UsrName == null || UsrName.isEmpty()) ?
            DB_NAME : (String.format(Locale.CHINA, DB_USR_NAME, UsrName)),
            null, DB_VERSION
        );
    }

    private void CreateNoteTbl(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_note(" +
            "n_id integer primary key autoincrement, " +
            "n_title varchar, " +
            "n_content varchar, " +
            "n_group_id integer, " +
            "n_create_time datetime, " +
            "n_update_time datetime )");
    }

    private void CreateGroupTbl(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_group(" +
            "g_id integer primary key autoincrement, " +
            "g_name varchar not null, " +
            "g_order integer, " +
            "g_color varchar)");
    }

    private void CreateFileClassTbl(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_file_class(" +
            "f_id integer primary key autoincrement, " +
            "f_name varchar, " +
            "f_order integer)");
    }

    private void CreateDocumentTbl(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_document(" +
            "doc_id integer primary key autoincrement, " +
            "doc_path varchar, " +
            "doc_class_name varchar, " +
            "doc_name varchar)");
    }

    private void CreateSearchItemTbl(SQLiteDatabase db) {
        db.execSQL("create table if not exists db_search_item_star (" +
            "sis_url varchar primary key, " +
            "sis_title varchar, " +
            "sis_content varchar)");
    }

    // @Deprecated
    // private void CreateScheduleTbl(SQLiteDatabase db) {
    //     db.execSQL("create table if not exists db_schedule (" +
    //         "schedule_json varchar primary key)");
    // }

    // @Deprecated
    // private void CreateLogTbl(SQLiteDatabase db) {
    //     db.execSQL("create table if not exists db_log (" +
    //         "log_module varchar primary key, " +
    //         "log_ut datetime)");
    // }

    private void DropScheduleTbl(SQLiteDatabase db) {
        db.execSQL("drop table if exists db_schedule");
    }

    private void DropLogTbl(SQLiteDatabase db) {
        db.execSQL("drop table if exists db_log");
    }

    private void RenameTblName(SQLiteDatabase db) {
        db.execSQL("alter table db_note rename to tbl_note");
        db.execSQL("alter table db_group rename to tbl_group");
        db.execSQL("alter table db_file_class rename to tbl_file_class");
        db.execSQL("alter table db_document rename to tbl_document");
        db.execSQL("alter table db_search_item_star rename to tbl_search_item");
    }

    private void AddUniqueConstraint(SQLiteDatabase db) {
        db.execSQL("create index tbl_group_g_name_unique on tbl_group(g_name)");
        db.execSQL("create index tbl_file_class_g_name_unique on tbl_file_class(f_name)");
    }

    private void AddNotNullConstraint(SQLiteDatabase db) {
        db.execSQL("alter table tbl_db_file_class rename to tbl_db_file_class_tmp");
        db.execSQL("create table if not exists db_file_class(" +
            "f_id integer primary key autoincrement, " +
            "f_name varchar not null, " +
            "f_order integer)");
        db.execSQL("insert into db_file_class select * from tbl_db_file_class_tmp");
        db.execSQL("drop table tbl_db_file_class_tmp");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建分组表
        CreateGroupTbl(db);

        // 创建笔记表
        CreateNoteTbl(db);

        // 创建文件分类表
        CreateFileClassTbl(db);

        // 创建文件列表
        CreateDocumentTbl(db);

        // 创建搜索收藏表
        CreateSearchItemTbl(db);

        // // 创建课表json表
        // CreateScheduleTbl(db);

        // // 创建日志表
        // CreateLogTbl(db);

        // 统一重命名表明
        RenameTblName(db);

        // 增加 UNIQUE 约束
        AddUniqueConstraint(db);

        // 增加 NOTNULL 约束
        AddNotNullConstraint(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion >= newVersion)
            return;

        // 相邻版本间数据库的更新
        switch (oldVersion) {
            case 0:
                CreateNoteTbl(db);
                CreateGroupTbl(db);
            case 1:
                CreateFileClassTbl(db);
            case 2:
                CreateDocumentTbl(db);
            case 3:
                CreateSearchItemTbl(db);
            case 4:
                // CreateLogTbl(db);
            case 5:
                // CreateScheduleTbl(db);
            case 6:
                DropScheduleTbl(db);
                DropLogTbl(db);
                RenameTblName(db);
                AddUniqueConstraint(db);
                AddNotNullConstraint(db);
        }
    }

    // /**
    //  * 判断表格是否存在
    //  * @param table 表名
    //  */
    // public boolean isTblExists(String table) {
    //     SQLiteDatabase db = getWritableDatabase();
    //     String sql = "SELECT COUNT(*) FROM sqlite_master where type='table' and name='" + table + "'";
    //     Cursor cursor = db.rawQuery(sql, null);
    //
    //     boolean ret = cursor.moveToNext() && cursor.getInt(0) > 0;
    //
    //     if (!cursor.isClosed()) cursor.close();
    //     if (db.isOpen()) db.close();
    //
    //     return ret;
    // }
}
