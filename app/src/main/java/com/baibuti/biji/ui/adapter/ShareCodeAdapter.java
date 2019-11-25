package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.model.po.ShareCodeItem;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ShareCodeAdapter extends RecyclerViewEmptySupport.Adapter<ShareCodeAdapter.ViewHolder>
    implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    @Setter @Getter
    private List<ShareCodeItem> shareCodeItems;

    @Setter
    private OnItemClickListener onItemClickListener;
    @Setter
    private OnItemLongClickListener onItemLongClickListener;

    public ShareCodeAdapter(Context context) {
        this.context = context;
        this.shareCodeItems = new ArrayList<>();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ShareCodeItem shareCodeItem);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, ShareCodeItem shareCodeItem);
    }


    @Override
    public void onClick(View v) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(v, (ShareCodeItem) v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        if (onItemLongClickListener != null)
            onItemLongClickListener.onItemLongClick(v, (ShareCodeItem) v.getTag());
        return true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_note_item, parent,false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShareCodeAdapter.ViewHolder holder, int position) {
        final ShareCodeItem shareCodeItem = shareCodeItems.get(position);
        holder.itemView.setTag(shareCodeItem);
        holder.m_txt.setText(shareCodeItem.getSc());
    }

    @Override
    public int getItemCount() {
        if (shareCodeItems == null)
            return 0;
        return shareCodeItems.size();
    }

    public class ViewHolder extends RecyclerViewEmptySupport.ViewHolder {

        TextView m_txt;

        ViewHolder(View view) {
            super(view);
            m_txt = view.findViewById(R.id.id_sc_adapter_txt);
        }
    }
}

