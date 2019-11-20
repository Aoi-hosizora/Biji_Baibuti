package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DatabaseManager;
import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.po.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao implements IDocumentDao {

    final static String TBL_NAME = "tbl_document";

    private final static String COL_ID = "doc_id";
    private final static String COL_PATH = "doc_path";
    final static String COL_DOCCLASS_ID = "doc_class_name";

    private DatabaseManager dbMgr;
    private DocClassDao docClassDao;

    public DocumentDao(Context context) {
        this.dbMgr = DatabaseManager.getInstance(new DbOpenHelper(context));
        this.docClassDao = new DocClassDao(context);
    }

    public static void create_tbl(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
            COL_ID          + " integer PRIMARY KEY AUTOINCREMENT, " +
            COL_PATH        + " varchar NOT NULL, " +
            COL_DOCCLASS_ID + " varchar NOT NULL)"
        );
    }

    /**
     * 查询所有文件
     * @return 文件列表
     */
    @Override
    public List<Document> queryAllDocuments() {
        return queryDocumentByClassId(-1);
    }

    /**
     * 根据 ClassName 查询所有文件
     * @param cid 分类，-1 for all
     * @return 文件分类列表
     */
    @Override
    public List<Document> queryDocumentByClassId(int cid) {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME +
            ((cid == -1) ? "" : " where " + COL_DOCCLASS_ID + " = " + cid);

        List<Document> documentList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String path = cursor.getString(cursor.getColumnIndex(COL_PATH));
                int classId = cursor.getInt(cursor.getColumnIndex(COL_DOCCLASS_ID));

                DocClass docClass = docClassDao.queryDocClassById(classId);
                documentList.add(new Document(id, path, docClass));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
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

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + id;

        Document document = null;
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                String path = cursor.getString(cursor.getColumnIndex(COL_PATH));
                int classId = cursor.getInt(cursor.getColumnIndex(COL_DOCCLASS_ID));

                DocClass docClass = docClassDao.queryDocClassById(classId);
                document = new Document(id, path, docClass);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
        }
        return document;
    }

    /**
     * 插入归档文件
     * @param document 新归档，自动编号
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType insertDocument(Document document) {

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String sql =
            "insert into " + TBL_NAME +
            "(" + COL_PATH + ", " + COL_DOCCLASS_ID + ") values (?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        try {
            if (docClassDao.queryDocClassById(document.getDocClass().getId()) == null)
                document.setDocClass(docClassDao.queryDefaultDocClass());

            stat.bindString(1, document.getFilename() == null ? "" : document.getFilename()); // COL_PATH
            stat.bindLong(2, document.getDocClass().getId());

            long ret_id = stat.executeInsert();
            if (ret_id != -1) {
                document.setId((int) ret_id);
                db.setTransactionSuccessful();
                return DbStatusType.SUCCESS;
            } else
                return DbStatusType.FAILED;
        } catch (SQLException e) {
            e.printStackTrace();
            return DbStatusType.FAILED;
        }  finally {
            db.endTransaction();
            dbMgr.closeDatabase();
        }
    }

    /**
     * 修改归档文件信息
     * @param document 覆盖更新信息
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType updateDocument(Document document) {

        if (docClassDao.queryDocClassById(document.getDocClass().getId()) == null)
            document.setDocClass(docClassDao.queryDefaultDocClass());

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_DOCCLASS_ID, document.getDocClass().getId());
        values.put(COL_PATH, document.getFilename());

        int ret = db.update(TBL_NAME, values,
            COL_ID + " = ?", new String[] { String.valueOf(document.getId()) } );
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }

    /**
     * 删除资料
     * @param id 删除的资料 id
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType deleteDocument(int id) {
        SQLiteDatabase db = dbMgr.getWritableDatabase();

        int ret = db.delete(TBL_NAME,
            COL_ID + " = ?", new String[]{String.valueOf(id)});
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }
}
