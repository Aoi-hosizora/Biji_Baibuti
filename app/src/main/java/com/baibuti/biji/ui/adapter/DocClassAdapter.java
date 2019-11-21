package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.R;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Single Checkable
 */
public class DocClassAdapter extends BaseAdapter {

    private Context context;

    @Getter @Setter
    private List<DocClass> docClassList;

    @Getter
    private DocClass currentItem;

    public DocClassAdapter(Context context) {
        this.context = context;
    }

    public void setCurrentItem(DocClass currentItem) {
        this.currentItem = currentItem;
        notifyDataSetChanged();
    }

    public int getCurrentIndex() {
        if (currentItem == null)
            return -1;
        return docClassList.indexOf(currentItem);
    }

    public interface OnButtonClickListener {
        void onClick(int position);
    }

    public interface OnButtonLongClickListener {
        boolean onLongClick(int position);
    }

    @Setter
    private OnButtonClickListener onButtonClickListener;

    @Setter
    private OnButtonLongClickListener onButtonLongClickListener;

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (this.docClassList != null && this.docClassList.size() > 0)
            currentItem = docClassList.get(0);
    }

    ////////////////

    @Override
    public int getCount() {
        if (docClassList == null)
            return 0;
        return docClassList.size();
    }

    @Override
    public DocClass getItem(int i) {
        if (i >= getCount() || docClassList == null)
            return null;
        return docClassList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    ////////////////

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.adapter_docclass, viewGroup, false);

            holder = new ViewHolder();
            holder.btn_docClass = view.findViewById(R.id.id_adapter_fileclasslistitem_name);

            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        ////////////

        // Text
        holder.btn_docClass.setText(getItem(position).getName());

        // Event
        holder.btn_docClass.setOnClickListener((v) -> {
            currentItem = docClassList.get(position);
            if (onButtonClickListener != null)
                onButtonClickListener.onClick(position);

            notifyDataSetChanged();
        });

        holder.btn_docClass.setOnLongClickListener((v) -> {
            if (onButtonLongClickListener != null)
                return onButtonLongClickListener.onLongClick(position);
            return false;
        });

        // State

        if (getItem(position) == currentItem)
            holder.btn_docClass.setBackground(context.getResources().getDrawable(R.drawable.btn_selected));
        else
            holder.btn_docClass.setBackground(context.getResources().getDrawable(R.drawable.btn_unselected));

        return view;
    }

    private class ViewHolder {
        Button btn_docClass;
    }
}
