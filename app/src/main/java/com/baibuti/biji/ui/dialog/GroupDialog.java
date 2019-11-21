package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IGroupDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.GroupRadioAdapter;
import com.baibuti.biji.R;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDialog extends AlertDialog implements IContextHelper {

    private Activity activity;

    @BindView(R.id.id_GroupDialog_ButtonUp)
    Button m_btn_up;

    @BindView(R.id.id_GroupDialog_ButtonDown)
    Button m_btn_down;

    @BindView(R.id.id_GroupDialog_GroupListView)
    ListView m_list_group;

    private GroupRadioAdapter groupAdapter;
    private List<Group> groupList;

    private OnUpdateGroupListener m_listener;

    public interface OnUpdateGroupListener {
        /**
         * 修改完成
         */
        void onUpdated();
    }

    public GroupDialog(Activity activity, @NonNull List<Group> groups, OnUpdateGroupListener listener) {
        super(activity);
        this.activity = activity;
        this.groupList = groups;
        this.m_listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_group_list);
        ButterKnife.bind(this);
        refreshBtnPositionEnabled(0);

        groupAdapter = new GroupRadioAdapter(activity);
        groupAdapter.setGroupList(groupList);
        groupAdapter.setCurrentItem(groupList.get(0));
        groupAdapter.setOnRadioButtonClickListener(this::refreshBtnPositionEnabled);
        m_list_group.setAdapter(groupAdapter);
    }

    /**
     * 新建分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonAdd)
    void ButtonAdd_Clicked() {
        addGroup();
    }

    /**
     * 编辑分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonEdit)
    void ButtonEdit_Clicked() {
        editGroup(groupAdapter.getCurrentItem());
    }

    /**
     * 删除分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonDelete)
    void ButtonDelete_Clicked() {
        deleteGroup(groupAdapter.getCurrentItem());
    }

    /**
     * 完成顺序修改
     */
    @OnClick(R.id.id_GroupDialog_ButtonOK)
    void ButtonOK_Clicked() {
        IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
        try {
            if (groupDao.updateGroupsOrder(groupList.toArray(new Group[0])) == DbStatusType.SUCCESS) {
                Log.i("", "ButtonOK_Clicked: order SUCCESS");
                if (m_listener != null)
                    m_listener.onUpdated();
                dismiss();
            } else
                showAlert(getContext(), "错误", "分组顺序更新失败。");
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(activity, "错误", "分组更新失败：" + ex.getMessage());
        }
    }

    /**
     * 上移
     */
    @OnClick(R.id.id_GroupDialog_ButtonUp)
    void ButtonUp_Clicked() {
        moveGroupOrder(groupList.indexOf(groupAdapter.getCurrentItem()), true);
    }

    /**
     * 下移
     */
    @OnClick(R.id.id_GroupDialog_ButtonDown)
    void ButtonDown_Clicked() {
        moveGroupOrder(groupList.indexOf(groupAdapter.getCurrentItem()), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 设置自定义背景 按钮可用
     */
    private void setEnabled(Button button, boolean en) {
        button.setEnabled(en);
        if (en)
            button.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
        else
            button.setTextColor(getContext().getResources().getColor(R.color.disable));
    }

    /**
     * 根据位置刷新 Enable
     * @param pos 0 .. #n - 1
     */
    private void refreshBtnPositionEnabled(int pos) {
        setEnabled(m_btn_up, true);
        setEnabled(m_btn_down, true);

        // 首行
        if (pos == 0) {
            setEnabled(m_btn_up, false);
            setEnabled(m_btn_down, false);
        }

        // 次行
        if (pos == 1)
            setEnabled(m_btn_up, false);

        // 最后一行
        if (pos >= groupList.size() - 1)
            setEnabled(m_btn_down, false);
    }

    /**
     * 新建分组
     */
    private void addGroup() {
        editGroup(null);
    }

    /**
     * 编辑分组
     * @param inputGroup
     *          null 新分组
     *          notnull 更新分组
     */
    private void editGroup(Group inputGroup) {
        GroupEditDialog dialog = new GroupEditDialog(activity, inputGroup, new GroupEditDialog.OnUpdateGroupListener() {

            @Override
            public void onUpdated(Group group) {
                if (inputGroup == null) { // 新建
                    groupList.add(group);
                    groupAdapter.setCurrentItem(group);
                }
                groupAdapter.notifyDataSetChanged();
                if (m_listener != null)
                    m_listener.onUpdated();
            }

            @Override
            public void onDeleted(Group group) {
                groupList.remove(group);
                groupAdapter.notifyDataSetChanged();
                if (m_listener != null)
                    m_listener.onUpdated();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 删除指定分组
     */
    private void deleteGroup(Group inputGroup) {
        GroupEditDialog dialog = new GroupEditDialog(activity, inputGroup, new GroupEditDialog.OnUpdateGroupListener() {
            @Override
            public void onUpdated(Group group) { }

            @Override
            public void onDeleted(Group group) {
                groupList.remove(group);
                groupAdapter.setCurrentItem(groupList.get(0));
                groupAdapter.notifyDataSetChanged();
                if (m_listener != null)
                    m_listener.onUpdated();
            }
        });
        dialog.DeleteButton_Clicked();
    }

    /**
     * 移动分组
     */
    private void moveGroupOrder(int pos, boolean isUP) {
        Group currGroup = groupList.get(pos);
        if (isUP && pos > 1) { // 上移
            Group upGroup = groupList.get(pos - 1);

            currGroup.setOrder(pos - 1);
            upGroup.setOrder(pos);
            refreshBtnPositionEnabled(pos - 1);
        }
        else if (!isUP && pos < groupList.size() - 1) { // 下移
            Group downGroup = groupList.get(pos + 1);

            currGroup.setOrder(pos + 1);
            downGroup.setOrder(pos);
            refreshBtnPositionEnabled(pos + 1);
        } else
            return;

        Collections.sort(groupList);
        groupAdapter.notifyDataSetChanged();
    }
}