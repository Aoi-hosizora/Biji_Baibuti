package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Models.FileClass;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class FileClassDao {
    private MyOpenHelper helper;
    private Context context;

    public FileClassDao(Context context) {
        helper = new MyOpenHelper(context);
        this.context = context;
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<FileClass> queryFileClassAll() { // ArrayList

        List<FileClass> fileClassList = selectAllClasses();

        if (fileClassList.isEmpty())
            fileClassList.add(insertDefaultClasses());

        return fileClassList;
    }

    // 内部 select
    private List<FileClass> selectAllClasses() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<FileClass> fileClassList = new ArrayList<FileClass>();

        FileClass fileClass ;
        Cursor cursor = null;
        try {
            cursor = db.query("db_file_class", null, null, null, null, null, "null");
            while (cursor.moveToNext()) {

                int fileClassId = cursor.getInt(cursor.getColumnIndex("f_id"));
                String fileClassName = cursor.getString(cursor.getColumnIndex("f_name"));
                int order = cursor.getInt(cursor.getColumnIndex("f_order"));

                //生成一个分类

                fileClass = new FileClass();
                fileClass.setId(fileClassId);
                fileClass.setFileClassName(fileClassName);
                fileClass.setOrder(order);

                fileClassList.add(fileClass);
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
        return fileClassList;
    }

    public FileClass queryFileClassByName(String fileClassName) {
        SQLiteDatabase db = helper.getWritableDatabase();

        FileClass fileClass = null;
        Cursor cursor = null;
        try {
            Log.i(TAG, "###queryGroupByName: "+fileClassName);
            cursor = db.query("db_file_class", null, "f_name=?", new String[]{fileClassName}, null, null, null);
            while (cursor.moveToNext()) {
                int fileClassId = cursor.getInt(cursor.getColumnIndex("f_id"));

                int order = cursor.getInt(cursor.getColumnIndex("f_order"));

                //生成一个分类

                fileClass = new FileClass();

                fileClass.setId(fileClassId);
                fileClass.setFileClassName(fileClassName);

                fileClass.setOrder(order);

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
        return fileClass;
    }

    public FileClass queryFileClassById(int fileClassId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        FileClass fileClass = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_file_class", null, "f_id=?", new String[]{fileClassId + ""}, null, null, null);
            while (cursor.moveToNext()) {
                int order = cursor.getInt(cursor.getColumnIndex("f_order"));

                String fileClassName = cursor.getString(cursor.getColumnIndex("f_name"));


                //生成一个订单

                fileClass = new FileClass();

                fileClass.setId(fileClassId);
                fileClass.setFileClassName(fileClassName);

                fileClass.setOrder(order);

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
        return fileClass;
    }


    public FileClass queryDefaultFileClass() {
        FileClass fileClass = null;

        fileClass = this.queryFileClassByName(FileClass.GetDefaultFileClassName);
        if (fileClass != null)
            return fileClass;

        return insertDefaultClasses();
    }

    private FileClass insertDefaultClasses() {
        FileClass def = new FileClass();
        def.setOrder(0);
        def.setFileClassName(FileClass.GetDefaultFileClassName);

        this.insertFileClass(def);

        return this.queryFileClassByName(FileClass.GetDefaultFileClassName);
    }

    /**
     * 检查是否有重复
     */
    public int checkDuplicate(FileClass fileClass, FileClass oldFileClass) {
        List<FileClass> tmp = selectAllClasses();
        int cnt=0;
        if (oldFileClass==null)
            oldFileClass = new FileClass();
        for (FileClass f : tmp)
            if (f.getFileClassName().equals(fileClass.getFileClassName()) && !f.getFileClassName().equals(oldFileClass.getFileClassName()))
                cnt++;
        return cnt;
    }

    /**
     * 处理重复插入
     */
    private FileClass HandleDuplicate(FileClass fileClass, FileClass oldFileClass) {
        int cnt = checkDuplicate(fileClass, oldFileClass);
        if (cnt!=0)
            fileClass.setFileClassName(fileClass.getFileClassName() + " (" + cnt + ")");

        return fileClass;
    }

    /**
     * 添加一个分类
     */
    public long insertFileClass(FileClass fileClass) {
        HandleDuplicate(fileClass, null);

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into db_file_class(f_name,f_order) values(?,?)";
        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, fileClass.getFileClassName());
            stat.bindLong(2, fileClass.getOrder());

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
     * 更新一个分类
     */
    public void updateFileClass(FileClass fileClass) {

        FileClass oldFileClass = queryFileClassById(fileClass.getId());

        HandleDuplicate(fileClass, oldFileClass);

        //////////////////////////////////////////////////

        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put("f_name", fileClass.getFileClassName());

            values.put("f_order", fileClass.getOrder());

            db.update("db_file_class", values, "f_id=?", new String[]{fileClass.getId() + ""});
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 检查是否为默认分组
     */
    private boolean checkDefaultFileClass(FileClass fileClass) {
        if (fileClass==null) {
            Log.e("checkDefaultFileClass", "checkDefaultFileCLass: fileClass==null");
        }
        else
        if (FileClass.GetDefaultFileClassName.equals(fileClass.getFileClassName())) {
            return true;
        }
        return false;
    }

    /**
     * 删除一个分类
     */
    public int deleteFileClass(int fileClassId) throws EditDefaultFileClassException {

        //////////////////////////////////////////////////

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_file_class", "f_id=?", new String[]{fileClassId + ""});
        }

        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        if (selectAllClasses().isEmpty())
            queryDefaultFileClass();

        return ret;
    }
}

class EditDefaultFileClassException extends Exception {

    public EditDefaultFileClassException() {
        super();
    }

    public EditDefaultFileClassException(String message) {
        super(message);
    }

    public EditDefaultFileClassException(String message, Throwable cause) {
        super(message,cause);
    }

    public EditDefaultFileClassException(Throwable cause) {

        super(cause);
    }
}

