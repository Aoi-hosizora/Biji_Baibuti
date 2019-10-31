package com.baibuti.biji.model.dao.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.module.file.DocumentUtil;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao {

    private final static String TBL_NAME = "db_document";

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
    public List<Document> queryAllDocuments() {
        return queryDocumentsByClassName(null);
    }

    /**
     * 根据 ClassName 查询所有文件
     * @param className 分类，null for all
     * @return 文件分类列表
     */
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
     * 根据 path 查询文件
     * @param path 归档文件 id
     * @return 一个文档
     */
    private Document queryDocumentByDocumentPath(String path) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_PATH + " = " + path;

        Document document = null;
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
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
            stat.bindString(1, document.getPath() == null ? "" : document.getPath()); // COL_PATH
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
     * 下载文件时，更新本地路径为下载后的路径
     * @param document
     */
    public void updateDocumentPath(Document document){

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("doc_path", document.getPath());

        db.update("db_document", values, "doc_id=?", new String[]{document.getId()+""});
        db.close();
        updateLog();
    }

    /**
     * 删除资料
     */
    public int deleteDocument() {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_document", null, null);
        }

        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return ret;
    }

    /**
     * 删除资料
     */
    @Deprecated
    public int deleteDocumentByPath(String documentName, String documentPath, boolean isLogCheck) {

        Document document = queryDocumentByDocumentPath(documentName, documentPath, false);

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_document", "doc_path=?", new String[]{documentPath});
        }

        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        updateLog();

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (document != null)
                    if (DocumentUtil.deleteFile(document))
                        ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Document);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 删除整个分类
     * @param documentClassName
     * @return
     */
    @Deprecated
    public int deleteDocumentByClass(String documentClassName, boolean isLogCheck){

        Log.e("", "deleteDocumentByClass: " + isLogCheck);

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_document", "doc_class_name=?", new String[]{documentClassName});
        }

        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

        updateLog();

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (DocumentUtil.deleteFiles(documentClassName))
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Document);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 更新文件日志
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_Document);
    }
}

