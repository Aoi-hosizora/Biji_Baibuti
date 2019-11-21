package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbManager;
import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocClassDao;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.po.Document;

import java.util.ArrayList;
import java.util.List;

public class DocClassDao implements IDocClassDao {

    private static final String TBL_NAME = "tbl_file_class";

    private static final String COL_ID = "f_id";
    private static final String COL_NAME = "f_name";

    private Context context;
    private DbManager dbMgr;

    public DocClassDao(Context context) {
        this.context = context;
        this.dbMgr = DbManager.getInstance(new DbOpenHelper(context));

        if (queryAllDocClasses().isEmpty())
            insertDocClass(new DocClass());
    }

    public static void create_tbl(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
            COL_ID      + " integer PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME    + " varchar NOT NULL UNIQUE)"
        );
    }

    /**
     * 查询所有分类
     * @return 文件分类列表
     */
    @Override
    public List<DocClass> queryAllDocClasses() {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
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
            dbMgr.closeDatabase();
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

        SQLiteDatabase db = dbMgr.getReadableDatabase();
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
            dbMgr.closeDatabase();
        }

        return null;
    }

    @Override
    public DocClass queryDocClassByName(String name) {
        SQLiteDatabase db = dbMgr.getReadableDatabase();
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
            dbMgr.closeDatabase();
        }

        return null;
    }

    /**
     * 查询默认分类
     * @return 返回数据库中的默认分类
     */
    @Override
    public DocClass queryDefaultDocClass() {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
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
            dbMgr.closeDatabase();
        }

        return docClass;
    }

    /**
     * 添加分组
     * @param docClass 新分组
     * @return SUCCESS | FAILED | DUPLICATED
     */
    @Override
    public DbStatusType insertDocClass(DocClass docClass) {

        if (queryDocClassByName(docClass.getName()) != null)
            return DbStatusType.DUPLICATED;

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            "(" + COL_NAME + ") values(?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        try {
            stat.bindString(1, docClass.getName()); // COL_NAME

            long ret_id = stat.executeInsert();
            if (ret_id != -1) {
                docClass.setId((int) ret_id);
                db.setTransactionSuccessful();
                return DbStatusType.SUCCESS;
            } else
                return DbStatusType.FAILED;
        } catch (SQLException e) {
            e.printStackTrace();
            return DbStatusType.FAILED;
        } finally {
            db.endTransaction();
            dbMgr.closeDatabase();
        }
    }

    /**
     * 更新分类
     * @param docClass 覆盖更新
     * @return SUCCESS | FAILED | DUPLICATED | DEFAULT
     */
    @Override
    public DbStatusType updateDocClass(DocClass docClass) {

        DocClass sameName = queryDocClassByName(docClass.getName());
        if (sameName != null && sameName.getId() != docClass.getId())
            return DbStatusType.DUPLICATED;

        DocClass def = queryDefaultDocClass();
        if (def.getId() == docClass.getId() && !def.getName().equals(docClass.getName()))
            return DbStatusType.DEFAULT;

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, docClass.getName());

        int ret = db.update(TBL_NAME, values,
            COL_ID + " = ?", new String[] { String.valueOf(docClass.getId()) });
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }

    /**
     * 删除分类
     * @param id 删除的分类 id
     * @param isToDefault 关联分组转移到默认 | 一块删除
     * @return SUCCESS | FAILED | DEFAULT
     */
    @Override
    public DbStatusType deleteDocClass(int id, boolean isToDefault) {

        DocClass defDocclass = queryDefaultDocClass();
        if (defDocclass.getId() == id)
            return DbStatusType.DEFAULT;

        DocumentDao documentDao = new DocumentDao(context);
        List<Document> documents = documentDao.queryDocumentByClassId(id);

        try (SQLiteDatabase db = dbMgr.getWritableDatabase()) {

            if (documents.isEmpty()) {
                int ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
                return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
            } else {
                db.beginTransaction();

                if (isToDefault) {
                    ContentValues values = new ContentValues();
                    values.put(DocumentDao.COL_DOCCLASS_ID, queryDefaultDocClass().getId());
                    int count = db.update(DocumentDao.TBL_NAME, values, DocumentDao.COL_DOCCLASS_ID + " = ?", new String[] { String.valueOf(id) });
                    if (count != documents.size()) {
                        db.endTransaction();
                        return DbStatusType.FAILED;
                    }
                } else {
                    int count = db.delete(DocumentDao.TBL_NAME, DocumentDao.COL_DOCCLASS_ID + " = ?", new String[] { String.valueOf(id) });
                    if (count != documents.size()) {
                        db.endTransaction();
                        return DbStatusType.FAILED;
                    }
                }

                int count = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
                if (count <= 0) {
                    db.endTransaction();
                    return DbStatusType.FAILED;
                } else {
                    db.setTransactionSuccessful();
                    return DbStatusType.SUCCESS;
                }
            }
        } finally {
            dbMgr.closeDatabase();
        }
    }
}
