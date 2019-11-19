package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.daoInterface.IGroupDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.GroupRadioAdapter;
import com.baibuti.biji.R;

import java.util.ArrayList;
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
         * 数据加载完成
         */
        void onLoaded();

        /**
         * 修改完成
         */
        void onUpdated();
    }

    public GroupDialog(Activity activity, OnUpdateGroupListener listener) {
        super(activity);
        this.activity = activity;
        this.groupList = new ArrayList<>();
        this.m_listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_group_list);
        ButterKnife.bind(this);

        groupAdapter = new GroupRadioAdapter(activity);
        groupList = new ArrayList<>();
        groupAdapter.setList(groupList);
        m_list_group.setAdapter(groupAdapter);
        m_list_group.setVisibility(View.VISIBLE);

        refreshBtnPositionEnabled(0);

        try {
            IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
            groupList.addAll(groupDao.queryAllGroups());
            groupAdapter.notifyDataSetChanged();
            groupAdapter.setChecked(0);
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(activity, "错误", "分组数据加载错误，请重试。");
            dismiss();
        }

        if (m_listener != null)
            m_listener.onLoaded();
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
        editGroup(groupList.get(groupAdapter.getCurrentItemIndex()));
    }

    /**
     * 删除分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonDelete)
    void ButtonDelete_Clicked() {
        deleteGroup(groupList.get(groupAdapter.getCurrentItemIndex()));
    }

    /**
     * 完成对话框
     */
    @OnClick(R.id.id_GroupDialog_ButtonOK)
    void ButtonOK_Clicked() {
        IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
        try {
            for (Group group : groupList) {
                groupDao.updateGroup(group); // 只修改分组顺序
            }
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(activity, "错误", "分组更新失败：" + ex.getMessage());
        }
        dismiss();
    }

    /**
     * 上移
     */
    @OnClick(R.id.id_GroupDialog_ButtonUp)
    void ButtonUp_Clicked() {
        moveGroupOrder(groupList.get(groupAdapter.getCurrentItemIndex()), true);
    }

    /**
     * 下移
     */
    @OnClick(R.id.id_GroupDialog_ButtonDown)
    void ButtonDown_Clicked() {
        moveGroupOrder(groupList.get(groupAdapter.getCurrentItemIndex()), false);
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

        // 首行与次行，默认分行不动
        if (pos <= 1)
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
        GroupEditDialog dialog = new GroupEditDialog(activity, inputGroup, () -> {
            if (inputGroup == null)
                groupAdapter.setChecked(groupList.size() - 1);
            groupAdapter.notifyDataSetChanged();
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 删除指定分组
     */
    private void deleteGroup(Group inputGroup) {
        GroupEditDialog dialog = new GroupEditDialog(activity, inputGroup, null);
        dialog.DeleteButton_Clicked();
    }

    /**
     * 移动分组
     */
    private void moveGroupOrder(Group group, boolean isUP) {
        int currentPos = groupAdapter.getCurrentItemIndex();
        if (isUP && currentPos != 1 && currentPos != 0) { // 上移
            Group upGroup = groupList.get(currentPos - 1);
            group.setOrder(currentPos - 1);
            upGroup.setOrder(currentPos);
        }
        else if (!isUP && currentPos != groupList.size() - 1 && currentPos != 0) { // 下移
            Group downGroup = groupList.get(currentPos + 1);
            group.setOrder(currentPos + 1);
            downGroup.setOrder(currentPos);
        }
        refreshBtnPositionEnabled(currentPos);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    public void dismiss() {
        if (m_listener != null)
            m_listener.onUpdated();
        super.dismiss();
    }
}