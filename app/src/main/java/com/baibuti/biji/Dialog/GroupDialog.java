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

    private static void refreshGroupList() {
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(context, GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    private static AlertDialog groupDialog;
    private static AlertDialog.Builder addGroupNamedialog;

    public static void showModifyGroup() {
        refreshGroupList();

        groupDialog = new AlertDialog
                .Builder(context)
                .setTitle("笔记分类")//设置对话框的标题
                .setNeutralButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ShowAddGroupDialog(null,null);
                    }
                })
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
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

    private static void ShowAddGroupDialog(final Group group, final Group displayGroup) {
        View view = layoutInflater.inflate(R.layout.modifygroup_addgroup_dialog, null);

        final EditText editText = (EditText) view.findViewById(R.id.id_addgroup_name);
        final TextView colorText = (TextView) view.findViewById(R.id.id_addgroup_colortext);
        final RainbowPalette colorPalette = (RainbowPalette) view.findViewById(R.id.id_addgroup_colorpalettle);
        colorPalette.setOnChangeListen(new RainbowPalette.OnColorChangedListen() {
            @Override
            public void onColorChange(int color) {
                colorText.setText("笔记代表颜色："+ CommonUtil.ColorInt_HexEncoding(color));
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
        colorText.setText("笔记代表颜色："+ dis.getColor());
        colorPalette.setColor(CommonUtil.ColorHex_IntEncoding(dis.getColor()));

        // 判断标题

        if (group == null)
            addGroupNamedialog = new AlertDialog.Builder(context).setTitle("添加笔记类型标签");
        else
            addGroupNamedialog = new AlertDialog.Builder(context).setTitle("修改笔记类型标签");


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        addGroupNamedialog.setView(view)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showModifyGroup();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //////////////////////////////////////////////////

                        String newGroupName = editText.getText().toString();
                        int newGroupOrder = 0;
                        String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                        Log.e("COLOR", newGroupColor);

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
                                    if ("默认分组".equals(group.getName()) && !newGroupName.equals(group.getName()))
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
            addGroupNamedialog.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String newGroupName = editText.getText().toString();
                    int newGroupOrder = 0;
                    String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                    Log.e("COLOR", newGroupColor);

                    // 更改好的分组信息
                    final Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);


                    //////////////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////


                    if ("默认分组".equals(group.getName()))
                        // 删除默认分组
                        ShowModifyDefaultGroupDialog(dialog, group, newGroup, false);
                    else
                        // 删除普通分组，判断关联
                        ShowDeleteGroupFileDialog(dialog, group, newGroup);
                }
            });
        }

        addGroupNamedialog.show();
    }

    private static void ShowNullTitleDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog emptyDialog = new AlertDialog
                .Builder(context)
                .setTitle("错误")
                .setMessage("没有输入分组名，请补全内容")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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

    private static void ShowDuplicateDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setTitle("错误")
                .setMessage("标签 "+ newGroup.getName() +" 已存在，请修改。")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(group, newGroup);
                        dialog.dismiss();
                    }
                }).create();

        dialog.dismiss();
        alertDialog.show();
    }

    private static void ShowModifyDefaultGroupDialog(DialogInterface dialog, final Group group,final Group newGroup, final boolean isModify) {
        AlertDialog.Builder deleteDialog = new AlertDialog
                .Builder(context)
                .setTitle("提示")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
            deleteDialog.setMessage("不允许修改默认分组。");
        else
            deleteDialog.setMessage("不允许删除默认分组。");

        dialog.dismiss();
        deleteDialog.show();
    }

    private static void ShowDeleteGroupFileDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        Log.e("0", "ShowDeleteGroupFileDialog: "+ noteDao.queryNotesAll(group.getId()) );
        Log.e("0", "ShowDeleteGroupFileDialog: "+ group.getId() );
        if (noteDao.queryNotesAll(group.getId()).isEmpty())
            ShowDeleteGroupDialog(dialog, group, newGroup); // 无关联
        else
            ShowDeleteFileDialog(dialog, group, newGroup); // 有关联
    }

    private static void ShowDeleteGroupDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog deleteDialog = new AlertDialog
            .Builder(context)
            .setTitle("提示")
            .setMessage("确定要删除分组 " + group.getName() + " 吗")
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ShowAddGroupDialog(group, newGroup);
                    dialog.dismiss();
                }
            })
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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

    private static void ShowDeleteFileDialog(DialogInterface dialog, final Group group,final Group newGroup) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(context)
                .setTitle("删除")
                .setMessage("该分组有相关联的笔记，是否更改与该分组对应的笔记？")
                .setNeutralButton("不删除分组", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShowAddGroupDialog(group, newGroup);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("删除分组并修改为默认分组", new DialogInterface.OnClickListener() {
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
                .setNegativeButton("删除分组及笔记", new DialogInterface.OnClickListener() {
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
