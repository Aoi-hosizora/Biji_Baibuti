package com.baibuti.biji.Data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

//public class NoteAdapter extends ArrayAdapter<Note> {
//
//    private int resourceId;
//    private Fragment fragment;
//    private NoteDao noteDao;
//
//    public NoteAdapter(Context context, int textViewResourceId, List<Note> objects, Fragment fragment) {
//        super(context, textViewResourceId, objects);
//        resourceId = textViewResourceId;
//        this.fragment = fragment;
//        this.noteDao = new NoteDao(getContext());
//    }
//
//    @Override
//    public View getView(final int position, final View convertView, final ViewGroup parent) {
//        final Note note = getItem(position);
//        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
//        TextView Title = (TextView) view.findViewById(R.id.id_notelistview_title);
//        TextView MakeTime = (TextView) view.findViewById(R.id.id_notelistview_maketime);
//        TextView Type = (TextView) view.findViewById(R.id.id_notelistview_type);
//
//        Title.setText(note.getTitle());
//        MakeTime.setText(note.getUpdateTime_ShortString());
//
//        CardView cardview = (CardView) view.findViewById(R.id.tab_note_card);
//        cardview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent=new Intent(getContext(), ViewModifyNoteActivity.class);
//                intent.putExtra("notedata",note);
//                intent.putExtra("notepos", position);
//                intent.putExtra("flag",1); // UPDATE
//                fragment.startActivityForResult(intent,1); // 1 from CardView
//            }
//        });
//
//
//
////        final Note DeletedNoteTmp = new Note(note);
////        final NoteDao noteDao = new NoteDao(getContext());
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
//                        if (ret > 0) {
//                             Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
//
////                            Snackbar.make(getView(position, convertView, parent), "删除成功", Snackbar.LENGTH_SHORT).setAction("撤销删除", new View.OnClickListener() {
////                                @Override
////                                public void onClick(View v) {
////                                    long noteId = noteDao.insertNote(DeletedNoteTmp);
////                                    DeletedNoteTmp.setId((int)noteId);
////                                }
////                            }).show();
//                        }
//                    }
//                });
//                builder.setNegativeButton("取消", null);
//                builder.create().show();
//
//                return false;
//            }
//        });
//        return view;
//    }
//
//}

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private List<Note> mNotes;
    private OnRecyclerViewItemClickListener mOnItemClickListener ;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener ;


    private OnItemClickListener mClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int postion);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }



    public NoteAdapter() {
        mNotes = new ArrayList<>();
    }

    public void setmNotes(List<Note> notes) {
        this.mNotes = notes;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Note)v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v,(Note)v.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Note note);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view , Note note);
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.i(TAG, "###onCreateViewHolder: ");
        //inflate(R.layout.list_item_record,parent,false) 如果不这么写，cardview不能适应宽度
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_note,parent,false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Log.i(TAG, "###onBindViewHolder: ");
        final Note note = mNotes.get(position);
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(note);
        //Log.e("adapter", "###record="+record);
        holder.tv_list_title.setText(note.getTitle());
        holder.tv_list_summary.setText(note.getContent());
        holder.tv_list_time.setText(note.getUpdateTime_ShortString());
        holder.tv_list_group.setText(note.getGroupLabel().getName());
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "###getItemCount: ");
        if (mNotes != null && mNotes.size()>0){
            return mNotes.size();
        }
        return 0;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tv_list_title;//笔记标题
        public TextView tv_list_summary;//笔记摘要
        public TextView tv_list_time;//创建时间
        public TextView tv_list_group;//笔记分类
        public CardView card_view_note;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);

            mListener = listener;
            itemView.setOnClickListener(this);


            card_view_note = (CardView) view.findViewById(R.id.card_view_note);
            tv_list_title = (TextView) view.findViewById(R.id.tv_list_title);
            tv_list_summary = (TextView) view.findViewById(R.id.tv_list_summary);
            tv_list_time = (TextView) view.findViewById(R.id.tv_list_time);
            tv_list_group = (TextView) view.findViewById(R.id.tv_list_group);
        }

        private OnItemClickListener mListener;

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            mListener.onItemClick(v, getPosition());
        }
    }
}

