package com.baibuti.biji.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.baibuti.biji.Data.Group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class GroupDao {
    private MyOpenHelper helper;
    private Context context;

    public GroupDao(Context context) {
        helper = new MyOpenHelper(context);
        this.context = context;
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<Group> queryGroupAll() { // ArrayList

        List<Group> groupList = selectAllGroup();

        if (groupList.isEmpty())
            groupList.add(insertDefaultGroup());

        return groupList;
    }

    // 内部 select
    private List<Group> selectAllGroup() {
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


    public Group queryDefaultGroup() {
        Group group = null;

        group = this.queryGroupByName(Group.GetDefaultGroupName);
        if (group != null)
            return group;

        return insertDefaultGroup();
    }

    private Group insertDefaultGroup() {
        Group def = new Group();
        def.setOrder(0);
        def.setColor("#F0F0F0");
        def.setName(Group.GetDefaultGroupName);

        this.insertGroup(def);

        return this.queryGroupByName(Group.GetDefaultGroupName);
    }

    /**
     * 检查是否有重复
     */
    public int checkDuplicate(Group group, Group oldgroup) {
        List<Group> tmp = selectAllGroup();
        int cnt=0;
        if (oldgroup==null)
            oldgroup = new Group();
        for (Group g : tmp)
            if (g.getName().equals(group.getName()) && !g.getName().equals(oldgroup.getName()))
                cnt++;
        return cnt;
    }

    /**
     * 处理重复插入
     */
    private Group HandleDuplicate(Group group, Group oldgroup) {
        int cnt = checkDuplicate(group, oldgroup);
        if (cnt!=0)
            group.setName(group.getName() + " (" + cnt + ")");

        return group;
    }

    /**
     * 添加一个分类
     */
    public long insertGroup(Group group) {
        HandleDuplicate(group, null);

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
        Group oldGroup = queryGroupById(group.getId());
        try {
            if (checkDefaultGroup(oldGroup)) {
                if (!oldGroup.getName().equals(group.getName()))
                    throw new EditDefaultGroupException();
            }
        }
        catch (EditDefaultGroupException ed) {
            Toast.makeText(context, "无法修改默认分组。", Toast.LENGTH_SHORT).show();
            return;
        }
        HandleDuplicate(group, oldGroup);

        //////////////////////////////////////////////////

        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put("g_name", group.getName());

            values.put("g_order", group.getOrder());

            values.put("g_color", group.getColor());

            db.update("db_group", values, "g_id=?", new String[]{group.getId() + ""});
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
    private boolean checkDefaultGroup(Group group) {
        if (group==null) {
            Log.e("checkDefaultGroup", "checkDefaultGroup: group==null");
        }
        else
            if (Group.GetDefaultGroupName.equals(group.getName())) {
                return true;
            }
        return false;
    }

    /**
     * 删除一个分类
     */
    public int deleteGroup(int groupId) throws EditDefaultGroupException {
        if (checkDefaultGroup(queryGroupById(groupId)))
            throw new EditDefaultGroupException();

        //////////////////////////////////////////////////

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_group", "g_id=?", new String[]{groupId + ""});
        }

        catch (Exception e) {
                e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        if (selectAllGroup().isEmpty())
            queryDefaultGroup();

        return ret;
    }
}

class EditDefaultGroupException extends Exception {

    public EditDefaultGroupException() {
        super();
    }

    public EditDefaultGroupException(String message) {
        super(message);
    }

    public EditDefaultGroupException(String message, Throwable cause) {
        super(message,cause);
    }

    public EditDefaultGroupException(Throwable cause) {

        super(cause);
    }
}
