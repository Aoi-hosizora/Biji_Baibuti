package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.UiThread;

import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.model.dao.db.GroupDao;
import com.baibuti.biji.model.dao.db.NoteDao;

import java.util.List;

public class GroupDeleteDialog {

    private NoteDao noteDao;
    private GroupDao groupDao;
    private Group inputGroup;
    private Activity activity;
    private OnDeleteGroupListener mListener; //接口

    public interface OnDeleteGroupListener{
        void DeleteGroupFinished(); // 修改引发的事件
    }

    public GroupDeleteDialog(Activity activity, Group inputGroup, OnDeleteGroupListener mListener) {
        this.activity = activity;
        this.inputGroup = inputGroup;
        this.noteDao = new NoteDao(activity);
        this.groupDao = new GroupDao(activity);
        this.mListener = mListener;
    }

    public void showDialog() {
        DeleteGroup();
    }

    private void DismissAndReturn() {

        if (mListener != null) {
            mListener.DeleteGroupFinished();
        }
    }

    /**
     * 删除分组处理
     */
    private void DeleteGroup() {
        if (Group.GetDefaultGroupName.equals(inputGroup.getName()))
            // 删除默认分组
            HandleDeleteDefaultGroup();
        else
            // 删除普通分组，判断关联
            HandleDeleteGroupNote(inputGroup);
    }

    /**
     * 删除分组的判断，是否要删除对应的笔记
     */
    private void HandleDeleteGroupNote( final Group group) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (noteDao.queryNotesByGroupId(inputGroup.getId()).isEmpty())
                    // TODO
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            HandleDeleteGroup(group); // 无关联
                        }
                    });
                else
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            HandleDeleteNote(group); // 有关联
                        }
                    });
            }
        }).start();
    }

    /**
     * 无笔记对应，直接删除分组
     */
    @UiThread
    private void HandleDeleteGroup(final Group group) {
        android.support.v7.app.AlertDialog deleteDialog = new android.support.v7.app.AlertDialog
                .Builder(activity)
                .setTitle(R.string.GroupDialog_DeleteGroupAlertTitle)
                .setMessage(String.format(activity.getText(R.string.GroupDialog_DeleteGroupAlertMsg).toString(), group.getName()))
                .setNegativeButton(R.string.GroupDialog_DeleteGroupAlertNegativeButtonForCancel, null)
                .setPositiveButton(R.string.GroupDialog_DeleteGroupAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        groupDao.deleteGroup(group.getId());
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();

                        dialog.dismiss();
                        DismissAndReturn();
                    }
                }).create();

        deleteDialog.show();
    }

    /**
     * 删除分组时判断处理对应的笔记
     */
    @UiThread
    private void HandleDeleteNote(final Group group) {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog
                .Builder(activity)
                .setTitle(R.string.GroupDialog_DeleteNoteAlertTitle)
                .setMessage(R.string.GroupDialog_DeleteNoteAlertMsg)
                .setNeutralButton(R.string.GroupDialog_DeleteNoteAlertNeutralButtonForNoDelete, null)
                .setPositiveButton(R.string.GroupDialog_DeleteNoteAlertPositiveButtonForDeleteGroupAndModifyToDefaultGroup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除分组并修改为默认分组
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // TODO !!!!
                                    List<Note> notes = noteDao.queryNotesByGroupId(group.getId());
                                    for (Note note : notes) {
                                        // 不更改修改时间修改分组
                                        note.setGroup(groupDao.queryDefaultGroup(), false);
                                        noteDao.updateNote(note);
                                    }

                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                //////////////////////////////////////////////////
                                try {
                                    groupDao.deleteGroup(group.getId());
                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                dialog.dismiss();
                                DismissAndReturn();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.GroupDialog_DeleteNoteAlertNegativeButtonForDeleteGroupAndNote, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除分组及笔记

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // TODO !!!!
                                    List<Note> notes = noteDao.queryNotesByGroupId(group.getId());
                                    for (Note note : notes)
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
                                dialog.dismiss();
                                DismissAndReturn();
                            }
                        }).start();
                    }
                })
                .create();

        alertDialog.show();
    }

    /**
     * 修改了默认的分组
     */
    private void HandleDeleteDefaultGroup() {
        android.support.v7.app.AlertDialog.Builder dupalert = new android.support.v7.app.AlertDialog
                .Builder(activity)
                .setTitle(R.string.GroupDialog_ModifyDefaultAlertTitle)
                .setPositiveButton(R.string.GroupDialog_ModifyDefaultAlertPositiveButtonForOK, null)
                .setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForDelete);

        dupalert.show();
    }

}
