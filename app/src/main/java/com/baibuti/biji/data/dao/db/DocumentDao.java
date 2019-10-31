package com.baibuti.biji.data.dao.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.data.dao.DbOpenHelper;
import com.baibuti.biji.data.model.Document;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.module.file.DocumentUtil;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao {

    private DbOpenHelper helper;
    private Context context;

    public DocumentDao(Context context) {
        this(context, (AuthMgr.getInstance().isLogin()) ? AuthMgr.getInstance().getUserName() : "");
    }

    @Deprecated
    public DocumentDao(Context context, String Username) {
        helper = new DbOpenHelper(context, Username);
        this.context = context;
    }

    /**
     * 根据id查询
     * @param id
     * @param isLogCheck
     * @return
     */
    @Deprecated
    private Document queryDocumentById(int id, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        Document document = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, "doc_id=?", new String[]{id + ""}, null, null, null);
            while (cursor.moveToNext()) {

                int documentId = cursor.getInt(cursor.getColumnIndex("doc_id"));
                String documentPath = cursor.getString(cursor.getColumnIndex("doc_path"));
                String documentClassName = cursor.getString(cursor.getColumnIndex("doc_class_name"));

                //生成一个文件列表子项
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
                document.setDocumentClassName(documentClassName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return document;
    }

    private Document queryDocumentByDocumentPath(String documentName, String documentPath, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        Document document = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, "doc_path=?", new String[]{documentPath}, null, null, null);
            while (cursor.moveToNext()) {

                int documentId = cursor.getInt(cursor.getColumnIndex("doc_id"));
                String documentPath1 = cursor.getString(cursor.getColumnIndex("doc_path"));
                String documentClassName = cursor.getString(cursor.getColumnIndex("doc_class_name"));

                //生成一个文件列表子项
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath1);
                document.setDocumentClassName(documentClassName);
                document.setDocumentName(documentName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return document;
    }

    /**
     * 查询所有文件
     * @return
     */
    public List<Document> queryDocumentAll() { // ArrayList
        return selectAllDocuments();
    }

    // 内部 select
    private List<Document> selectAllDocuments() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Document> documentList = new ArrayList<Document>();

        Document document ;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, null, null, null, null, "null");
            while (cursor.moveToNext()) {

                int documentId = cursor.getInt(cursor.getColumnIndex("doc_id"));
                String documentPath = cursor.getString(cursor.getColumnIndex("doc_path"));
                String documentClassName = cursor.getString(cursor.getColumnIndex("doc_class_name"));
                String documentName = cursor.getString(cursor.getColumnIndex("doc_name"));

                //生成一个文件列表子项
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
                document.setDocumentClassName(documentClassName);
                document.setDocumentName(documentName);
                documentList.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return documentList;
    }

    /**
     * 进行 push pull
     */
    public void pushpull() {
        if (AuthMgr.getInstance().isLogin()) {
            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_Document)) { // 本地新
                // TODO 异步
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Document);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_Document)) { // 服务器新
                // TODO 同步
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Document);
            }
        }
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<Document> queryDocumentAll(String documentClassName) { // ArrayList
         return queryDocumentAll(documentClassName, true);
    }

    @Deprecated
    public List<Document> queryDocumentAll(String documentClassName, boolean isLogCheck) { // ArrayList
        if (isLogCheck) pushpull();
        return selectAllDocuments(documentClassName);
    }

    // 内部 select
    private List<Document> selectAllDocuments(String documentClassName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Document> documentList = new ArrayList<Document>();

        Document document ;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, "doc_class_name=?", new String[]{documentClassName}, null, null, "null");
            while (cursor.moveToNext()) {

                int documentId = cursor.getInt(cursor.getColumnIndex("doc_id"));
                String documentPath = cursor.getString(cursor.getColumnIndex("doc_path"));
                String documentName = cursor.getString(cursor.getColumnIndex("doc_name"));

                //生成一个文件列表子项
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
                document.setDocumentClassName(documentClassName);
                document.setDocumentName(documentName);
                documentList.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("测试", "selectAllDocuments: "+e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return documentList;
    }

    /**
     * 添加一个文件
     */
    public long insertDocument(Document document) {

        pushpull();
        long ret = insertDocument(document, -1);

        document.setId((int)ret);

        if (AuthMgr.getInstance().isLogin()) {
            try {
                DocumentUtil.postFile(document, new IPushCallBack() {
                    @Override
                    public void onCallBack() {
                        ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Document);
                    }
                });
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 添加一个文件, 带id
     */
    public long insertDocument(Document document, int id) {

        //新文件
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql;
        if(id == -1)
            sql = "insert into db_document(doc_path, doc_class_name, doc_name) values(?, ?, ?)";
        else
            sql = "insert into db_document(doc_path, doc_class_name, doc_name, doc_id) values(?, ?, ?, ?)";
        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, document.getDocumentPath()==null?"":document.getDocumentPath());
            stat.bindString(2, document.getDocumentClassName());
            stat.bindString(3, document.getDocumentName());

            if(id != -1)
                stat.bindLong(4, document.getId());

            ret = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return ret;
    }

    /**
     * 下载文件时，更新本地路径为下载后的路径
     * @param document
     */
    public void updateDocumentPath(Document document){

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("doc_path", document.getDocumentPath());

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

        if (isLogCheck && AuthMgr.getInstance().isLogin()) {
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

        if (isLogCheck && AuthMgr.getInstance().isLogin()) {
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

