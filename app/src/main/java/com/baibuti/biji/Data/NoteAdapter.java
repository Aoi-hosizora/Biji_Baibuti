package com.baibuti.biji.Data;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.modulelayout_notefrag_notelistitem,parent,false);
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
        holder.tv_list_summary.setText(getShortNoteContent(note));
        holder.tv_list_time.setText(note.getUpdateTime_ShortString());
        holder.tv_list_group.setText(note.getGroupLabel().getName());
        holder.tv_list_group.setTextColor(CommonUtil.ColorHex_IntEncoding(note.getGroupLabel().getColor()));
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "###getItemCount: ");
        if (mNotes != null && mNotes.size()>0){
            return mNotes.size();
        }
        return 0;
    }

    private String getShortNoteContent(Note note) {
        String motoContent = note.getContent();
        motoContent = motoContent.replaceAll("<img src=.*" , mContext.getString(R.string.NoteAdapter_ImgStr));
        return motoContent;
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

