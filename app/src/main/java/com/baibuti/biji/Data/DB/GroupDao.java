package com.baibuti.biji.Data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Note.GroupUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class GroupDao {
    private MyOpenHelper helper;
    private Context context;

    public GroupDao(Context context) {
        this(context, (!(AuthMgr.getInstance().getUserName().isEmpty())) ? AuthMgr.getInstance().getUserName() : "");
    }

    public GroupDao(Context context, String username) {
        helper = new MyOpenHelper(context, username);
        this.context = context;
    }

    /**
     * 更新分组日志，可能存在冗杂
     */
    private void updateLog() {
        UtLogDao utLogDao = new UtLogDao(context);
        utLogDao.updateLog(LogModule.Mod_Group);
    }

    /**
     * 进行 push pull
     */
    public void pushpull() {

        // TODO 需要处理分组

        if (AuthMgr.getInstance().isLogin()) {
            Log.e("测试", "GroupDao.pushpull: "+new UtLogDao(context).getLog(LogModule.Mod_Group).getUpdateTime().toString());
            if (ServerDbUpdateHelper.isLocalNewer(context, LogModule.Mod_Group)) { // 本地新
                // TODO 异步
                ServerDbUpdateHelper.pushData(context, LogModule.Mod_Group);
            }
            else if (ServerDbUpdateHelper.isLocalOlder(context, LogModule.Mod_Group)) { // 服务器新
                // TODO 同步
                ServerDbUpdateHelper.pullData(context, LogModule.Mod_Group);
            }
        }
    }

    // region 查询全部 queryGroupAll selectAllGroup

    /**
     * 查询所有分类列表，同步
     * @return
     */
    public List<Group> queryGroupAll() {
        return queryGroupAll(true);
    }

    /**
     * 查询所有分类列表，同步?
     * @param isLogCheck
     * @return
     */
    public List<Group> queryGroupAll(boolean isLogCheck) { // ArrayList

        if (isLogCheck) pushpull();

        List<Group> groupList = selectAllGroup();

        // 处理 默认分组
        if (groupList.isEmpty())
            groupList.add(insertDefaultGroup());

        handleDefaultGroupOrder();

        // 处理 重复顺序
        handleOrderDuplicate(groupList);

        return groupList;
    }

    /**
     * 内部 select, 不处理同步
     * @return
     */
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

                group = new Group(groupId, groupName, order, color);
                groupList.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return groupList;
    }

    /**
     * @return 返回当前数据库内有多少分组，从一开始算
     */
    public int queryGroupCnt() {
        return selectAllGroup().size();
    }

    // endregion

    // region 自定义查询 queryGroupByName queryGroupById queryGroupByOrder


    // /**
    //  * 查询分组名，同步
    //  * @param groupName
    //  * @return
    //  */
    // public Group queryGroupByName(String groupName) {
    //     return queryGroupByName(groupName, false);
    // }

    /**
     * 按照分组名查询，同步？
     * @param groupName
     * @return
     */
    public Group queryGroupByName(String groupName, boolean isLogCheck) {

        if (isLogCheck) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;

        try {
            cursor = db.query("db_group", null, "g_name=?", new String[]{groupName},
                    null, null, null);

            while (cursor.moveToNext()) {

                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));

                // 返回
                group = new Group(groupId, groupName, order, color);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return group;
    }

    /**
     * 按照分组 id 查询，同步
     * @param groupId
     * @return
     */
    public Group queryGroupById(int groupId) {
        return queryGroupById(groupId, true);
    }

    /**
     * 按照分组 id 查询，同步？
     * @param groupId
     * @return
     */
    public Group queryGroupById(int groupId, boolean isCheckLog) {

        if (isCheckLog) pushpull();

        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, "g_id=?", new String[]{groupId + ""},
                    null, null, null);

            while (cursor.moveToNext()) {

                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));
                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));

                // 返回
                group = new Group(groupId, groupName, order, color);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return group;
    }

    /**
     * 按照分组顺序查询，默认查询第一个
     * 不同步, 效率问题!!!
     * @param order
     * @return
     */
    public Group queryGroupByOrder(int order) {
        return queryGroupByOrder(order, 1);
    }

    /**
     * 按照分组顺序查询，需保证唯一
     * 不同步, 效率问题!!!
     * @param order
     * @param sumcnt 查询第几个分组，默认为1
     * @return
     */
    private Group queryGroupByOrder(int order, int sumcnt) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, "g_order=?", new String[]{order + ""},
                    null, null, null);

            int cnt = 0;

            while (cursor.moveToNext()) {

                cnt++;
                if (sumcnt != cnt)
                    continue; // 查询多少个之后

                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));
                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));

                // 返回
                group = new Group(groupId, groupName, order, color);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return group;
    }

    // endregion 自定义查询

    // region 查重分组名 checkDuplicate HandleDuplicate

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

    // endregion

    // region 查重顺序 checkOrderDuplicate handleOrderDuplicateWhenUpdate

    /**
     * 查询当前的 Order 是否已经存在
     * @param handleGroup 要处理更新或者插入的分组
     *
     * @return 重复次数，可能存在多次重复情况
     */
    private int checkOrderDuplicate(Group handleGroup) {
        List<Group> tmp = selectAllGroup();
        int cnt=0;
        for (Group g : tmp)
            if (g.getOrder() == handleGroup.getOrder())
                cnt++;
        return cnt;
    }

    /**
     * 更新时处理 Order 重复问题
     * @param handleGroup 更新的分组
     */
    private void handleOrderDuplicateWhenUpdate(Group handleGroup) {
        // 关键 !!!
        if (handleGroup != null && checkOrderDuplicate(handleGroup) != 1) { // 为1，只存在一个，处理结束

            Log.e(TAG, "handleOrderDuplicateWhenUpdate: " + handleGroup.getName());

            // 绕过默认分组
            if (handleGroup.getOrder() == 0) {
                handleGroup.setOrder(1);
                updateGroup(handleGroup, false);
            }

            Group firstGroup = queryGroupByOrder(handleGroup.getOrder());

            if (firstGroup != null) {

                if (firstGroup.equals(handleGroup)) {
                    firstGroup = queryGroupByOrder(handleGroup.getOrder(), 2); // 查询第二个
                    if (firstGroup == null)
                        return;
                }

                // 将重复的下移，正常情况下最多只存在两个重复
                firstGroup.setOrder(firstGroup.getOrder() + 1);

                updateGroup(firstGroup, false); // 存进数据库才能 checkOrderDuplicate

                handleOrderDuplicateWhenUpdate(firstGroup); // 递归
                // 若checkOrderDuplicate(firstGroup) == 1，修改的分组不重复，结束递归
            }
        }
//        handleOrderGap(); // 每次检查重复都检查一遍间隔
    }

    /**
     * 删除时处理 Order 空缺问题，处理：压缩
     */
    public void handleOrderGap() {
        int order = 0;

        for (int i=0; i<queryGroupCnt(); i++) {

            Group nextGroup;
            boolean hasGap = false; // 存在间隙

            while ((nextGroup = queryGroupByOrder(order)) == null) {
                // 找不到紧接着的 order
                Log.e(TAG, "handleOrderGap: " + order );
                order++;
                hasGap = true;
            }
            if (hasGap) {
                nextGroup.setOrder(i); // 压缩
                updateGroup(nextGroup, false);
                handleOrderDuplicateWhenUpdate(nextGroup);
            }
            order++;
        }
    }

    /**
     * 处理所有重复 Order
     */
    public void handleOrderDuplicate(List<Group> groupList) {
        for (Group g : groupList) {
            handleOrderDuplicateWhenUpdate(g);
        }
    }

    // endregion

    // region 默认分组 queryDefaultGroup insertDefaultGroup checkDefaultGroup

    /**
     * 查询是否存在默认分组，若不存在则创建
     * @return 默认分组引用
     */
    public Group queryDefaultGroup() {

        Group group = null;

        group = this.queryGroupByName(Group.GetDefaultGroupName, false);
        if (group != null)
            return group;

        return insertDefaultGroup();
    }

    /**
     * 创建默认分组
     * @return
     */
    private Group insertDefaultGroup() {
        Group def = new Group();

        def.setOrder(0);

        def.setColor("#F0F0F0");
        def.setName(Group.GetDefaultGroupName);

        this.insertGroup(def, def.getId());

        return this.queryGroupByName(Group.GetDefaultGroupName, false);
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
     * 处理默认分组的顺序不为顶层
     */
    private void handleDefaultGroupOrder() {
        Group def = queryDefaultGroup();
        if (def.getOrder() != 0) {
            def.setOrder(0);
            updateGroup(def);
        }
    }

    // endregion queryDefaultGroup insertDefaultGroup

    // region 数据增删改 insertGroup updateGroup deleteGroup

    /**
     * 添加分组，同步
     * @param group
     * @return
     */
    public long insertGroup(Group group) {


        pushpull();

        long ret = insertGroup(group, -1);


        if (AuthMgr.getInstance().isLogin()) {
            try {
                if (GroupUtil.insertGroup(group) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Group);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * 添加一个分类，不同步
     * @param group
     * @param idx
     * @return
     */
    public long insertGroup(Group group, int idx) {

        // if (isCheckLog) pushpull();

        HandleDuplicate(group, null);

        int order = queryGroupCnt(); // 每次都插入到最后

        SQLiteDatabase db = helper.getWritableDatabase();
        String sql;

        if (idx == -1)
            sql = "insert into db_group(g_name,g_order,g_color) values(?,?,?)";
        else
            sql = "insert into db_group(g_name,g_order,g_color, g_id) values(?,?,?,?)";


        long ret = 0;

        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {

            Log.e(TAG, "insertGroup: order" + order);

            stat.bindString(1, group.getName());

            stat.bindLong(2, order);

            stat.bindString(3, group.getColor());

            if (idx != -1)
                stat.bindLong(4, idx); // id

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
        updateLog();

        return ret;
    }

    /**
     * 更新一个分类，同步
     */
    public void updateGroup(Group group) {
        updateGroup(group, true);
    }

    /**
     * 更新一个分类，同步?
     */
    public void updateGroup(Group group, boolean isCheckLog) {

        Group oldGroup = queryGroupById(group.getId(), isCheckLog);
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
        }
        finally {
            if (db != null) {
                db.close();
            }
        }

        if (isCheckLog && AuthMgr.getInstance().isLogin()) {
            try {
                if (GroupUtil.updateGroup(group) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Group);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        updateLog();
    }

    public int deleteGroup(int groupId) throws EditDefaultGroupException {
        return deleteGroup(groupId, true);
    }

    /**
     * 删除一个分类
     */
    public int deleteGroup(int groupId, boolean isCheckLog) throws EditDefaultGroupException {

        // check: 同步，检查默认
        // uncheck: 不同步，忽略默认

        Group g = queryGroupById(groupId, false);

        if (isCheckLog)
            if (checkDefaultGroup(queryGroupById(groupId)))
                throw new EditDefaultGroupException();

        //////////////////////////////////////////////////

        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_group", "g_id=?", new String[]{groupId + ""});
            // 处理删除间隙
            handleOrderGap();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
        if (selectAllGroup().isEmpty())
            queryDefaultGroup();

        updateLog();

        if (isCheckLog && AuthMgr.getInstance().isLogin()) {
            try {
                if (GroupUtil.updateGroup(g) != null)
                    ServerDbUpdateHelper.pushLog(context, LogModule.Mod_Group);
            }
            catch (ServerErrorException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    // endregion
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
