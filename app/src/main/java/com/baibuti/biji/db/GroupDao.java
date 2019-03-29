package com.baibuti.biji.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baibuti.biji.Data.Group;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class GroupDao {
    private MyOpenHelper helper;
    private NoteDao noteDataDao;

    public GroupDao(Context context) {
        helper = new MyOpenHelper(context);
        noteDataDao = new NoteDao(context);
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<Group> queryGroupAll() { // ArrayList
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Group> groupList = new ArrayList<Group>();

        Group group ;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, null, null, null, null, "null");
            while (cursor.moveToNext()) {

                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));
                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));

                //生成一个分类

                group = new Group();
                group.setId(groupId);
                group.setName(groupName);
                group.setOrder(order);
                group.setColor(color);

                groupList.add(group);
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
        return groupList;
    }

    /**
     * 根据分类名查询分类
     *
     * @param groupName
     * @return
     */
    public Group queryGroupByName(String groupName) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            Log.i(TAG, "###queryGroupByName: "+groupName);
            cursor = db.query("db_group", null, "g_name=?", new String[]{groupName}, null, null, null);
            while (cursor.moveToNext()) {
                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));

                int order = cursor.getInt(cursor.getColumnIndex("g_order"));

                String color = cursor.getString(cursor.getColumnIndex("g_color"));

                //生成一个分类

                group = new Group();

                group.setId(groupId);
                group.setName(groupName);

                group.setOrder(order);

                group.setColor(color);

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
        return group;
    }

    /**
     * 根据分类ID查询分类
     *
     * @return
     */
    public Group queryGroupById(int groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, "g_id=?", new String[]{groupId + ""}, null, null, null);
            while (cursor.moveToNext()) {
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));

                String color = cursor.getString(cursor.getColumnIndex("g_color"));

                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));


                //生成一个订单

                group = new Group();

                group.setId(groupId);
                group.setName(groupName);

                group.setOrder(order);

                group.setColor(color);

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
        return group;
    }

    /**
     * 添加一个分类
     */
    public long insertGroup(Group group) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into db_group(g_name,g_order,g_color) values(?,?,?)";

        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            stat.bindString(1, group.getName());
            stat.bindLong(2, group.getOrder());
            stat.bindString(3, group.getColor());

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
    public void updateGroup(Group group) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put("g_name", group.getName());

            values.put("g_order", group.getOrder());

            values.put("g_color", group.getColor());

            db.update("db_group", values, "g_id=?", new String[]{group.getId() + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 删除一个分类
     */
    public int deleteGroup(int groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_group", "g_id=?", new String[]{groupId + ""});
            //Group group = queryGroupByName("默认笔记");
            //noteDataDao.updateNote2(groupId, group.getGroupId());
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
