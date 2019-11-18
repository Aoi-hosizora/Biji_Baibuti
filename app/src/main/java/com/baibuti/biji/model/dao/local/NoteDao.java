package com.baibuti.biji.model.dao.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.model.dao.DbOpenHelper;
import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteDao implements INoteDao {

    private final static String TBL_NAME = "tbl_note";

    private final static String COL_ID = "n_id";
    private final static String COL_TITLE = "n_title";
    private final static String COL_CONTENT = "n_content";
    private final static String COL_GROUP_ID = "n_group_id";
    private final static String COL_CREATE_TIME = "n_create_time";
    private final static String COL_UPDATE_TIME = "n_update_time";

    private DbOpenHelper helper;
    private GroupDao groupDao;

    public NoteDao(Context context) {
        helper = new DbOpenHelper(context);
        groupDao = new GroupDao(context);

        // 是否为空
        // if (queryAllNotes().isEmpty())
        //     insertNote(Note.DEF_NOTE);
    }

    public static void create_tbl(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
            COL_ID          + " integer PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE       + " varchar NOT NULL, " +
            COL_CONTENT     + " varchar, " +
            COL_GROUP_ID    + " integer NOT NULL, " +
            COL_CREATE_TIME + " datetime NOT NULL, " +
            COL_UPDATE_TIME + " datetime NOT NULL)"
        );
    }

    /**
     * 查询所有笔记
     * @return 笔记列表
     */
    @Override
    public List<Note> queryAllNotes() {
        return queryNotesByGroupId(-1);
    }

    /**
     * 根据分组查询所有笔记
     * @param groupId -1 for all
     */
    @Override
    public List<Note> queryNotesByGroupId(int groupId) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + ((groupId == -1) ? "" : " where " + COL_GROUP_ID + " = " + groupId);

        List<Note> noteList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                Group group = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex(COL_GROUP_ID)));
                if (group == null)
                    group = groupDao.queryDefaultGroup();

                Date ct = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_CREATE_TIME)));
                Date ut = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME)));

                noteList.add(new Note(id, title, content, group, ct, ut));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return noteList;
    }

    /**
     * 根据 nid 查询笔记
     * @param noteId 笔记 id
     */
    @Override
    public Note queryNoteById(int noteId) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + noteId;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));

                Group group = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex(COL_GROUP_ID)));
                if (group == null)
                    group = groupDao.queryDefaultGroup();

                Date ct = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_CREATE_TIME)));
                Date ut = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME)));

                return new Note(id, title, content, group, ct, ut);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return null;
    }

    /**
     * 插入笔记
     * @param note 新笔记，自动编号
     * @return 笔记 id
     */
    public long insertNote(Note note) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql =
                "insert into " + TBL_NAME +
                "(" + COL_TITLE + ", " + COL_CONTENT + ", " + COL_GROUP_ID + ", " + COL_CREATE_TIME + ", " + COL_UPDATE_TIME + ")" +
                "values (?, ?, ?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            stat.bindString(1, note.getTitle()); // COL_TITLE
            stat.bindString(2, note.getContent()); // COL_TITLE
            stat.bindLong(3, note.getGroup().getId()); // COL_GROUP_ID
            stat.bindString(4, DateColorUtil.Date2Str(note.getCreateTime())); // COL_CREATE_TIME
            stat.bindString(5, DateColorUtil.Date2Str(note.getUpdateTime())); // COL_UPDATE_TIME

            ret_id = stat.executeInsert();
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
            db.close();
        }

        return ret_id;
    }

    /**
     * 更新笔记
     * @param note 覆盖更新
     * @return 是否成功更新
     */
    @Override
    public boolean updateNote(Note note) {

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TITLE, note.getTitle());
        values.put(COL_CONTENT, note.getContent());
        values.put(COL_GROUP_ID, note.getGroup().getId());
        values.put(COL_UPDATE_TIME, DateColorUtil.Date2Str(note.getUpdateTime()));

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?", new String[] { String.valueOf(note.getId()) });
        db.close();

        return ret > 0;
    }

    /**
     * 删除笔记
     * @param id 删除笔记的 id
     * @return 是否成功删除
     */
    @Override
    public boolean deleteNote(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen()) db.close();
        }

        return ret > 0;
    }

    @Override
    public int deleteNotes(int[] ids) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] id_str = new String[ids.length];
        for (int i = 0; i < id_str.length; i++)
            id_str[i] = String.valueOf(ids[i]);

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?", id_str);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen()) db.close();
        }

        return ret;
    }
}
