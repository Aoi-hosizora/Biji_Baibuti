package com.baibuti.biji.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.RainbowPalette;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;

import java.util.List;

public class GroupDialog {

    private static Context context;
    private static GroupAdapter groupAdapter;
    private static List<Group> GroupList;

    private static GroupDao groupDao;
    private static NoteDao noteDao;

    private static LayoutInflater layoutInflater; // getLayoutInflater().inflate(R.layout.modifygroup_addgroup_dialog, null);

    private static GroupDialog GroupDialogInstance;

    private GroupDialog() {}

    /**
     * 设置全局 Group Dialog
     *
     * @param context
     * @param groupAdapter
     * @param GroupList
     * @param groupDao
     * @param noteDao
     * @param layoutInflater
     * @return
     */
    public static GroupDialog setupGroupDialog(Context context, GroupAdapter groupAdapter, List<Group> GroupList, GroupDao groupDao, NoteDao noteDao, LayoutInflater layoutInflater) {
        if (GroupDialogInstance == null) {
            GroupDialogInstance = new GroupDialog();
            GroupDialogInstance.context = context;
            GroupDialogInstance.groupAdapter = groupAdapter;
            GroupDialogInstance.GroupList = GroupList;
            GroupDialogInstance.groupDao = groupDao;
            GroupDialogInstance.noteDao = noteDao;
            GroupDialogInstance.layoutInflater = layoutInflater;
        }
        GroupDialogInstance.context = context;
            return GroupDialogInstance;
    }

    /**
     * 全局设置 Log 格式
     * 用于 static 方法不使用接口
     * @param FunctionName
     * @param Msg
     */
    private static void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "GroupDialog";
        Log.e(context.getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 刷新列表
     */
    private static void refreshGroupList() {
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(context, GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    private static AlertDialog groupDialog;
    private static AlertDialog.Builder addGroupNamedialog;

    /**
     * 显示首个对话框，Modify Group
     */
    public static void showModifyGroup() {
        refreshGroupList();

        groupDialog = new AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_ModifyAlertTitle)//设置对话框的标题
                .setNeutralButton(R.string.GroupDialog_ModifyAlertNeutralButtonForAdd, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ShowAddGroupDialog(null,null);
                    }
                })
                .setPositiveButton(R.string.GroupDialog_ModifyAlertPositiveButtonForBack, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setSingleChoiceItems(groupAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Group group = GroupList.get(which);
                        ShowAddGroupDialog(group, group);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        refreshGroupList();
                    }
                }).create();

