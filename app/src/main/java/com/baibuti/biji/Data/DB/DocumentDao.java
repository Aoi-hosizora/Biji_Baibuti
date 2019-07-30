package com.baibuti.biji.Data.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.Data.Models.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao {
    private MyOpenHelper helper;
    private Context context;

    public DocumentDao(Context context) {
        helper = new MyOpenHelper(context);
        this.context = context;
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<Document> queryDocumentAll(String documentClassName) { // ArrayList
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

                //生成一个文件列表子项
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
                document.setDocumentClassName(documentClassName);
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

    public Document queryDocumentByPath(String documentPath) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Document document = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, "doc_path=?", new String[]{documentPath}, null, null, null);
            while (cursor.moveToNext()) {
                int documentId = cursor.getInt(cursor.getColumnIndex("doc_id"));

                //生成一个文件
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
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
        return document;
    }

    public Document queryDocumentById(int documentId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Document document = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_document", null, "doc_id=?", new String[]{documentId + ""}, null, null, null);
            while (cursor.moveToNext()) {
                String documentPath = cursor.getString(cursor.getColumnIndex("doc_path"));
                //生成一个文件
                document = new Document();
                document.setId(documentId);
                document.setDocumentPath(documentPath);
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
        return document;
    }

    /**
     * 添加一个文件
     */
    public long insertDocument(Document document) {

        //列表中已存在该文件
        List<Document> tmp = selectAllDocuments(document.getDocumentClassName());
        for (Document f : tmp)
            if (f.getDocumentPath().equals(document.getDocumentPath()))
                return 0;

        //新文件
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into db_document(doc_path, doc_class_name) values(?, ?)";
        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, document.getDocumentPath());
            stat.bindString(2, document.getDocumentClassName());

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
     * 删除资料
     */
    public int deleteDocument(int documentId) {

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_document", "doc_id=?", new String[]{documentId + ""});
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
    public int deleteDocumentByPath(String documentPath) {

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

        return ret;
    }

    /**
     * 删除整个分类
     * @param documentClassName
     * @return
     */
    public int deleteDocumentByClass(String documentClassName){

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

        return ret;
    }
}

