package com.baibuti.biji.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.util.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// 存储形式：
// n_id, n_title, n_content, n_group_id, n_createtime, n_updatetime

public class NoteDao {
    private MyOpenHelper helper;
    private GroupDao groupDao;

    public NoteDao(Context context) {
        helper = new MyOpenHelper(context);
        groupDao = new GroupDao(context);
    }

    /**
     * 查询所有笔记
     */
    public List<Note> queryNotesAll(int groupId) { // ArrayList
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Note> noteList = new ArrayList<>();
        Note note ;
        String sql ;
        Cursor cursor = null;
        try {
            if (groupId >= 0){
                sql = "select * from db_note where n_group_id = " + groupId;
            } else {
                sql = "select * from db_note " ;
            }


            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");

            while (cursor.moveToNext()) {

                note = new Note("","");

                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));

                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));

                Group grouptmp = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                if (grouptmp == null)
                    grouptmp = groupDao.queryDefaultGroup();

//                Group grouptmp = new Group();
//                grouptmp.setId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
//                grouptmp.setName(cursor.getString(cursor.getColumnIndex("n_group_name")));
                note.setGroupLabel(grouptmp, false);

                note.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
                        cursor.getString(cursor.getColumnIndex("n_create_time"))
                ));

                note.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
                        cursor.getString(cursor.getColumnIndex("n_update_time"))
                ));


                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    public List<Note> queryNotesAll() { // ArrayList
        List<Note> Re = queryNotesAll(-1);
        if (Re.isEmpty())
            Re.add(insertDefaultNote());
        return Re;
    }

    public Note queryNoteById(int noteId) {
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

                Date createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
                        cursor.getString(cursor.getColumnIndex("n_create_time"))
                );

                Date updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
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
            if (db != null) {
                db.close();
            }
        }
        return note;
    }


    public Note insertDefaultNote() {
        Note dft = new Note(Note.GetDefaultNoteName, "");
        dft.setGroupLabel(groupDao.queryDefaultGroup(), true);
        long id = this.insertNote(dft);
        return queryNoteById((int)id);
    }

    /**
     * 插入笔记
     */
    public long insertNote(Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into db_note(n_title,n_content,n_group_id,n_create_time,n_update_time) values(?,?,?,?,?)";

        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, note.getTitle()); // title
            stat.bindString(2, note.getContent()); // content



            stat.bindLong(3, note.getGroupLabel().getId()); // groupid
            // stat.bindString(4, note.getGroupLabel().getName()); // groupname

            stat.bindString(4, CommonUtil.date2string((note.getCreateTime()==null)?new Date():note.getCreateTime())); // createtime
             stat.bindString(5, CommonUtil.date2string((note.getUpdateTime()==null)?new Date():note.getUpdateTime())); // updatetime

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
        Log.e("insertNote", "insertNote: "+ ret);
        return ret;
    }

    /**
     * 更新笔记
     * @param note
     */
    public void updateNote(Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("n_title", note.getTitle());
        values.put("n_content", note.getContent());

        values.put("n_group_id", note.getGroupLabel().getId());

        values.put("n_update_time", CommonUtil.date2string(new Date()));

        db.update("db_note", values, "n_id=?", new String[]{note.getId()+""});
        db.close();
    }

    /**
     * 删除笔记
     */
    public int deleteNote(int noteId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            ret = db.delete("db_note", "n_id=?", new String[]{noteId + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }

    /**
     * 批量删除笔记
     * @param mNotes
     */
    public int deleteNote(List<Note> mNotes) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            if (mNotes != null && mNotes.size() > 0) {
                db.beginTransaction();//开始事务
                try {
                    for (Note note : mNotes) {
                        ret += db.delete("db_note", "n_id=?", new String[]{note.getId() + ""});
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }
}
