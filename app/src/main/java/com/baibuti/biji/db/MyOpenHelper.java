package com.baibuti.biji.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baibuti.biji.util.CommonUtil;

import java.util.Date;

/**
 * 作者：Sendtion on 2016/10/24 0024 15:14
 * 邮箱：sendtion@163.com
 * 博客：http://sendtion.cn
 * 描述：数据库帮助类
 */

public class MyOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "note.db";// 数据库文件名
    private final static int DB_VERSION = 1;// 数据库版本

    public MyOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建分类表
        db.execSQL("create table db_group(" +
                "g_id integer primary key autoincrement, " +
                "g_name varchar, " +
                "g_order integer, " +
                "g_color varchar)");

        //创建笔记表
        db.execSQL("create table db_note(" +
                "n_id integer primary key autoincrement, " +
                "n_title varchar, " +
                "n_content varchar, " +
                "n_group_id integer, " +
                "n_create_time datetime, " +
                "n_update_time datetime )");

        //创建文件分类表
        db.execSQL("create table db_file_class(" +
                "f_id integer primary key autoincrement, " +
                "f_name varchar, " +
                "f_order integer )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
