package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.data.adapter.GroupRadioAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.data.dao.db.GroupDao;
import com.baibuti.biji.data.dao.db.NoteDao;

import java.util.Collections;
import java.util.List;

public class GroupDialog extends AlertDialog implements OnClickListener {
    private OnUpdateGroupListener mListener; //接口

    private Activity activity;

    private Button mButtonEdit;
    private Button mButtonCancel;
    private Button mButtonAdd;
    private Button mButtonDelete;
    private Button mButtonUE;
    private Button mButtonSHITA;

    private ListView GroupListView;

    /**
     * 当前被选中的项
     */
    private int GroupListViewClickId = 0;

    public interface OnUpdateGroupListener{
        void UpdateGroupFinished(); // 修改引发的事件
        void OnUICreateFinished(); // 显示成功
    }

    public GroupDialog(Activity activity, OnUpdateGroupListener mListener) {
        super(activity);
        this.activity = activity;
        this.mListener = mListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_groupdialog);

        mButtonEdit = (Button)findViewById(R.id.id_GroupDialog_ButtonEdit);
        mButtonCancel = (Button)findViewById(R.id.id_GroupDialog_ButtonCancel);
        mButtonAdd = (Button)findViewById(R.id.id_GroupDialog_ButtonAdd);
        mButtonDelete = (Button)findViewById(R.id.id_GroupDialog_ButtonDelete);
        mButtonUE = (Button)findViewById(R.id.id_GroupDialog_ButtonUE);
        mButtonSHITA = (Button)findViewById(R.id.id_GroupDialog_ButtonSHITA);

        mButtonEdit.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        mButtonAdd.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);
        mButtonUE.setOnClickListener(this);
        mButtonSHITA.setOnClickListener(this);

        noteDao = new NoteDao(getContext());
        groupDao = new GroupDao(getContext());

        new Thread(new Runnable() {
            @Override
            public void run() {

                handleOrder();

                refreshGroupList();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setEnabled(mButtonUE, false);
                        setEnabled(mButtonSHITA, false);

                        groupAdapter.setChecked(GroupListViewClickId);

                        GroupListView = (ListView) findViewById(R.id.id_GroupDialog_GroupListView);
                        GroupListView.setAdapter(groupAdapter);
                        GroupListView.setVisibility(View.VISIBLE);

                        if (mListener != null)
                            mListener.OnUICreateFinished();
                    }
                });
            }
        }).start();

    }

    /**
     * 全局设置 Log 格式
     * 用于 static 方法不使用接口
     * @param FunctionName
     * @param Msg
     */
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "GroupDialog";
        Log.e(getContext().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    private GroupRadioAdapter groupAdapter;
    private List<Group> GroupList;
    private GroupDao groupDao;
    private NoteDao noteDao;

    /**
     * 刷新列表，同步
     */
    private void refreshGroupList() {
        refreshGroupList(true);
    }

    /**
     * 刷新列表
     * @param isLogCheck 次序时不同步，默认同步
     */
    private void refreshGroupList(boolean isLogCheck) {

        GroupList = groupDao.queryGroupAll(isLogCheck);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Collections.sort(GroupList);
                groupAdapter = new GroupRadioAdapter(getContext(), GroupList, new GroupRadioAdapter.OnRadioButtonSelect() {
                    @Override
                    public void onSelect(int position) {
                        GroupListViewClickId = position;
                        refreshUeShitaEnabled(position);
                    }
                });
                groupAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setEnabled(Button button, boolean en) {
        button.setEnabled(en);
        if (en)
            button.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
        else
            button.setTextColor(getContext().getResources().getColor(R.color.disable));
    }

    private void refreshUeShitaEnabled(int pos) {
        ShowLogE("onCreate", "onItemSelected: pos = " + pos);

        setEnabled(mButtonUE, true);
        setEnabled(mButtonSHITA, true);

        if (pos == 0) {
            setEnabled(mButtonUE, false);
            setEnabled(mButtonSHITA, false);
        }
        if (pos == 1)
            setEnabled(mButtonUE, false);
        if (pos == GroupList.size() - 1)
            setEnabled(mButtonSHITA, false);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_GroupDialog_ButtonCancel:
                DismissAndReturn(true);
            break;
            case R.id.id_GroupDialog_ButtonEdit:
                showGroupAddDialog(GroupList.get(GroupListViewClickId));
            break;
            case R.id.id_GroupDialog_ButtonAdd:
                showGroupAddDialog(null);
            break;
            case R.id.id_GroupDialog_ButtonDelete:
                showGroupDeleteDialog(GroupList.get(GroupListViewClickId));
            break;
            case R.id.id_GroupDialog_ButtonUE:
                moveGroupOrder(GroupList.get(GroupListViewClickId), true);
            break;
            case R.id.id_GroupDialog_ButtonSHITA:
                moveGroupOrder(GroupList.get(GroupListViewClickId), false);
            break;
        }
    }

    /**
     * 显示 Group Add Dialog
     * @param inputGroup
     *          null 新分组
     *          notnull 更新分组
     */
    private void showGroupAddDialog(final Group inputGroup) {
        GroupAddDialog dialog = new GroupAddDialog(activity, inputGroup, new GroupAddDialog.OnUpdateGroupListener() {

            @Override
            public void UpdateGroupFinished() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        refreshGroupList();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (inputGroup == null)  // 新分组
                                    GroupListViewClickId = GroupList.size() - 1; // 选择最后一项

                                groupAdapter.setChecked(GroupListViewClickId);
                                GroupListView.setAdapter(groupAdapter); // 必要

                                DismissAndReturn(false);

                            }
                        });
                    }
                }).start();
            }
        });
        dialog.setView(new EditText(getContext()));  //若对话框无法弹出输入法，加上这句话
        dialog.show();
    }

    private void showGroupDeleteDialog(Group inputGroup) {
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
                                GroupListView.setAdapter(groupAdapter); // 必要

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
     *          true: UE, Order--
     *          false: SHITA, Order++
     */
    private void moveGroupOrder(Group currentGroup, boolean isUP) {

        ShowLogE("moveGroupOrder", "currentGroup.order: " + currentGroup.getOrder());

        currentPos = GroupListViewClickId;

        if (isUP && currentPos != 1 && currentPos != 0) { // 上移
            new Thread(new Runnable() {
                @Override
                public void run() {

                    ShowLogE("moveGroupOrder", "UP");
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
        else if (!isUP && currentPos != GroupList.size() - 1 && currentPos != 0) { // 下移
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

    private void moveGroupOrderRe() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // 更新显示
                refreshGroupList(false);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GroupListView.setAdapter(groupAdapter); // 必要
                        groupAdapter.setChecked(currentPos);
                        GroupListViewClickId = currentPos;
                        refreshUeShitaEnabled(GroupListViewClickId);
                        DismissAndReturn(false);

                    }
                });
            }
        }).start();
    }

    private void handleOrder() {
        groupDao.handleOrderDuplicate(groupDao.queryGroupAll());
        groupDao.handleOrderGap();
    }

    /////////////////////////////////////////////////////

    private void DismissAndReturn(boolean isReturn) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                handleOrder();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 不允许 back
                        if (mListener != null)
                            mListener.UpdateGroupFinished(); // 同时令 Note Frac 更新分组信息

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