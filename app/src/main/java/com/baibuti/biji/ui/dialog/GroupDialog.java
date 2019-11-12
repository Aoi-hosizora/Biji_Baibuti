package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
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
import butterknife.OnClick;

public class GroupDialog extends Dialog implements IContextHelper {

    private Activity activity;

    @BindView(R.id.id_GroupDialog_ButtonAdd)
    private Button m_btn_add;

    @BindView(R.id.id_GroupDialog_ButtonEdit)
    private Button m_btn_edit;

    @BindView(R.id.id_GroupDialog_ButtonDelete)
    private Button m_btn_delete;

    @BindView(R.id.id_GroupDialog_ButtonCancel)
    private Button m_btn_dismiss;

    @BindView(R.id.id_GroupDialog_ButtonUp)
    private Button m_btn_up;

    @BindView(R.id.id_GroupDialog_ButtonDown)
    private Button m_btn_down;

    @BindView(R.id.id_GroupDialog_GroupListView)
    private ListView m_list_group;

    /**
     * 当前被选中的项
     */
    private int GroupListViewClickId = 0;

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
        this.m_listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_group_list);

        refreshBtnPositionEnabled(0);

        groupAdapter = new GroupRadioAdapter(activity);
        groupList = new ArrayList<>();
        groupAdapter.setList(groupList);
        m_list_group.setAdapter(groupAdapter);
        m_list_group.setVisibility(View.VISIBLE);

        try {
            IGroupDao groupDao = DaoStrategyHelper.getInstance().getGroupDao(activity);
            groupList = groupDao.queryAllGroups();
            groupAdapter.notifyDataSetChanged();
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
    private void ButtonAdd_Clicked() {
        editGroup(null);
    }

    /**
     * 编辑分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonEdit)
    private void ButtonEdit_Clicked() {
        editGroup(groupList.get(GroupListViewClickId));
    }

    /**
     * 删除分组
     */
    @OnClick(R.id.id_GroupDialog_ButtonDelete)
    private void ButtonDelete_Clicked() {
        deleteGroup(groupList.get(GroupListViewClickId));
    }

    /**
     * 关闭对话框
     */
    @OnClick(R.id.id_GroupDialog_ButtonCancel)
    private void ButtonDismiss_Clicked() {
        DismissAndReturn(true);
    }

    /**
     * 上移
     */
    @OnClick(R.id.id_GroupDialog_ButtonUp)
    private void ButtonUp_Clicked() {
        moveGroupOrder(groupList.get(GroupListViewClickId), true);
    }

    /**
     * 下移
     */
    @OnClick(R.id.id_GroupDialog_ButtonDown)
    private void ButtonDown_Clicked() {
        moveGroupOrder(groupList.get(GroupListViewClickId), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private GroupRadioAdapter groupAdapter;
    private List<Group> groupList;

    /**
     * 刷新列表
     */
    private void refreshGroupList() {

    }

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
        if (pos == 0 || pos == 1)
            setEnabled(m_btn_up, false);

        // 最后一行
        if (pos == groupList.size() - 1)
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

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        refreshGroupList();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (inputGroup == null)  // 新分组
                                    GroupListViewClickId = groupList.size() - 1; // 选择最后一项

                                groupAdapter.setChecked(GroupListViewClickId);
                                m_list_group.setAdapter(groupAdapter); // 必要

                                DismissAndReturn(false);

                            }
                        });

                }).start();
            }
        });
        dialog.setView(new EditText(getContext()));
        dialog.show();
    }

    /**
     * 删除指定分组
     */
    private void deleteGroup(Group inputGroup) {
        GroupDeleteDialog groupDeleteDialog = new GroupDeleteDialog(activity, inputGroup, new GroupDeleteDialog.OnDeleteGroupListener() {
            @Override
            public void DeleteGroupFinished() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        refreshGroupList();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                GroupListViewClickId--;
                                groupAdapter.setChecked(GroupListViewClickId);
                                m_list_group.setAdapter(groupAdapter); // 必要

                                DismissAndReturn(false);
                            }
                        });
                    }
                }).start();
            }
        });
        groupDeleteDialog.showDialog();
    }

    private int currentPos;

    /**
     * 移动分组 Order，不同步
     * @param currentGroup 当前分组
     * @param isUP
     *          true: Order--
     *          false: Order++
     */
    private void moveGroupOrder(Group currentGroup, boolean isUP) {

        currentPos = GroupListViewClickId;

        if (isUP && currentPos != 1 && currentPos != 0) { // 上移
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int motoorder = currentGroup.getOrder();

                    currentGroup.setOrder(motoorder - 1);
                    Group upGroup = groupDao.queryGroupByOrder(motoorder - 1);
                    if (upGroup != null) {
                        upGroup.setOrder(motoorder);
                        groupDao.updateGroup(upGroup, false);
                    }

                    currentPos--;

                    groupDao.updateGroup(currentGroup, false); // 更新数据库

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            moveGroupOrderRe();
                        }
                    });
                }
            }).start();
        }
        else if (!isUP && currentPos != groupList.size() - 1 && currentPos != 0) { // 下移
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ShowLogE("moveGroupOrder", "DOWN");
                    int motoorder = currentGroup.getOrder();

                    currentGroup.setOrder(motoorder + 1);
                    Group downGroup = groupDao.queryGroupByOrder(motoorder + 1); // 向下交换
                    if (downGroup != null) {
                        downGroup.setOrder(motoorder);
                        groupDao.updateGroup(downGroup, false);
                    }

                    currentPos++;

                    groupDao.updateGroup(currentGroup, false); // 更新数据库

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            moveGroupOrderRe();
                        }
                    });
                }
            }).start();

        }
    }

    /////////////////////////////////////////////////////

    private void DismissAndReturn(boolean isReturn) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 不允许 back
                        if (m_listener != null)
                            m_listener.onUpdated(); // 同时令 Note Frac 更新分组信息

                        if (isReturn)
                            dismiss();
                    }
                });
            }
        }).start();

    }

    @Override
    public void dismiss() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // 最后同步
                groupDao.pushpull();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GroupDialog.super.dismiss();
                    }
                });
            }
        }).start();
    }
}