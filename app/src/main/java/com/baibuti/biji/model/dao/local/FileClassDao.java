package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.daoInterface.IFileClassDao;
import com.baibuti.biji.model.po.DocClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileClassDao implements IFileClassDao {

    private static final String TBL_NAME = "tbl_file_class";

    private static final String COL_ID = "f_id";
    private static final String COL_NAME = "f_name";
    private static final String COL_ORDER = "f_order";

    private DbOpenHelper helper;

    public FileClassDao(Context context) {
        helper = new DbOpenHelper(context);

        // 处理默认
        if (queryAllFileClasses().isEmpty())
            insertFileClass(new DocClass()); // DEF_CLASS_NAME

        // 预处理顺序
        precessOrder();
    }

    /**
     * 查询所有分类
     * @return 文件分类列表
     */
    @Override
    public List<DocClass> queryAllFileClasses() {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME;
        Cursor cursor = null;

        List<DocClass> docClassList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String className = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));

                docClassList.add(new DocClass(id, className, order));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return docClassList;
    }

    /**
     * 根据分类 id 查询分类
     * @param id 分类 id
     * @return 一个分类
     */
    @Override
    public DocClass queryFileClassById(int id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + id;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));

                return new DocClass(id, name, order);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return null;
    }

    /**
     * 查询默认分类
     * @return 返回数据库中的默认分类
     */
    @Override
    public DocClass queryDefaultFileClass() {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_NAME + " = \"" + DocClass.DEF_CLASS_NAME + "\"";

        DocClass docClass = null;
        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {

                docClass = new DocClass();
                docClass.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                docClass.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                docClass.setOrder(cursor.getInt(cursor.getColumnIndex(COL_ORDER)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return docClass;
    }

    /**
     * 添加分组 (添加在末尾，不更新 Order)
     * @param docClass 新分类，自动编码
     * @return 分类 id
     */
    @Override
    public long insertFileClass(DocClass docClass) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            "(" + COL_NAME + ", " + COL_ORDER + ") " +
            "values(?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            docClass.setOrder(queryAllFileClasses().size()); // 插入到最后

            stat.bindString(1, docClass.getName()); // COL_NAME
            stat.bindLong(2, docClass.getOrder()); // COL_ORDER

            ret_id = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return ret_id;
    }

    /**
     * 更新分类 (刷新 Order)
     * @param docClass 覆盖更新
     * @return 是否成功更新
     */
    @Override
    public boolean updateFileClass(DocClass docClass) {

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, docClass.getName());
        values.put(COL_ORDER, docClass.getOrder());

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?", new String[] { String.valueOf(docClass.getId()) });
        db.close();

        // 更新后刷新 Order
        precessOrder();
        return ret > 0;
    }

    /**
     * 删除分类 (刷新 Order)
     * @param id 删除的分类 id
     * @return 是否成功删除
     */
    @Override
    public boolean deleteFileClass(int id) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen())
                db.close();
        }

        // 删除后刷新 Order
        precessOrder();
        return ret > 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 处理顺序 (所有操作前 以及 删除操作后)
     */
    private void precessOrder() {
        List<DocClass> docClasses = queryAllFileClasses();
        Collections.sort(docClasses);

        for (int i = 0; i < docClasses.size(); i++) {
            if (docClasses.get(i).getOrder() != i) {
                docClasses.get(i).setOrder(i);
                updateFileClass(docClasses.get(i));
            }
        }
    }
}
