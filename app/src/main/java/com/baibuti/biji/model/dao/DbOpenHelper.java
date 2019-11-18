package com.baibuti.biji.model.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.baibuti.biji.model.dao.local.DocClassDao;
import com.baibuti.biji.model.dao.local.DocumentDao;
import com.baibuti.biji.model.dao.local.GroupDao;
import com.baibuti.biji.model.dao.local.NoteDao;
import com.baibuti.biji.model.dao.local.SearchItemDao;

public class DbOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "biji.db";

    /**
     * 数据库版本号更新记录：
     *
     * 1：
     * 重构数据库
     */

    private final static int DB_VERSION = 1; // 数据库版本

    /**
     * 本地数据库访问
     */
    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建分组表
        GroupDao.create_tbl(db);

        // 创建笔记表
        NoteDao.create_tbl(db);

        // 创建文件分类表
        DocClassDao.create_tbl(db);

        // 创建文件列表
        DocumentDao.create_tbl(db);

        // 创建搜索收藏表
        SearchItemDao.create_tbl(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion >= newVersion)
            return;

        // 相邻版本间数据库的更新
        switch (oldVersion) {
            case 0:
                onCreate(db);
            case 1:
                break;
        }
    }
}
