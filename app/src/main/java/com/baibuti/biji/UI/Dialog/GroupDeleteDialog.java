package com.baibuti.biji.UI.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.Data.DB.GroupDao;
import com.baibuti.biji.Data.DB.NoteDao;

public class GroupDeleteDialog {

    private NoteDao noteDao;
    private GroupDao groupDao;
    private Group inputGroup;
    private Context context;
    private OnDeleteGroupListener mListener; //接口

    public interface OnDeleteGroupListener{
        void DeleteGroupFinished(); // 修改引发的事件
    }

    public GroupDeleteDialog(Context context, Group inputGroup, OnDeleteGroupListener mListener) {
        this.context = context;
        this.inputGroup = inputGroup;
        this.noteDao = new NoteDao(context);
        this.groupDao = new GroupDao(context);
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
        if (noteDao.queryNotesAll(inputGroup.getId()).isEmpty())
            HandleDeleteGroup(group); // 无关联
        else
            HandleDeleteNote(group); // 有关联
    }

    /**
     * 无笔记对应，直接删除分组
     */
    private void HandleDeleteGroup(final Group group) {
        android.support.v7.app.AlertDialog deleteDialog = new android.support.v7.app.AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_DeleteGroupAlertTitle)
                .setMessage(String.format(context.getText(R.string.GroupDialog_DeleteGroupAlertMsg).toString(), group.getName()))
                .setNegativeButton(R.string.GroupDialog_DeleteGroupAlertNegativeButtonForCancel, null)
                .setPositiveButton(R.string.GroupDialog_DeleteGroupAlertPositiveButtonForOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            groupDao.deleteGroup(group.getId());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        dialog.dismiss();
                        DismissAndReturn();
                    }
                }).create();

        deleteDialog.show();
    }

    /**
     * 删除分组时判断处理对应的笔记
     */
    private void HandleDeleteNote(final Group group) {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog
                .Builder(context)
                .setTitle(R.string.GroupDialog_DeleteNoteAlertTitle)
                .setMessage(R.string.GroupDialog_DeleteNoteAlertMsg)
                .setNeutralButton(R.string.GroupDialog_DeleteNoteAlertNeutralButtonForNoDelete, null)
                .setPositiveButton(R.string.GroupDialog_DeleteNoteAlertPositiveButtonForDeleteGroupAndModifyToDefaultGroup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除分组并修改为默认分组
                        try {
                            for (Note note : noteDao.queryNotesAll(group.getId())) {
                                // 不更改修改时间修改分组
                                note.setGroupLabel(groupDao.queryDefaultGroup(), false);
                                noteDao.updateNote(note);
                            }

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
                })
                .setNegativeButton(R.string.GroupDialog_DeleteNoteAlertNegativeButtonForDeleteGroupAndNote, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除分组及笔记
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
                        dialog.dismiss();
                        DismissAndReturn();
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
                .Builder(context)
                .setTitle(R.string.GroupDialog_ModifyDefaultAlertTitle)
                .setPositiveButton(R.string.GroupDialog_ModifyDefaultAlertPositiveButtonForOK, null)
                .setMessage(R.string.GroupDialog_ModifyDefaultAlertMsgForDelete);

        dupalert.show();
    }

}
