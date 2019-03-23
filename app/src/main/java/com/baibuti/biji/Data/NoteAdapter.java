package com.baibuti.biji.Data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Fragment.NoteFragment;
import com.baibuti.biji.R;
import com.baibuti.biji.db.NoteDao;

import java.util.List;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class NoteAdapter extends ArrayAdapter<Note> {

    private int resourceId;
    private Fragment fragment;

    public NoteAdapter(Context context, int textViewResourceId, List<Note> objects, Fragment fragment) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.fragment = fragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Note note = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView Title = (TextView) view.findViewById(R.id.id_notelistview_title);
        TextView MakeTime = (TextView) view.findViewById(R.id.id_notelistview_maketime);
        TextView Type = (TextView) view.findViewById(R.id.id_notelistview_type);

        Title.setText(note.getTitle());
        MakeTime.setText(note.getUpdateTime_ShortString());

        CardView cardview = (CardView) view.findViewById(R.id.tab_note_card);
        cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), ViewModifyNoteActivity.class);
                intent.putExtra("notedata",note);
                intent.putExtra("notepos", position);
                intent.putExtra("flag",1); // UPDATE
                fragment.startActivityForResult(intent,1); // 1 from CardView
            }
        });



//        final Note DeletedNoteTmp = new Note(note);
//        final NoteDao noteDao = new NoteDao(getContext());
//
//        cardview.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setTitle("提示");
//                builder.setMessage("确定删除笔记？");
//                builder.setCancelable(false);
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        int ret = noteDao.deleteNote(note.getId());
//                        if (ret > 0){
//                            Snackbar.make(null, "删除成功", Snackbar.LENGTH_SHORT).setAction("撤销删除", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    long noteId = noteDao.insertNote(DeletedNoteTmp);
//                                    DeletedNoteTmp.setId((int)noteId);
//                                }
//                            }).show();
//                        }
//                    }
//                });
//                builder.setNegativeButton("取消", null);
//                builder.create().show();
//
//                return false;
//            }
//        });
        return view;
    }
}
