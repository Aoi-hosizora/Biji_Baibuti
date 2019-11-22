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
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteDao {

    final static String TBL_NAME = "tbl_note";

    private final static String COL_ID = "n_id";
    private final static String COL_TITLE = "n_title";
    private final static String COL_CONTENT = "n_content";
    final static String COL_GROUP_ID = "n_group_id";
    private final static String COL_CREATE_TIME = "n_create_time";
    private final static String COL_UPDATE_TIME = "n_update_time";

    private GroupDao groupDao;
    private DbManager dbMgr;

    public NoteDao(Context context) {
        this.dbMgr = DbManager.getInstance(new DbOpenHelper(context));
        this.groupDao = new GroupDao(context);
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
    public List<Note> queryAllNotes() {
        return queryNotesByGroupId(-1);
    }

    /**
     * 根据分组查询所有笔记
     * @param groupId -1 for all
     */
    public List<Note> queryNotesByGroupId(int groupId) {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + ((groupId == -1) ? "" : " where " + COL_GROUP_ID + " = " + groupId);

        List<Note> noteList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(COL_CONTENT));
                Date ct = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_CREATE_TIME)));
                Date ut = DateColorUtil.Str2Date(cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME)));

                Group group = groupDao.queryGroupById(cursor.getInt(cursor.getColumnIndex(COL_GROUP_ID)));
                if (group == null)
                    group = groupDao.queryDefaultGroup();

                noteList.add(new Note(id, title, content, group, ct, ut));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
        }

        return noteList;
    }

    /**
     * 根据 nid 查询笔记
     * @param noteId 笔记 id
     */
    public Note queryNoteById(int noteId) {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
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
            dbMgr.closeDatabase();
        }
        return null;
    }

    /**
     * 插入笔记
     * @param note 新笔记，自动编号
     * @return SUCCESS | FAILED
     */
    public DbStatusType insertNote(Note note) {

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String sql =
            "insert into " + TBL_NAME +
                "(" + COL_TITLE + ", " + COL_CONTENT + ", " + COL_GROUP_ID + ", "
                + COL_CREATE_TIME + ", " + COL_UPDATE_TIME + ")" +
                "values (?, ?, ?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        try {
            if (groupDao.queryGroupById(note.getGroup().getId()) == null)
                note.setGroup(groupDao.queryDefaultGroup());

            stat.bindString(1, note.getTitle()); // COL_TITLE
            stat.bindString(2, note.getContent()); // COL_TITLE
            stat.bindLong(3, note.getGroup().getId()); // COL_GROUP_ID
            stat.bindString(4, DateColorUtil.Date2Str(note.getCreateTime())); // COL_CREATE_TIME
            stat.bindString(5, DateColorUtil.Date2Str(note.getUpdateTime())); // COL_UPDATE_TIME

            long ret_id = stat.executeInsert();
            if (ret_id != -1) {
                note.setId((int) ret_id);
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
     * 更新笔记
     * @param note 覆盖更新
     * @return SUCCESS | FAILED
     */
    public DbStatusType updateNote(Note note) {

        if (groupDao.queryGroupById(note.getGroup().getId()) == null)
            note.setGroup(groupDao.queryDefaultGroup());

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TITLE, note.getTitle());
        values.put(COL_CONTENT, note.getContent());
        values.put(COL_GROUP_ID, note.getGroup().getId());
        values.put(COL_UPDATE_TIME, DateColorUtil.Date2Str(note.getUpdateTime()));

        int ret = db.update(TBL_NAME, values,
            COL_ID + " = ?", new String[] { String.valueOf(note.getId()) });
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }

    /**
     * 删除笔记
     * @param id 删除笔记的 id
     * @return SUCCESS | FAILED
     */
    public DbStatusType deleteNote(int id) {
        SQLiteDatabase db = dbMgr.getWritableDatabase();

        int ret = db.delete(TBL_NAME,
            COL_ID + " = ?", new String[] { String.valueOf(id) });
        dbMgr.closeDatabase();
        return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
    }

    /**
     * 删除多条笔记
     * @return 删除的数量
     */
    public int deleteNotes(int[] ids) {
        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String[] id_str = new String[ids.length];
        for (int i = 0; i < id_str.length; i++)
            id_str[i] = String.valueOf(ids[i]);

        int ret = db.delete(TBL_NAME, COL_ID + " in (?)", id_str);
        dbMgr.closeDatabase();
        return ret;
    }
}