        groupDialog.show();
    }

    /**
     * 新增分组对话框
     * @param group 分组
     * @param displayGroup 以 displayGroup 优先显示分组
     */
    private static void ShowAddGroupDialog(final Group group, final Group displayGroup) {
        View view = layoutInflater.inflate(R.layout.modifygroup_addgroup_dialog, null);

        final EditText editText = (EditText) view.findViewById(R.id.id_addgroup_name);
        final TextView colorText = (TextView) view.findViewById(R.id.id_addgroup_colortext);
        final RainbowPalette colorPalette = (RainbowPalette) view.findViewById(R.id.id_addgroup_colorpalettle);
        colorPalette.setOnChangeListen(new RainbowPalette.OnColorChangedListen() {
            @Override
            public void onColorChange(int color) {
                colorText.setText(R.string.GroupDialog_AddAlertColorText+ CommonUtil.ColorInt_HexEncoding(color));
            }
        });

        Group dis;

        // 以 displayGroup 优先
        if (displayGroup != null)
            dis = displayGroup;
        else {
            if (group != null)
                dis = group;
            else {
                dis = new Group();
                dis.setName("");
                dis.setColor("#FFFFFF");
            }
        }

        editText.setText(dis.getName());
        colorText.setText(R.string.GroupDialog_AddAlertColorText+ dis.getColor());
        colorPalette.setColor(CommonUtil.ColorHex_IntEncoding(dis.getColor()));

        // 判断标题

        if (group == null)
            addGroupNamedialog = new AlertDialog.Builder(context).setTitle(R.string.GroupDialog_AddAlertTitleForNew);
        else
            addGroupNamedialog = new AlertDialog.Builder(context).setTitle(R.string.GroupDialog_AddAlertTitleForUpdate);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        addGroupNamedialog.setView(view)
                .setCancelable(false)
                .setNegativeButton(R.string.GroupDialog_AddAlertNegativeButtonForCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showModifyGroup();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.GroupDialog_AddAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //////////////////////////////////////////////////

                        String newGroupName = editText.getText().toString();
                        int newGroupOrder = 0;
                        String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                        ShowLogE("ShowAddGroupDialog", "COLOR: " + newGroupColor);

                        // 更改好的分组信息
                        final Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);

                        //////////////////////////////////////////////////

                        // 先判断空标题
                        if (newGroupName.isEmpty())
                            ShowNullTitleDialog(dialog, group, newGroup);

                        else { // 标题非空

                            if (group == null) { // 新建分组

                                int cnt = groupDao.checkDuplicate(newGroup, null);
                                if (cnt == 0) {
                                    // 正常无重复分组
                                    try {
                                        groupDao.insertGroup(newGroup);
                                    }
                                    catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    showModifyGroup();
                                }
                                else // 新建分组重复
                                    ShowDuplicateDialog(dialog, group, newGroup);

                            }
                            else { // 更改分组

                                if (!group.equals(newGroup)) { // 信息有所修改

                                    // 不允许更改默认分组名
                                    if (Group.GetDefaultGroupName.equals(group.getName()) && !newGroupName.equals(group.getName()))
                                        ShowModifyDefaultGroupDialog(dialog, group, newGroup, true);

                                    else { // 无修改默认分组名

                                        int cnt = groupDao.checkDuplicate(newGroup, group);

                                        if (cnt == 0) { // 无重复
                                            group.setName(newGroupName);
                                            group.setColor(newGroupColor);
                                            group.setOrder(newGroupOrder);
                                            try {
                                                groupDao.updateGroup(group);
                                            }
                                            catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            showModifyGroup();
                                            dialog.dismiss();
                                        }
                                        else { // 重复
                                            ShowDuplicateDialog(dialog, group, newGroup);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

        // 增加删除Button
        if (group != null) {
            addGroupNamedialog.setNeutralButton(R.string.GroupDialog_AddAlertNeutralButtonForDelete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String newGroupName = editText.getText().toString();
                    int newGroupOrder = 0;
                    String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                    ShowLogE("ShowAddGroupDialog", "COLOR: "+newGroupColor);

                    // 更改好的分组信息
                    final Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);


                    //////////////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////


                    if (Group.GetDefaultGroupName.equals(group.getName()))
                        // 删除默认分组
                        ShowModifyDefaultGroupDialog(dialog, group, newGroup, false);
                    else
                        // 删除普通分组，判断关联
                        ShowDeleteGroupNoteDialog(dialog, group, newGroup);
                }
            });
        }

        addGroupNamedialog.show();
    }

    /**
     * 空标题提醒对话框
     * @param dialog
     * @param group
     * @param newGroup
     */
    private static void ShowNullTitleDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog emptyDialog = new AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_NullTitleAlertTitle)
                .setMessage(R.string.GroupDialog_NullTitleAlertMsg)
                .setPositiveButton(R.string.GroupDialog_NullTitleAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ShowAddGroupDialog(group, newGroup);
                    }
                }).create();
        dialog.dismiss();
        emptyDialog.show();
    }

    /**
     * 重复分组标题对话框
     * @param dialog
     * @param group 添加的分组
     * @param newGroup 显示的分组
     */
    private static void ShowDuplicateDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_DuplicateAlertTitle)
                .setMessage(String.format(context.getText(R.string.GroupDialog_DuplicateAlertMsg).toString(), newGroup.getName()))
                .setNegativeButton(R.string.GroupDialog_DuplicateAlertOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(group, newGroup);
                        dialog.dismiss();
                    }
                }).create();

        dialog.dismiss();
        alertDialog.show();
    }

    /**
     * 修改了默认的分组
     * @param dialog
     * @param group
     * @param newGroup
     * @param isModify 判断修改还是删除
     */
    private static void ShowModifyDefaultGroupDialog(DialogInterface dialog, final Group group,final Group newGroup, final boolean isModify) {
        AlertDialog.Builder deleteDialog = new AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_ModifyDefaultAlertTitle)
                .setPositiveButton(R.string.GroupDialog_ModifyDefaultAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // showModifyGroup();
                        ShowAddGroupDialog(group, newGroup);
                    }
                });

        if (isModify)
            deleteDialog.setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForModify);
        else
            deleteDialog.setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForDelete);

        dialog.dismiss();
        deleteDialog.show();
    }

    /**
     * 删除分组的判断，是否要删除对应的笔记
     * @param dialog
     * @param group
     * @param newGroup
     */
    private static void ShowDeleteGroupNoteDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        ShowLogE("ShowDeleteGroupNoteDialog", "IsEmpty: " + noteDao.queryNotesAll(group.getId()).isEmpty());
        ShowLogE("ShowDeleteGroupNoteDialog", "GroupID: " + group.getId());
        if (noteDao.queryNotesAll(group.getId()).isEmpty())
            ShowDeleteGroupDialog(dialog, group, newGroup); // 无关联
        else
            ShowDeleteNoteDialog(dialog, group, newGroup); // 有关联
    }

    /**
     * 无笔记对应，直接删除分组
     * @param dialog
     * @param group
     * @param newGroup
     */
    private static void ShowDeleteGroupDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog deleteDialog = new AlertDialog
            .Builder(context)
            .setTitle(R.string.GroupDialog_DeleteGroupAlertTitle)
            .setMessage(String.format(context.getText(R.string.GroupDialog_DeleteGroupAlertMsg).toString(), newGroup.getName()))
            .setNegativeButton(R.string.GroupDialog_DeleteGroupAlertNegativeButtonForCancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ShowAddGroupDialog(group, newGroup);
                    dialog.dismiss();
                }
            })
            .setPositiveButton(R.string.GroupDialog_DeleteGroupAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        groupDao.deleteGroup(group.getId());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    showModifyGroup();
                    dialog.dismiss();
                }
            }).create();

        dialog.dismiss();
        deleteDialog.show();
    }

    /**
     * 删除分组时判断处理对应的笔记
     * @param dialog
     * @param group
     * @param newGroup
     */
    private static void ShowDeleteNoteDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_DeleteNoteAlertTitle)
                .setMessage(R.string.GroupDialog_DeleteNoteAlertMsg)
                .setNeutralButton(R.string.GroupDialog_DeleteNoteAlertNeutralButtonForNoDelete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(group, newGroup);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.GroupDialog_DeleteNoteAlertPositiveButtonForDeleteGroupAndModifyToDefaultGroup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            for (Note note : noteDao.queryNotesAll(group.getId()))
                                note.setGroupLabel(groupDao.queryDefaultGroup());
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        //////////////////////////////////////////////////
                        try {
                            groupDao.deleteGroup(group.getId());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                })
                .setNegativeButton(R.string.GroupDialog_DeleteNoteAlertNegativeButtonForDeleteGroupAndNote, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            for (Note note : noteDao.queryNotesAll(group.getId()))
                                noteDao.deleteNote(note.getId());
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        //////////////////////////////////////////////////
                        try {
                            groupDao.deleteGroup(group.getId());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                })
                .create();

        alertDialog.show();
    }
}
