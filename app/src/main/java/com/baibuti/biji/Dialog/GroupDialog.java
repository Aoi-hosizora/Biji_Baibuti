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
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;

import java.util.List;

import static com.baibuti.biji.util.CommonUtil.ColorHex_IntEncoding;

public class GroupDialog extends AlertDialog implements OnClickListener, IShowLog {
    private OnUpdateGroupListener mListener; //接口

    private Button mButtonEdit;
    private Button mButtonCancel;
    private Button mButtonAdd;

    private ListView GroupListView;

    /**
     * 当前被选中的项
     */
    private int GroupListViewClickId = -1;

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
        setContentView(R.layout.dialog_mainalert_groupdialog);

        mButtonEdit = (Button)findViewById(R.id.id_GroupDialog_ButtonEdit);
        mButtonCancel = (Button)findViewById(R.id.id_GroupDialog_ButtonCancel);
        mButtonAdd = (Button)findViewById(R.id.id_GroupDialog_ButtonAdd);

        mButtonEdit.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        mButtonAdd.setOnClickListener(this);

        mButtonEdit.setEnabled(false);
        mButtonEdit.setTextColor(ColorHex_IntEncoding("#757575"));

        groupDao = new GroupDao(getContext());
        noteDao = new NoteDao(getContext());

        refreshGroupList();

        GroupListView = (ListView) findViewById(R.id.id_GroupDialog_GroupListView);
        GroupListView.setAdapter(groupAdapter);
        GroupListView.setVisibility(View.VISIBLE);

        GroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupListViewClickId = position;
                mButtonEdit.setEnabled(true);
                mButtonEdit.setTextColor(ColorHex_IntEncoding("#EC407A"));
                showGroupAddDialog(GroupList.get(position)); // 临时
            }
        });

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

    private GroupAdapter groupAdapter;
    private List<Group> GroupList;
    private GroupDao groupDao;
    private NoteDao noteDao;

    /**
     * 刷新列表
     */
    private void refreshGroupList() {
        ShowLogE("refreshGroupList", "DISOYONG");
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
        // GroupListView.setAdapter(groupAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_GroupDialog_ButtonCancel:
                if (mListener != null)
                    mListener.UpdateGroupFinished();
                dismiss();
            break;
            case R.id.id_GroupDialog_ButtonEdit:
                showGroupAddDialog(GroupList.get(GroupListViewClickId));
            break;
            case R.id.id_GroupDialog_ButtonAdd:
                showGroupAddDialog(null);
            break;
        }
    }

    /**
     * 显示 Group Add Dialog
     * @param inputGroup
     *          null 新分组
     *          notnull 更新分组
     */
    private void showGroupAddDialog(Group inputGroup) {
        GroupAddDialog dialog = new GroupAddDialog(getContext(), inputGroup, new GroupAddDialog.OnUpdateGroupListener() {

            @Override
            public void UpdateGroupFinished() {
                refreshGroupList();
                GroupListView.setAdapter(groupAdapter); // 必要

                if (mListener != null)
                    mListener.UpdateGroupFinished(); // 同时令 Note Frac 更新分组信息
            }
        });
        dialog.setView(new EditText(getContext()));  //若对话框无法弹出输入法，加上这句话
        dialog.show();
    }
}