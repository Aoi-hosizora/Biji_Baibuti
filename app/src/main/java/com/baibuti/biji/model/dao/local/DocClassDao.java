package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.daoInterface.IDocClassDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;

import java.util.ArrayList;
import java.util.List;

public class DocClassDao implements IDocClassDao {

    private static final String TBL_NAME = "tbl_file_class";

    private static final String COL_ID = "f_id";
    private static final String COL_NAME = "f_name";

    private DbOpenHelper helper;

    public DocClassDao(Context context) {
        helper = new DbOpenHelper(context);

        // 处理默认
        if (queryAllDocClasses().isEmpty())
            insertDocClass(new DocClass()); // DEF_CLASS_NAME
    }

    /**
     * 查询所有分类
     * @return 文件分类列表
     */
    @Override
    public List<DocClass> queryAllDocClasses() {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME;
        Cursor cursor = null;

        List<DocClass> docClassList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String className = cursor.getString(cursor.getColumnIndex(COL_NAME));

                docClassList.add(new DocClass(id, className));
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
    public DocClass queryDocClassById(int id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + id;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));

                return new DocClass(id, name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return null;
    }

    @Override
    public DocClass queryDocClassByName(String name) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from " + TBL_NAME + " where " + COL_NAME + " = \"" + name + "\"";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));

                return new DocClass(id, name);
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
    public DocClass queryDefaultDocClass() {

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
    public long insertDocClass(DocClass docClass) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            "(" + COL_NAME + ") " +
            "values(?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            stat.bindString(1, docClass.getName()); // COL_NAME

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
    public boolean updateDocClass(DocClass docClass) {

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, docClass.getName());

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?", new String[] { String.valueOf(docClass.getId()) });
        db.close();
        return ret > 0;
    }

    /**
     * 删除分类 (刷新 Order)
     * @param id 删除的分类 id
     * @return 是否成功删除
     */
    @Override
    public boolean deleteDocClass(int id) {

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
        return ret > 0;
    }
}
