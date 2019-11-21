package com.baibuti.biji.model.dao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://www.cnblogs.com/wangmars/p/4530670.html
 * attempt to re-open an already-closed object SQLiteDatabase
 * synchronized AtomicInteger
 */
public class DbManager {

    private static volatile DbManager instance;

    private AtomicInteger openCount = new AtomicInteger();
    private SQLiteOpenHelper helper;
    private volatile SQLiteDatabase db;

    public static synchronized DbManager getInstance(SQLiteOpenHelper helper) {
        if (instance == null) synchronized (DbManager.class) {
            if (instance == null) {
                instance = new DbManager();
                instance.helper = helper;
            }
        }

        return instance;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (openCount.incrementAndGet() == 1) {
            // Opening new database
            db = helper.getWritableDatabase();
        }
        return db;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (openCount.incrementAndGet() == 1) {
            // Opening new database
            db = helper.getReadableDatabase();
        }
        return db;
    }

    public synchronized void closeDatabase() {
        if (openCount.decrementAndGet() == 0) {
            // Closing database
            db.close();
        }
    }
}