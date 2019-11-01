package com.baibuti.biji.model.dao.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.model.po.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchItemDao implements ISearchItemDao {

    private final static String TBL_NAME = "tbl_search_item";

    private final static String COL_URL = "sis_url";
    private final static String COL_TITLE = "sis_title";
    private final static String COL_CONTENT = "sis_content";

    private DbOpenHelper helper;

    public SearchItemDao(Context context) {
        helper = new DbOpenHelper(context);
    }

    /**
     * 查询所有收藏项
     * @return 收藏列表
     */
    @Override
    public List<SearchItem> queryAllSearchItems() {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME;
        Cursor cursor = null;

        List<SearchItem> searchItems = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                searchItems.add(new SearchItem(title, content, url));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return searchItems;
    }

    /**
     * 根据 url 查询收藏项
     * @param Url 记录连接
     * @return 指定收藏项
     */
    @Override
    public SearchItem querySearchItemByUrl(String Url) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME + " where " + COL_URL + " = \"" + Url + "\"";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                String url = cursor.getString(cursor.getColumnIndex(COL_URL));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                return new SearchItem(title, content, url);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return null;
    }

    /**
     * 插入收藏项
     * @param searchItem 新收藏
     * @return 收藏的记录 ID (无用)
     */
    public long insertSearchItem(SearchItem searchItem) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            " (" + COL_URL + ", " + COL_TITLE + ", " + COL_CONTENT + ") " +
            "values (?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            stat.bindString(1, searchItem.getUrl()); // COL_URL
            stat.bindString(2, searchItem.getTitle()); // COL_TITLE
            stat.bindString(3, searchItem.getContent()); // COL_CONTENT

            ret_id = stat.executeInsert();
            db.setTransactionSuccessful();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }

        return ret_id;
    }

    /**
     * 删除收藏项
     * @param url 收藏的链接
     * @return 是否成功删除 (删除 1 项)
     */
    @Override
    public boolean deleteSearchItem(String url) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_URL + " = ?", new String[] { url });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen())
                db.close();
        }

        return ret == 1;
    }

    /**
     * 批量删除收藏项
     * @param searchItems 收藏项集合
     * @return 删除的项数
     */
    @Override
    public int deleteSearchItems(List<SearchItem> searchItems) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret_num = 0;
        if (searchItems != null && searchItems.size() > 0) {
            db.beginTransaction();
            try {
                for (SearchItem searchItem : searchItems)
                    ret_num += db.delete(TBL_NAME, COL_URL + " = ?",
                        new String[] { searchItem.getUrl() } );

                db.setTransactionSuccessful();
            }
            catch (Exception e) {
                ret_num = 0;
                e.printStackTrace();
            }
            finally {
                db.endTransaction();
                db.close();
            }
        }
        return ret_num;
    }
}
