package com.baibuti.biji.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupRadioAdapter;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;

import java.util.Collections;
import java.util.List;

public class GroupDialog extends AlertDialog implements OnClickListener, IShowLog {
    private OnUpdateGroupListener mListener; //接口

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
    }

    public GroupDialog(Context context, OnUpdateGroupListener mListener) {
        super(context);
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

        handleOrder();
        refreshGroupList();

        setEnabled(mButtonUE, false);
        setEnabled(mButtonSHITA, false);

        groupAdapter.setValue(GroupListViewClickId);

        GroupListView = (ListView) findViewById(R.id.id_GroupDialog_GroupListView);
        GroupListView.setAdapter(groupAdapter);
        GroupListView.setVisibility(View.VISIBLE);

    }

    /**
     * 全局设置 Log 格式
     * 用于 static 方法不使用接口
     * @param FunctionName
     * @param Msg
     */
    @Override
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
     * 刷新列表
     */
    private void refreshGroupList() {
        GroupList = groupDao.queryGroupAll();
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

    private void setEnabled(Button button, boolean en) {
        button.setEnabled(en);
        if (en)
            button.setTextColor(getContext().getResources().getColor(R.color.pink));
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
        GroupAddDialog dialog = new GroupAddDialog(getContext(), inputGroup, new GroupAddDialog.OnUpdateGroupListener() {

            @Override
            public void UpdateGroupFinished() {
                refreshGroupList();

                if (inputGroup == null)  // 新分组
                    GroupListViewClickId = GroupList.size() - 1; // 选择最后一项

                groupAdapter.setValue(GroupListViewClickId);
                GroupListView.setAdapter(groupAdapter); // 必要

                DismissAndReturn(false);

            }
        });
        dialog.setView(new EditText(getContext()));  //若对话框无法弹出输入法，加上这句话
        dialog.show();
    }

    private void showGroupDeleteDialog(Group inputGroup) {
        GroupDeleteDialog groupDeleteDialog = new GroupDeleteDialog(getContext(), inputGroup, new GroupDeleteDialog.OnDeleteGroupListener() {
            @Override
            public void DeleteGroupFinished() {
                refreshGroupList();
                GroupListViewClickId--;
                groupAdapter.setValue(GroupListViewClickId);
                GroupListView.setAdapter(groupAdapter); // 必要

                DismissAndReturn(false);
            }
        });
        groupDeleteDialog.showDialog();
    }

    /**
     * 移动分组 Order
     * @param currentGroup 当前分组
     * @param isUP
     *          true: UE, Order--
     *          false: SHITA, Order++
     */
    private void moveGroupOrder(Group currentGroup, boolean isUP) {

        ShowLogE("moveGroupOrder", "currentGroup.order: " + currentGroup.getOrder());

        int currentpos = GroupListViewClickId;

        if (isUP && currentpos != 1 && currentpos != 0) { // 上移
            ShowLogE("moveGroupOrder", "UP");
            int motoorder = currentGroup.getOrder();

            currentGroup.setOrder(motoorder - 1);
            Group upGroup = groupDao.queryGroupByOrder(motoorder - 1);
            if (upGroup != null) {
                upGroup.setOrder(motoorder);
                groupDao.updateGroup(upGroup);
            }

            currentpos--;

            groupDao.updateGroup(currentGroup); // 更新数据库
//            groupDao.handleOrderDuplicateWhenUpdate(currentGroup); // 处理重复 order
//            groupDao.handleOrderGap(); // 处理错误Gap
        }
        else if (!isUP && currentpos != GroupList.size() - 1 && currentpos != 0) { // 下移
            ShowLogE("moveGroupOrder", "DOWN");
            int motoorder = currentGroup.getOrder();

            currentGroup.setOrder(motoorder + 1);
            Group downGroup = groupDao.queryGroupByOrder(motoorder + 1); // 向下交换
            if (downGroup != null) {
                downGroup.setOrder(motoorder);
                groupDao.updateGroup(downGroup);
            }

            currentpos++;

            groupDao.updateGroup(currentGroup); // 更新数据库
//            groupDao.handleOrderDuplicateWhenUpdate(currentGroup); // 处理重复 order
//            groupDao.handleOrderGap(); // 处理错误Gap
        }
        else {
            return;
        }



        // 更新显示
        refreshGroupList();
        GroupListView.setAdapter(groupAdapter); // 必要
        groupAdapter.setValue(currentpos);
        GroupListViewClickId = currentpos;
        refreshUeShitaEnabled(GroupListViewClickId);
        DismissAndReturn(false);
    }

    private void handleOrder() {
        groupDao.handleOrderDuplicate(groupDao.queryGroupAll());
        groupDao.handleOrderGap();
    }

    /////////////////////////////////////////////////////

    private void DismissAndReturn(boolean isReturn) {
        handleOrder();

        if (mListener != null)
            mListener.UpdateGroupFinished(); // 同时令 Note Frac 更新分组信息

        if (isReturn)
            dismiss();
    }
}