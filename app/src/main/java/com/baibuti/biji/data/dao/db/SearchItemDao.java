package com.baibuti.biji.data.dao.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.data.dao.DbOpenHelper;
import com.baibuti.biji.data.model.LogModule;
import com.baibuti.biji.data.model.SearchItem;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.module.star.StarUtil;

import java.util.ArrayList;


public class SearchItemDao {

    private DbOpenHelper helper;
    private Context context;

    private final static String TBL_NAME = "db_search_item_star";

    private final static String COL_URL = "sis_url";
    private final static String COL_TTL = "sis_title";
    private final static String COL_CNT = "sis_content";

    public SearchItemDao(Context context) {
        this(context, (!(AuthMgr.getInstance().getUserName().isEmpty())) ? AuthMgr.getInstance().getUserName() : "");
    }

    @Deprecated
    public SearchItemDao(Context context, String username) {
        helper = new DbOpenHelper(context, username);
        this.context = context;
    }

    /**
     * 更新收藏日志
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_Star);
    }

    /**
     * 进行 push pull
     */
    @Deprecated
    private void pushpull() {
        if (AuthMgr.getInstance().isLogin()) {
            Log.e("", "run: queryAllStarSearchItems");
            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_Star)) { // 本地新
                // TODO 异步
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Star);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_Star)) { // 服务器新
                // TODO 同步
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Star);
            }
        }
    }

    /**
     * 查询所有收藏项，在线
     *
     * @return ArrayList<SearchItem>
     */
    public ArrayList<SearchItem> queryAllStarSearchItems() {
        return queryAllStarSearchItems(true);
    }

    /**
     * 查询所有收藏项
     *
     * @param isLogCheck
     * @return ArrayList<SearchItem>
     */
    @Deprecated
    ArrayList<SearchItem> queryAllStarSearchItems(boolean isLogCheck) {

        if (isLogCheck) pushpull();

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
    private SearchItem queryOneStarSearchItem(String Url) {
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

    public long insertStarSearchItem(SearchItem searchItem) {
        return insertStarSearchItem(searchItem, true);
    }

    /**
     * 插入新收藏项
     *
     * @param searchItem
     * @param isLogCheck
     * @return
     */
    @Deprecated
    long insertStarSearchItem(SearchItem searchItem, boolean isLogCheck) {

        if (isLogCheck) pushpull();

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
        updateLog();

        if (isLogCheck && AuthMgr.getInstance().isLogin()) {
            try {
                if (StarUtil.insertStar(searchItem) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Star);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public long deleteStarSearchItem(SearchItem searchItem) {
        return deleteStarSearchItem(searchItem, true);
    }

    /**
     * 删除收藏项
     *
     * @param searchItem
     * @return
     */
    @Deprecated
    long deleteStarSearchItem(SearchItem searchItem, boolean isLogCheck) {


        if (isLogCheck) pushpull();

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

        updateLog();

        if (isLogCheck && AuthMgr.getInstance().isLogin()) {
            try {
                if (StarUtil.deleteStar(searchItem) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Star);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public int deleteStarSearchItems(ArrayList<SearchItem> searchItems) {
        return deleteStarSearchItems(searchItems, false);
    }

    /**
     * 批量删除收藏项
     * @param searchItems
     */
    @Deprecated
    int deleteStarSearchItems(ArrayList<SearchItem> searchItems, boolean isLogCheck) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            if (searchItems != null && searchItems.size() > 0) {
                db.beginTransaction();
                try {
                    for (SearchItem searchItem : searchItems) {
                        ret += db.delete(TBL_NAME, COL_URL + " = ?", new String[]{searchItem.getUrl()});
                    }
                    db.setTransactionSuccessful();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    db.endTransaction();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        updateLog();

        if (isLogCheck) pushpull();

        return ret;
    }
}
