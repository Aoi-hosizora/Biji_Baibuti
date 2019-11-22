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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDao {

    private final static String TBL_NAME = "tbl_group";

    private final static String COL_ID = "g_id";
    private final static String COL_NAME = "g_name";
    private final static String COL_ORDER = "g_order";
    private final static String COL_COLOR = "g_color";

    private Context context;
    private DbManager dbMgr;

    public GroupDao(Context context) {
        this.context = context;
        this.dbMgr = DbManager.getInstance(new DbOpenHelper(context));

        if (queryAllGroups().isEmpty())
            insertGroup(Group.DEF_GROUP);
        precessOrder();
    }

    public static void create_tbl(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
            COL_ID      + " integer PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME    + " varchar NOT NULL UNIQUE, " +
            COL_ORDER   + " integer NOT NULL, " +
            COL_COLOR   + " varchar NOT NULL)"
        );
    }

    /**
     * 查询所有分组
     * @return 分组列表
     */
    public List<Group> queryAllGroups() {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME;

        List<Group> groupList = new ArrayList<>();
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));
                String color = cursor.getString(cursor.getColumnIndex(COL_COLOR));

                groupList.add(new Group(id, name, order, color));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            dbMgr.closeDatabase();
        }
        return groupList;
    }

    /**
     * 按照分组 gid 查询
     * @param groupId 分组 id
     * @return 指定分组
     */
    public Group queryGroupById(int groupId) {

        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + groupId;

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));
                String color = cursor.getString(cursor.getColumnIndex(COL_COLOR));

                return new Group(id, name, order, color);
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
     * 根据分组名查询
     * @param groupName 分组名
     * @return 指定分组
     */
    public Group queryGroupByName(String groupName) {
        SQLiteDatabase db = dbMgr.getReadableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_NAME + " = \"" + groupName + "\"";

        try {
            cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));
                String color = cursor.getString(cursor.getColumnIndex(COL_COLOR));

                return new Group(id, name, order, color);
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
     * 查询默认分组
     * @return 返回数据库中的默认分组
    if (queryGroupBy
     */
    public Group queryDefaultGroup() {
        return queryGroupByName(Group.DEF_GROUP.getName());
    }

    /**
     * 添加分组
     * @param group 新分组，自动编码
     * @return SUCCESS | FAILED | DUPLICATED
     */
    public DbStatusType insertGroup(Group group) {

        if (queryGroupByName(group.getName()) != null)
            return DbStatusType.DUPLICATED;

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
            "(" + COL_NAME + ", " + COL_ORDER + ", " + COL_COLOR + ") " +
            "values(?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        try {
            group.setOrder(queryAllGroups().size()); // 每次都到插入最后

            stat.bindString(1, group.getName()); // COL_NAME
            stat.bindLong(2, group.getOrder()); // COL_ORDER
            stat.bindString(3, group.getColor()); // COL_ORDER

            long ret_id = stat.executeInsert();
            if (ret_id != -1) {
                db.setTransactionSuccessful();
                group.setId((int) ret_id);
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
     * 更新分组
     * @param group 覆盖更新
     * @return SUCCESS | FAILED | DUPLICATED | DEFAULT
     */
    public DbStatusType updateGroup(Group group) {

        Group sameNameGroup = queryGroupByName(group.getName());
        if (sameNameGroup != null && sameNameGroup.getId() != group.getId())
            return DbStatusType.DUPLICATED;

        Group def = queryDefaultGroup();
        if (def.getId() == group.getId() && !def.getName().equals(group.getName()))
            return DbStatusType.DEFAULT;

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, group.getName());
        values.put(COL_ORDER, group.getOrder());
        values.put(COL_COLOR, group.getColor());

        int ret = db.update(TBL_NAME, values,
            COL_ID + " = ?", new String[] { String.valueOf(group.getId()) });
        dbMgr.closeDatabase();
        if (ret != 0) {
            precessOrder();
            return DbStatusType.SUCCESS;
        } else
            return DbStatusType.FAILED;
    }

    /**
     * 只修改分组顺序
     * @return SUCCESS | FAILED
     */
    public DbStatusType updateGroupsOrder(Group[] groups) {
        Group def = queryDefaultGroup();

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = null;
        for (Group group : groups) {
            if (group.getId() == def.getId())
                continue;

            cursor = db.rawQuery("SELECT " + COL_ORDER + " FROM " + TBL_NAME + " WHERE " + COL_ID + " = " + group.getId(), null);
            if (cursor.moveToFirst()) {
                int motoOrder = cursor.getInt(cursor.getColumnIndex(COL_ORDER));
                if (motoOrder == group.getOrder())
                    continue;
            }

            ContentValues values = new ContentValues();
            values.put(COL_ORDER, group.getOrder());

            int ret = db.update(TBL_NAME, values, COL_ID + " = " + group.getId(), null);
            if (ret == 0) {
                if (!cursor.isClosed()) cursor.close();
                db.endTransaction();
                dbMgr.closeDatabase();
                return DbStatusType.FAILED;
            }
        }

        if (cursor != null && !cursor.isClosed()) cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        dbMgr.closeDatabase();

        precessOrder();
        return DbStatusType.SUCCESS;
    }

    /**
     * 删除分组
     * @param id 删除的分组 id
     * @param isToDefault 是否转移到默认分组
     * @return SUCCESS | FAILED | DEFAULT
     */
    public DbStatusType deleteGroup(int id, boolean isToDefault) {

        Group defGroup = queryDefaultGroup();
        if (defGroup.getId() == id)
            return DbStatusType.DEFAULT;

        NoteDao noteDao = new NoteDao(context);
        List<Note> notes = noteDao.queryNotesByGroupId(id);

        try (SQLiteDatabase db = dbMgr.getWritableDatabase()) {

            if (notes.isEmpty()) {
                int ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(id) });
                return ret == 0 ? DbStatusType.FAILED : DbStatusType.SUCCESS;
            } else {
                db.beginTransaction();

                if (isToDefault) {
                    ContentValues values = new ContentValues();
                    values.put(NoteDao.COL_GROUP_ID, defGroup.getId());
                    int count = db.update(NoteDao.TBL_NAME, values, NoteDao.COL_GROUP_ID + " = ?", new String[] { String.valueOf(id) });
                    if (count != notes.size()) {
                        db.endTransaction();
                        return DbStatusType.FAILED;
                    }
                } else {
                    int count = db.delete(NoteDao.TBL_NAME, NoteDao.COL_GROUP_ID + " = ?", new String[] { String.valueOf(id) });
                    if (count != notes.size()) {
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
                    db.endTransaction();
                    return DbStatusType.SUCCESS;
                }
            }
        } finally {
            dbMgr.closeDatabase();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 处理顺序 (所有操作前 以及 更新删除操作后)
     */
    private void precessOrder() {
        List<Group> groups = queryAllGroups();
        Collections.sort(groups);

        SQLiteDatabase db = dbMgr.getWritableDatabase();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getOrder() != i) {
                ContentValues values = new ContentValues();
                values.put(COL_ORDER, i);
                db.update(TBL_NAME, values,
                    COL_ID + " = ?", new String[] { String.valueOf(groups.get(i).getId()) });
            }
        }
        dbMgr.closeDatabase();
    }
}
