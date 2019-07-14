package com.baibuti.biji.UI.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.RainbowPalette;
import com.baibuti.biji.Data.DB.GroupDao;
import com.baibuti.biji.Data.DB.NoteDao;
import com.baibuti.biji.Utils.CommonUtil;

public class GroupAddDialog extends AlertDialog implements OnClickListener, IShowLog {
    private OnUpdateGroupListener mListener; //接口

    private EditText editText;
    private TextView colorText;
    private TextView titleText;
    private RainbowPalette colorPalette;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private Group inputGroup;

    private Button mButtonDelete;
    private Button mButtonUpdate;
    private Button mButtonCancel;

    /**
     * NEW_GROUP 新建分组
     * UPDATE_GROUP 更改分组
     */
    private int GROUPFLAG = 0;
    private static final int NEW_GROUP = 0; // 新建分组
    private static final int UPDATE_GROUP = 1; // 更改分组


    public interface OnUpdateGroupListener{
        void UpdateGroupFinished(); // 修改引发的事件
    }

    public GroupAddDialog(Context context, Group inputGroup, OnUpdateGroupListener mListener) {
        super(context);
        this.mListener = mListener;
        this.inputGroup = inputGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_groupadddialog);

        editText = (EditText) findViewById(R.id.id_addgroup_name);
        colorText = (TextView) findViewById(R.id.id_addgroup_colortext);
        titleText = (TextView) findViewById(R.id.id_addgroup_title);
        colorPalette = (RainbowPalette) findViewById(R.id.id_addgroup_colorpalettle);
        colorPalette.setOnChangeListen(new RainbowPalette.OnColorChangedListen() {
            @Override
            public void onColorChange(int color) {
                colorText.setText(getContext().getString(R.string.GroupDialog_AddAlertColorText)+ CommonUtil.ColorInt_HexEncoding(color));
            }
        });

        mButtonDelete = (Button) findViewById(R.id.id_AddGroupDialog_ButtonDelete);
        mButtonUpdate = (Button) findViewById(R.id.id_AddGroupDialog_ButtonUpdate);
        mButtonCancel = (Button) findViewById(R.id.id_AddGroupDialog_ButtonCancel);

        mButtonDelete.setOnClickListener(this);
        mButtonUpdate.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        groupDao = new GroupDao(getContext());
        noteDao = new NoteDao(getContext());
        HandleDisplay(); // 处理显示问题
    }


    /**
     * 处理控件显示，并且指定 GROUPFLAG
     */
    private void HandleDisplay() {
        Group dis = inputGroup;
        if (dis == null) {
            dis = new Group();
            dis.setName("");
            dis.setColor("#FFFFFF");
        }
        editText.setText(dis.getName());
        colorText.setText(getContext().getString(R.string.GroupDialog_AddAlertColorText) + dis.getStringColor());
        colorPalette.setColor(dis.getIntColor());

        if (inputGroup == null) {
            titleText.setText(R.string.GroupDialog_AddAlertTitleForNew);
            GROUPFLAG = NEW_GROUP;
            mButtonDelete.setVisibility(View.GONE);
            // 根据是否为新建分组而显示删除按钮
        }
        else {
            titleText.setText(R.string.GroupDialog_AddAlertTitleForUpdate);
            GROUPFLAG = UPDATE_GROUP;
            mButtonDelete.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 全局设置 Log 格式
     * 用于 static 方法不使用接口
     * @param FunctionName
     * @param Msg
     */
    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "GroupAddDialog";
        Log.e(getContext().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 统一处理返回事件
     */
    private void DismissAndReturn() {
        if (mListener != null)
            mListener.UpdateGroupFinished();
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_AddGroupDialog_ButtonCancel:
                if (mListener != null)
                    mListener.UpdateGroupFinished();
                dismiss();
            break;
            case R.id.id_AddGroupDialog_ButtonUpdate:
                UpdateGroup();
            break;
            case R.id.id_AddGroupDialog_ButtonDelete:
                DeleteGroup();
            break;
        }
    }

    /**
     * 修改分组信息提交
     */
    private void UpdateGroup() {
        String newGroupName = editText.getText().toString();

        int newGroupOrder;
        if (GROUPFLAG == UPDATE_GROUP)
            newGroupOrder = inputGroup.getOrder();
        else
            newGroupOrder = 0;

        String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
        ShowLogE("UpdateGroup", "COLOR: " + newGroupColor);

        // 更改好的分组信息
        final Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);

        // 先判断空标题
        if (newGroupName.isEmpty())
            HandleNullTitle();

        else {
            // 标题非空

            if (GROUPFLAG == NEW_GROUP) {
                // 新建分组

                if (groupDao.checkDuplicate(newGroup, null) != 0)
                    // 新建分组重复
                    HandleDuplicateGroup(newGroup);

                else {
                    // 新建分组不重复
                    try {
                        groupDao.insertGroup(newGroup);
                        DismissAndReturn();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                // 更改分组

                if (!inputGroup.equals(newGroup)) { // 信息有所修改
                    // 不允许更改默认分组名

                    if (Group.GetDefaultGroupName.equals(inputGroup.getName()) && !newGroupName.equals(inputGroup.getName()))
                        HandleModifyDefaultGroup(true);

                    else {
                        // 无修改默认分组名
                        if (groupDao.checkDuplicate(newGroup, inputGroup) != 0)
                            // 修改分组重复
                            HandleDuplicateGroup(newGroup);

                        else {
                            // 修改分组无重复
                            inputGroup.setName(newGroupName);
                            inputGroup.setColor(newGroupColor);
                            inputGroup.setOrder(newGroupOrder);
                            try {
                                groupDao.updateGroup(inputGroup);
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            DismissAndReturn();
                        }
                    }
                }
            }
        }
    }

    /**
     * 弹出删除判断
     */
    private void DeleteGroup() {
        GroupDeleteDialog groupDeleteDialog = new GroupDeleteDialog(getContext(), inputGroup, new GroupDeleteDialog.OnDeleteGroupListener() {
            @Override
            public void DeleteGroupFinished() { // 接口事件
                DismissAndReturn();
            }
        });
        groupDeleteDialog.showDialog();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    // 杂对话框

    /**
     * 空标题提醒对话框
     */
    private void HandleNullTitle() {
        android.support.v7.app.AlertDialog emptyDialog = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.GroupDialog_NullTitleAlertTitle)
                .setMessage(R.string.GroupDialog_NullTitleAlertMsg)
                .setPositiveButton(R.string.GroupDialog_NullTitleAlertPositiveButtonForOK, null)
                .create();
        emptyDialog.show();
    }

    /**
     * 重复分组标题对话框
     * @param newGroup 添加的分组
     */
    private void HandleDuplicateGroup(Group newGroup) {
        android.support.v7.app.AlertDialog dupalert = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.GroupDialog_DuplicateAlertTitle)
                .setMessage(String.format(getContext().getText(R.string.GroupDialog_DuplicateAlertMsg).toString(), newGroup.getName()))
                .setNegativeButton(R.string.GroupDialog_DuplicateAlertOk, null)
                .create();
        dupalert.show();
    }

    /**
     * 修改了默认的分组
     * @param isModify 判断修改还是删除
     */
    private void HandleModifyDefaultGroup(final boolean isModify) {
        android.support.v7.app.AlertDialog.Builder dupalert = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.GroupDialog_ModifyDefaultAlertTitle)
                .setPositiveButton(R.string.GroupDialog_ModifyDefaultAlertPositiveButtonForOK, null);

        if (isModify)
            dupalert.setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForModify);
        else
            dupalert.setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForDelete);

        dupalert.show();
    }
}