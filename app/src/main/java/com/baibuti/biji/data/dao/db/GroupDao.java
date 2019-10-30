package com.baibuti.biji.data.dao.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.baibuti.biji.data.dao.DbOpenHelper;
import com.baibuti.biji.data.dao.daoInterface.IGroupDao;
import com.baibuti.biji.data.model.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDao implements IGroupDao {

    private final static String TBL_NAME = "db_group";

    private final static String COL_ID = "g_id";
    private final static String COL_NAME = "g_name";
    private final static String COL_ORDER = "g_order";
    private final static String COL_COLOR = "g_color";

    private DbOpenHelper helper;

    public GroupDao(Context context) {
        helper = new DbOpenHelper(context);

        // 处理默认
        if (queryGroupAll().isEmpty())
            insertGroup(Group.DEF_GROUP);

        // 预处理顺序
        precessOrder();
    }

    // queryGroupAll queryGroupById queryDefaultGroup insertGroup updateGroup deleteGroup
    // region 常规操作 增删改查

    /**
     * 查询所有分组
     * @return 分组列表
     */
    @Override
    public List<Group> queryGroupAll() {

        SQLiteDatabase db = helper.getWritableDatabase();
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
            if (db != null && db.isOpen()) db.close();
        }

        return groupList;
    }

    /**
     * 按照分组 gid 查询
     * @param groupId 分组 id
     * @return 指定分组
     */
    @Override
    public Group queryGroupById(int groupId) {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_ID + " = " + groupId;

        Group group = null;
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                int order = cursor.getInt(cursor.getColumnIndex(COL_ORDER));
                String color = cursor.getString(cursor.getColumnIndex(COL_COLOR));

                group = new Group(id, name, order, color);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return group;
    }

    /**
     * 查询默认分组
     * @return 返回数据库中的默认分组
     */
    @Override
    public Group queryDefaultGroup() {

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = null;
        String sql = "select * from " + TBL_NAME + " where " + COL_NAME + " = \"" + Group.DEF_GROUP.getName() + "\"";

        Group group = null;
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {

                group = new Group();
                group.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
                group.setName(cursor.getString(cursor.getColumnIndex(COL_NAME)));
                group.setOrder(cursor.getInt(cursor.getColumnIndex(COL_ORDER)));
                group.setColor(cursor.getString(cursor.getColumnIndex(COL_COLOR)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return group;
    }

    /**
     * 添加分组 (没必要刷新 Order)
     * @param group 新分组，自动编码
     * @return 分组 id
     */
    @Override
    public long insertGroup(Group group) {

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into " + TBL_NAME +
                "(" + COL_NAME + ", " + COL_ORDER + ", " + COL_COLOR + ") " +
                "values(?, ?, ?)";
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();

        long ret_id = 0;
        try {
            group.setOrder(queryGroupAll().size()); // 每次都到插入最后

            stat.bindString(1, group.getName()); // COL_NAME
            stat.bindLong(2, group.getOrder()); // COL_ORDER
            stat.bindString(3, group.getColor()); // COL_COLOR

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
     * 更新分组 (没必要刷新 Order)
     * @param group 覆盖更新笔记
     * @return 是否成功更新 (更新非 0 项)
     */
    @Override
    public boolean updateGroup(Group group) {

        // 更新默认分组的标签
        Group def = queryDefaultGroup();
        if (def.getId() == group.getId() && !def.getName().equals(group.getName()))
            return false;

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, group.getName());
        values.put(COL_ORDER, group.getOrder());
        values.put(COL_COLOR, group.getColor());

        int ret = db.update(TBL_NAME, values, COL_ID + " = ?", new String[] { String.valueOf(group.getId()) });
        db.close();

        return ret != 0;
    }


    /**
     * 删除分组 (刷新 Order)
     * @param groupId 删除的分组 id
     * @return 是否成功删除 (删除 1 项)
     */
    @Override
    public boolean deleteGroup(int groupId) {

        // 删除默认分组
        if (queryDefaultGroup().getId() == groupId)
            return false;

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete(TBL_NAME, COL_ID + " = ?", new String[] { String.valueOf(groupId) });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null && db.isOpen())
                db.close();
        }

        // 删除后刷新 Order
        precessOrder();
        return ret == 1;
    }

    // endregion

    // precessOrder
    // region 内部处理

    /**
     * 处理顺序 (所有操作前 以及 删除操作后)
     */
    private void precessOrder() {
        List<Group> groups = queryGroupAll();
        Collections.sort(groups);

        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getOrder() != i) {
                groups.get(i).setOrder(i);
                updateGroup(groups.get(i));
            }
        }
    }

    // endregion
}