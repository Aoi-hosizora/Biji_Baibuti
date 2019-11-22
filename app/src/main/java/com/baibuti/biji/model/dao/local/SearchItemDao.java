package com.baibuti.biji.model.dao.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.baibuti.biji.model.dao.DbManager;
import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.po.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchItemDao {

    private final static String TBL_NAME = "tbl_search_item";

    private final static String COL_ID = "sis_id";
    private final static String COL_TITLE = "sis_title";
    private final static String COL_URL = "sis_url";
    private final static String COL_CONTENT = "sis_content";

    private DbManager dbMgr;

    public SearchItemDao(Context context) {
        this.dbMgr = DbManager.getInstance(new DbOpenHelper(context));
    }

    public static void create_tbl(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
            COL_ID      + " integer PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE   + " varchar NOT NULL, " +
            COL_URL     + " varchar NOT NULL, " +
            COL_CONTENT + " varchar NOT NULL)"
        );
    }

    /**
     * 查询所有收藏项
     * @return 收藏列表
     */
    public List<SearchItem> queryAllSearchItems() {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        String sql = "select * from " + TBL_NAME;
        Cursor cursor = null;

        List<SearchItem> searchItems = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                searchItems.add(new SearchItem(id, title, content, url));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
        }
        return searchItems;
    }

    /**
     * 根据 url 查询收藏项
     * @param id 记录连接
     * @return 指定收藏项
     */
    public SearchItem querySearchItemById(int id) {
        SQLiteDatabase db = dbMgr.getReadableDatabase();
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + id;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                return new SearchItem(id, title, content, url);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
        }

        return null;
    }

    /**
     * 插入收藏项
     * @param searchItem 新收藏
     * @return SUCCESS | FAILED
     */
    public DbStatusType insertSearchItem(SearchItem searchItem) {

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            " (" + COL_URL + ", " + COL_TITLE + ", " + COL_CONTENT + ") " +
            "values (?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        try {
            stat.bindString(1, searchItem.getUrl()); // COL_URL
            stat.bindString(2, searchItem.getTitle()); // COL_TITLE
            stat.bindString(3, searchItem.getContent()); // COL_CONTENT

            long ret_id = stat.executeInsert();
            if (ret_id != -1) {
                searchItem.setId((int) ret_id);
                db.setTransactionSuccessful();
                return DbStatusType.SUCCESS;
            } else
                return DbStatusType.FAILED;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return DbStatusType.FAILED;
        }
        finally {
            db.endTransaction();
            dbMgr.closeDatabase();
        }
    }

    /**
     * 删除收藏项
     * @param id 收藏的链接
     * @return SUCCESS | FAILED
     */
    public DbStatusType deleteSearchItem(int id) {

        SQLiteDatabase db = dbMgr.getWritableDatabase();

        int ret = db.delete(TBL_NAME, COL_ID + " = " + id, null);
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }

    /**
     * 批量删除收藏项
     * @param searchItems 收藏项集合
     * @return 删除的项数
     */
    public int deleteSearchItems(List<SearchItem> searchItems) {
        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String[] id_str = new String[searchItems.size()];
        for (int i = 0; i < id_str.length; i++)
            id_str[i] = String.valueOf(searchItems.get(i).getId());

        int ret = db.delete(TBL_NAME, COL_ID + " in (" +  TextUtils.join(", ", id_str) + ")", null);
        dbMgr.closeDatabase();
        return ret;
    }
}
