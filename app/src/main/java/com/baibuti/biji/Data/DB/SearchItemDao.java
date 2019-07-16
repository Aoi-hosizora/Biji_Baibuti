package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Models.SearchItem;

import java.util.ArrayList;


public class SearchItemDao {


    private MyOpenHelper helper;
    private final static String TBL_NAME = "db_search_item_star";

    private final static String COL_URL = "sis_url";
    private final static String COL_TTL = "sis_title";
    private final static String COL_CNT = "sis_content";


    public SearchItemDao(Context context) {
        helper = new MyOpenHelper(context);
    }

    /**
     * 查询所有收藏项
     *
     * @return ArrayList<SearchItem>
     */
    public ArrayList<SearchItem> queryAllStarSearchItem() {
        SQLiteDatabase db = helper.getWritableDatabase();

        ArrayList<SearchItem> searchItems = new ArrayList<>();

        Cursor cursor = null;

        try {
            String sql = "select * from " + TBL_NAME;
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TTL));
                String content = cursor.getString(cursor.getColumnIndex(COL_CNT));
                searchItems.add(new SearchItem(title, content, url));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return searchItems;
    }

    /**
     * 查询一个收藏项
     *
     * @param Url
     * @return SearchItem
     *      null 未找到
     *      not null
     */
    public SearchItem queryOneStarSearchItem(String Url) {
        SQLiteDatabase db = helper.getWritableDatabase();

        SearchItem searchItem = null;

        Cursor cursor = null;

        try {
            String sql = "select * from " + TBL_NAME + " where " + COL_URL + " = \"" + Url + "\"";
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TTL));
                String content = cursor.getString(cursor.getColumnIndex(COL_CNT));

                searchItem = new SearchItem(title, content, url);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return searchItem;
    }

    /**
     * 判断是否存储收藏
     * @param searchItem
     * @return
     */
    public boolean hasStaredSearchItem(SearchItem searchItem) {
        return queryOneStarSearchItem(searchItem.getUrl()) != null;
    }

    /**
     * 插入新收藏项
     *
     * @param searchItem
     * @return
     */
    public long insertStarSearchItem(SearchItem searchItem) {

        SQLiteDatabase db = helper.getWritableDatabase();
        long ret = 0;

        String sql = "insert into " + TBL_NAME + " (" + COL_URL + ", " + COL_TTL + ", " + COL_CNT + ") values (?, ?, ?)";

        SQLiteStatement stat = db.compileStatement(sql);

        db.beginTransaction();
        try {
            stat.bindString(1, searchItem.getUrl()); // COL_URL
            stat.bindString(2, searchItem.getTitle()); // COL_TTL
            stat.bindString(3, searchItem.getContent()); // COL_CNT

            ret = stat.executeInsert();

            Log.e("", "insertStarSearchItem: " + "sql = " + sql + ", ret = " + ret);
            db.setTransactionSuccessful();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }
        return ret;
    }

    /**
     * 更新原有收藏项
     *
     * @param searchItem
     * @return 成功修改
     */
    public boolean updateStarSearchItem(SearchItem searchItem) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            values.put(COL_URL, searchItem.getUrl());
            values.put(COL_TTL, searchItem.getTitle());
            values.put(COL_CNT, searchItem.getContent());

            db.update(TBL_NAME, values, COL_URL + " = ?", new String[] { searchItem.getUrl() });

            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            db.endTransaction();
            db.close();
        }
        return true;
    }

    /**
     * 删除收藏项
     *
     * @param searchItem
     * @return
     */
    public long deleteStarSearchItem(SearchItem searchItem) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;

        db.beginTransaction();
        try {
            ret = db.delete(TBL_NAME, COL_URL + " = ?", new String[] {searchItem.getUrl()});
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }

        return ret;
    }
}
