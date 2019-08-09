package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Note.NoteUtil;
import com.baibuti.biji.Utils.OtherUtils.DateColorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// 存储形式：
// n_id, n_title, n_content, n_group_id, n_createtime, n_updatetime

public class NoteDao {
    private MyOpenHelper helper;
    private GroupDao groupDao;
    private Context context;

    public NoteDao(Context context) {
        this(context, (AuthMgr.getInstance().isLogin()) ? AuthMgr.getInstance().getUserName() : "");
    }

    public NoteDao(Context context, String username) {
        helper = new MyOpenHelper(context, username);
        groupDao = new GroupDao(context, username);
        this.context = context;
    }

    /**
     * 更新笔记日志
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_Note);
    }

    /**
     * 进行 push pull
     */
    private void pushpull() {
        if (AuthMgr.getInstance().isLogin()) {
            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_Note)) { // 本地新
                // TODO 异步
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Note);
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Group);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_Note)) { // 服务器新
                // TODO 同步
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Group);
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Note);
            }
        }
    }

     /**
      * 查询所有笔记，同步
      * @return
      */
     public List<Note> queryAllNotes() {
         Log.e("", "queryAllNotes0" );
         return queryAllNotesFromGroupId(-1, true);
     }

     /**
      * 根据分组查询所有笔记，同步
      * @param groupId
      * @return
      */
     public List<Note> queryAllNotesFromGroupId(int groupId) {
         Log.e("", "queryAllNotesFromGroupId: 1 " + groupId);
         return queryAllNotesFromGroupId(groupId, true);
     }

    /**
     * 根据分组查询所有笔记，同步？
     * @param groupId -1 for all
     * @param isLogCheck
     * @return
     */
    List<Note> queryAllNotesFromGroupId(int groupId, boolean isLogCheck) { // ArrayList
        Log.e("", "queryAllNotesFromGroupId: 2 " + groupId + isLogCheck);
        if (isLogCheck) pushpull();

        // TODO !!!
        //  W/System.err: java.lang.NullPointerException:
        //  Attempt to invoke virtual method '
        //      android.database.sqlite.SQLiteDatabase android.content.Context.openOrCreateDatabase(
        //          java.lang.String, int, android.database.sqlite.SQLiteDatabase$CursorFactory, android.database.DatabaseErrorHandler
        //      )
        //  ' on a null object reference

        SQLiteDatabase db = helper.getWritableDatabase();

        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql ;
        Cursor cursor = null;
        try {
            if (groupId >= 0){
                sql = "select * from db_note where n_group_id = " + groupId;
            } else {
                sql = "select * from db_note" ;
            }


            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                note = new Note("","");

                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));

                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));

                Group grouptmp = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                if (grouptmp == null)
                    grouptmp = groupDao.queryDefaultGroup();

                note.setGroupLabel(grouptmp, false);

                note.setCreateTime(DateColorUtil.Str2Date(
                        cursor.getString(cursor.getColumnIndex("n_create_time"))
                ));

                note.setUpdateTime(DateColorUtil.Str2Date(
                        cursor.getString(cursor.getColumnIndex("n_update_time"))
                ));

                noteList.add(note);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    public Note queryNoteById(int noteId) {
        Log.e("", "queryNoteById: ");
        return queryNoteById(noteId, true);
    }

    /**
     * 查询 ID 笔记
     * @param noteId
     * @param isLogCheck
     * @return
     */
    private Note queryNoteById(int noteId, boolean isLogCheck) {
        Log.e("", "queryNoteById: ");
        // always false

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        Note note = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_note", null, "n_id=?", new String[]{noteId + ""}, null, null, null);
            while (cursor.moveToNext()) {

                String title = cursor.getString(cursor.getColumnIndex("n_title"));
                String content = cursor.getString(cursor.getColumnIndex("n_content"));

                Group grouptmp = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                if (grouptmp == null)
                    grouptmp = groupDao.queryDefaultGroup();

                Date createTime = DateColorUtil.Str2Date(
                        cursor.getString(cursor.getColumnIndex("n_create_time"))
                );

                Date updateTime = DateColorUtil.Str2Date(
                        cursor.getString(cursor.getColumnIndex("n_update_time"))
                );


                note = new Note(noteId, title, content, grouptmp, createTime, updateTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return note;
    }


    // public Note insertDefaultNote() {
    //     Note dft = new Note(Note.GetDefaultNoteName, "默认内容");
    //     dft.setGroupLabel(groupDao.queryDefaultGroup(), true);
    //     long id = this.insertNote(dft);
    //     return queryNoteById((int)id);
    // }

    /**
     * 插入笔记，更新编号，不同步
     * @param note
     * @param idx
     * @return
     */
    long insertNote(Note note, int idx) {

        Log.e("", "insertNote: ");
        // if (isLogCheck) -> False

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql;
        if (idx == -1)
            sql = "insert into db_note(n_title,n_content,n_group_id,n_create_time,n_update_time) values(?,?,?,?,?)";
        else
            sql = "insert into db_note(n_title,n_content,n_group_id,n_create_time,n_update_time,n_id) values(?,?,?,?,?,?)";

        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, note.getTitle()); // title
            stat.bindString(2, note.getContent()); // content
            stat.bindLong(3, note.getGroupLabel().getId()); // groupid

            stat.bindString(4, DateColorUtil.Date2Str((note.getCreateTime()==null)?new Date():note.getCreateTime())); // createtime
            stat.bindString(5, DateColorUtil.Date2Str((note.getUpdateTime()==null)?new Date():note.getUpdateTime())); // updatetime

            if (idx != -1)
                stat.bindLong(6, idx); // id

            ret = stat.executeInsert();
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }
        Log.e("insertNote", "insertNote: "+ ret);
        updateLog();

        // 不同步

        return ret;
    }

    /**
     * 插入笔记，自动编号，同步
     * @param note
     * @return
     */
    public long insertNote(Note note) {

        Log.e("", "insertNote: ");
        pushpull();

        long ret = insertNote(note, -1);

        // isLogCheck -> True
        if (AuthMgr.getInstance().isLogin()) {
            try {
                if (NoteUtil.insertNote(note) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Note);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 更新笔记，同步
     * @param note
     */
    public void updateNote(Note note) {
        Log.e("", "updateNote: ");
        updateNote(note, true);
    }

    /**
     * 更新笔记，同步？
     * @param note
     */
    void updateNote(Note note, boolean isLogCheck) {

        Log.e("", "updateNote: ");

        // TODO 提前被删除的处理

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("n_title", note.getTitle());
        values.put("n_content", note.getContent());

        values.put("n_group_id", note.getGroupLabel().getId());

        values.put("n_update_time", DateColorUtil.Date2Str(note.getUpdateTime()));

        db.update("db_note", values, "n_id=?", new String[]{note.getId()+""});
        db.close();
        updateLog();

        if (AuthMgr.getInstance().isLogin()) {
            try {
                if (NoteUtil.updateNote(note) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Note);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 删除笔记，同步
     * @param noteId
     * @return
     */
    public int deleteNote(int noteId) {
        Log.e("", "deleteNote: " + noteId);
        return deleteNote(noteId, true);
    }

    /**
     * 删除笔记，同步？
     * @param noteId
     * @param isLogCheck
     * @return
     */
    int deleteNote(int noteId, boolean isLogCheck) {
        Log.e("", "deleteNote: " + isLogCheck);

        Note note = queryNoteById(noteId, false);

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            ret = db.delete("db_note", "n_id=?", new String[]{noteId + ""});

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null)
                db.close();
        }
        updateLog();

        if (isLogCheck && AuthMgr.getInstance().isLogin()) {
            try {
                if (note != null)
                    if (NoteUtil.deleteNote(note) != null)
                        ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Note);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }
}
