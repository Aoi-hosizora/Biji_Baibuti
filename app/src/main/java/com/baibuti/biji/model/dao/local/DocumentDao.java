package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.po.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao implements IDocumentDao {

    private final static String TBL_NAME = "tbl_document";

    private final static String COL_ID = "doc_id";
    private final static String COL_PATH = "doc_path";
    private final static String COL_CLASS_NAME = "doc_class_name";
    private final static String COL_NAME = "doc_name";

    private DbOpenHelper helper;

    public DocumentDao(Context context) {
        helper = new DbOpenHelper(context);
    }

    /**
     * 查询所有文件
     * @return 文件列表
     */
    @Override
    public List<Document> queryAllDocuments() {
        return queryDocumentsByClassName(null);
    }

    /**
     * 根据 ClassName 查询所有文件
     * @param className 分类，null for all
     * @return 文件分类列表
     */
    @Override
    public List<Document> queryDocumentsByClassName(String className) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME +
            ((className == null) ? "" : " where " + COL_CLASS_NAME + " = " + className);

        List<Document> documentList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String path = cursor.getString(cursor.getColumnIndex(COL_PATH));
                String queryClassName = cursor.getString(cursor.getColumnIndex(COL_CLASS_NAME));
                String docName = cursor.getString(cursor.getColumnIndex(COL_NAME));

                documentList.add(new Document(id, path, queryClassName, docName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return documentList;
    }

    /**
     * 根据 id 查询文件
     * @param id 归档文件 id
     * @return 一个文档
     */
    @Override
    public Document queryDocumentById(int id) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + id;

        Document document = null;
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                String path = cursor.getString(cursor.getColumnIndex(COL_PATH));
                String className = cursor.getString(cursor.getColumnIndex(COL_CLASS_NAME));
                String docName = cursor.getString(cursor.getColumnIndex(COL_NAME));

                document = new Document(id, path, className, docName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return document;
    }

    /**
     * 插入归档文件
     * @param document 新归档，自动编号
     * @return 归档 id
     */
    @Override
    public long insertDocument(Document document) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql =
            "insert into " + TBL_NAME +
            "(" + COL_PATH + ", " + COL_CLASS_NAME + ", " + COL_NAME + ") " +
            "values (?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            stat.bindString(1, document.getFilePath() == null ? "" : document.getFilePath()); // COL_PATH
            stat.bindString(2, document.getClassName()); // COL_CLASS_NAME
            stat.bindString(3, document.getDocName()); // COL_NAME

            ret_id = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            db.endTransaction();
            db.close();
        }

        return ret_id;
    }

    /**
     * 修改归档文件信息
     * @param document 覆盖更新信息
     * @return 是否成功更新
     */
    @Override
    public boolean updateDocument(Document document) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, document.getDocName());
        values.put(COL_CLASS_NAME, document.getClassName());
        values.put(COL_PATH, document.getFilePath());

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?",
            new String[] { String.valueOf(document.getId()) } );
        db.close();

        return ret != 0;
    }

    /**
     * 删除资料
     * @param id 删除的资料 id
     * @return 是否删除成功
     */
    @Override
    public boolean deleteDocument(int id) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?",
                new String[]{ String.valueOf(id) });
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

