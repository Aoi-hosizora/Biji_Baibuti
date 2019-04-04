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
import com.baibuti.biji.R;
import com.baibuti.biji.RainbowPalette;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.util.CommonUtil;

import java.util.List;

public class GroupDialog {

    private static Context context;
    private static GroupAdapter groupAdapter;
    private static List<Group> GroupList;
    private static GroupDao groupDao;
    private static LayoutInflater layoutInflater; // getLayoutInflater().inflate(R.layout.modifygroup_addgroup_dialog, null);

    private static GroupDialog GroupDialogInstance;

    private GroupDialog() {}

    public static GroupDialog setupGroupDialog(Context context, GroupAdapter groupAdapter, List<Group> GroupList, GroupDao groupDao, LayoutInflater layoutInflater) {
        if (GroupDialogInstance == null) {
            GroupDialogInstance = new GroupDialog();
            GroupDialogInstance.context = context;
            GroupDialogInstance.groupAdapter = groupAdapter;
            GroupDialogInstance.GroupList = GroupList;
            GroupDialogInstance.groupDao = groupDao;
            GroupDialogInstance.layoutInflater = layoutInflater;
        }
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
                        ShowAddGroupDialog(null);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setSingleChoiceItems(groupAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ShowAddGroupDialog(GroupList.get(which));
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

    private static void ShowAddGroupDialog(final Group group) {
        View view = layoutInflater.inflate(R.layout.modifygroup_addgroup_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.id_addgroup_name);
        final TextView colorText = (TextView) view.findViewById(R.id.id_addgroup_colortext);
        final RainbowPalette colorPalette = (RainbowPalette) view.findViewById(R.id.id_addgroup_colorpalettle);

        if (group != null) {
            editText.setText(group.getName());
            colorText.setText("笔记代表颜色："+ group.getColor());
            colorPalette.setColor(CommonUtil.ColorHex_IntEncoding(group.getColor()));
        } else {
            colorText.setText("笔记代表颜色：#FFFFFF");
            colorPalette.setColor(CommonUtil.ColorHex_IntEncoding("#FFFFFF"));
        }

        colorPalette.setOnChangeListen(new RainbowPalette.OnColorChangedListen() {
            @Override
            public void onColorChange(int color) {
                colorText.setText("笔记代表颜色："+ CommonUtil.ColorInt_HexEncoding(color));
            }
        });

        addGroupNamedialog = new AlertDialog
                .Builder(context)
                .setTitle("添加笔记类型标签")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newGroupName = editText.getText().toString();
                        int newGroupOrder = 0;
                        String newGroupColor = CommonUtil.ColorInt_HexEncoding(colorPalette.getColor());
                        Log.e("COLOR", newGroupColor);


                        if (newGroupName.isEmpty()) {
                            AlertDialog emptyDialog = new AlertDialog
                                    .Builder(context)
                                    .setTitle("没有输入类型，请补全内容")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            ShowAddGroupDialog(group);
                                        }
                                    }).create();
                            dialog.dismiss();
                            emptyDialog.show();
                        }
                        else {
                            if (group == null) {
                                Group newGroup = new Group(newGroupName, newGroupOrder, newGroupColor);
                                try {
                                    groupDao.insertGroup(newGroup);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            else {
                                group.setName(newGroupName);
                                group.setColor(newGroupColor);
                                group.setOrder(newGroupOrder);
                                try {
                                    groupDao.updateGroup(group);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            /// DB Finish
                            dialog.cancel();
                        }
                    }
                });

        if (group != null) {
            addGroupNamedialog.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog deleteDialog = new AlertDialog
                            .Builder(context)
                            .setTitle("提示")
                            .setMessage("确定要删除类型 " + group.getName() + " 吗")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ShowAddGroupDialog(group);
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
                                    dialog.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    refreshGroupList();
                                    showModifyGroup();
                                }
                            }).create();

                    dialog.dismiss();
                    deleteDialog.show();
                }
            });
        }

        addGroupNamedialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                refreshGroupList();
                showModifyGroup();
            }
        });

        addGroupNamedialog.show();

    }

}
