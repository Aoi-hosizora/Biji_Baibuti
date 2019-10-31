package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.po.FileClass;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.module.file.FileClassUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class FileClassDao {
    private DbOpenHelper helper;
    private Context context;

    public FileClassDao(Context context) {
        this(context, (AuthManager.getInstance().isLogin()) ? AuthManager.getInstance().getUserName() : "");
    }

    @Deprecated
    public FileClassDao(Context context, String Username) {
        helper = new DbOpenHelper(context, Username);
        this.context = context;
    }

    /**
     * 更新文件分类日志，可能存在冗杂
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_FileClass);
    }

    public List<FileClass> queryFileClassAll() {
        return queryFileClassAll(true);
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<FileClass> queryFileClassAll(boolean isLogCheck) { // ArrayList

        if (isLogCheck) pushpull();

        List<FileClass> fileClassList = selectAllClasses();

        if (fileClassList.isEmpty())
            fileClassList.add(insertDefaultClasses());

        return fileClassList;
    }

    public void pushpull() {
        if (AuthManager.getInstance().isLogin()) {

            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_FileClass)) { // 本地新
                // TODO 异步
                Log.e("测试", "fileclass本地新");
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_FileClass);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_FileClass)) { // 服务器新
                // TODO 同步
                Log.e("测试", "fileclass服务器新");
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_FileClass);
            }
        }
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
        return queryFileClassByName(fileClassName, true);
    }

    @Deprecated
    public FileClass queryFileClassByName(String fileClassName, boolean isLogCheck) {

        if (isLogCheck) pushpull();

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
        return queryFileClassById(fileClassId, true);
    }

    @Deprecated
    public FileClass queryFileClassById(int fileClassId, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        FileClass fileClass = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_file_class", null, "f_id=?", new String[]{fileClassId + ""}, null, null, null);
            while (cursor.moveToNext()) {
                int order = cursor.getInt(cursor.getColumnIndex("f_order"));

                String fileClassName = cursor.getString(cursor.getColumnIndex("f_name"));


                //生成一个文件分类

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

        fileClass = this.queryFileClassByName(FileClass.GetDefaultFileClassName, false);
        if (fileClass != null)
            return fileClass;

        return insertDefaultClasses();
    }

    private FileClass insertDefaultClasses() {
        FileClass def = new FileClass();
        def.setOrder(0);
        def.setFileClassName(FileClass.GetDefaultFileClassName);

        this.insertFileClass(def, def.getId());

        return this.queryFileClassByName(FileClass.GetDefaultFileClassName, false);
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

    public long insertFileClass(FileClass fileClass){

        Log.e("测试", "insertFileClass: " + fileClass.getId());

        pushpull();

        long ret = insertFileClass(fileClass, -1);

        fileClass.setId((int)ret);

        List<FileClass> fileClasses = queryFileClassAll();
        for(FileClass fileClass1: fileClasses){
            Log.e("测试", "insertFileClass: " +fileClass1.getId() + " " + fileClass1.getFileClassName());
        }

        if (AuthManager.getInstance().isLogin()) {
            try {
                if (FileClassUtil.insertFileClass(fileClass) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_FileClass);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 添加一个分类
     */
    public long insertFileClass(FileClass fileClass, int id) {
        HandleDuplicate(fileClass, null);

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql;

        if (id == -1)
            sql = "insert into db_file_class(f_name,f_order) values(?,?)";
        else
            sql = "insert into db_file_class(f_name,f_order,f_id) values(?,?,?)";

        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, fileClass.getFileClassName());
            stat.bindLong(2, fileClass.getOrder());

            if (id != -1)
                stat.bindLong(3, id); // id

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

        Log.e("测试", "insertFileClass: 调用");

        updateLog();

        return ret;
    }

    public void updateFileClass(FileClass fileClass){
        updateFileClass(fileClass, true);
    }

    /**
     * 更新一个分类
     */
    @Deprecated
    public void updateFileClass(FileClass fileClass, boolean isLogCheck) {

        FileClass oldFileClass = queryFileClassById(fileClass.getId(), isLogCheck);

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

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (FileClassUtil.updateFileClass(fileClass) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_FileClass);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        updateLog();
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

    public int deleteFileClass(int fileClassId) throws EditDefaultFileClassException {
        return deleteFileClass(fileClassId, true);
    }

    /**
     * 删除一个分类
     */
    @Deprecated
    public int deleteFileClass(int fileClassId, boolean isLogCheck) throws EditDefaultFileClassException {

        FileClass f = queryFileClassById(fileClassId, false);

        if (isLogCheck)
            if (checkDefaultFileClass(queryFileClassById(fileClassId)))
                throw new EditDefaultFileClassException();

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

        updateLog();

        if (isLogCheck && AuthManager.getInstance().isLogin()) {
            try {
                if (FileClassUtil.deleteFileClass(f) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_FileClass);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }
}

@Deprecated
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

