package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerViewEmptySupport.Adapter<NoteAdapter.ViewHolder>
    implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private List<Note> noteList;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public NoteAdapter(Context context) {
        this.context = context;
        this.noteList = new ArrayList<>();
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    // public List<Note> getNoteList() {
    //     return noteList;
    // }

    ///

    public interface OnItemClickListener {
        void onItemClick(View view, Note note);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Note note);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(v, (Note) v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        if (onItemLongClickListener != null)
            return onItemLongClickListener.onItemLongClick(v, (Note) v.getTag());
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.adapter_note_item, parent,false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Note note = noteList.get(position);
        holder.itemView.setTag(note);

        holder.m_titleTextView.setText(note.getTitle());
        holder.m_contentTextView.setText(note.getContent().replaceAll("<img src=.*" , "[图片]"));
        holder.n_timeTextView.setText(note.getUpdateTime_ShortString());
        holder.m_groupTextView.setText(note.getGroup().getName());
        holder.m_groupTextView.setTextColor(DateColorUtil.ColorHex_IntEncoding(note.getGroup().getColor()));
    }

    @Override
    public int getItemCount() {
        if (noteList == null)
            return 0;
        return noteList.size();
    }

    public class ViewHolder extends RecyclerViewEmptySupport.ViewHolder {

        TextView m_titleTextView;
        TextView m_contentTextView;
        TextView n_timeTextView;
        TextView m_groupTextView;

        ViewHolder(View view) {
            super(view);

            m_titleTextView = view.findViewById(R.id.tv_list_title);
            m_contentTextView = view.findViewById(R.id.tv_list_summary);
            n_timeTextView = view.findViewById(R.id.tv_list_time);
            m_groupTextView = view.findViewById(R.id.tv_list_group);
        }
    }
}

