package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.R;

import java.util.List;

/**
 * Single Checkable
 */
public class DocClassAdapter extends BaseAdapter {

    private Context context;
    private List<DocClass> list;
    private DocClass currentItem;

    public DocClassAdapter(Context context) {
        this.context = context;
    }

    public List<DocClass> getList() {
        return list;
    }

    public void setList(List<DocClass> list) {
        this.list = list;
        if (this.list.size() > 0)
            currentItem = this.list.get(0);
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public DocClass getItem(int i) {
        if (i >= getCount() || list == null)
            return null;
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //////

    @Override
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {
        ViewHolder holder;

        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_docclass, viewGroup, false);

            holder = new ViewHolder();
            holder.btn_docClass = convertView.findViewById(R.id.id_adapter_fileclasslistitem_name);
            holder.btn_docClass.setBackground(context.getResources().getDrawable(R.drawable.button_transition));

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.btn_docClass.setOnClickListener((v) -> currentItem = list.get(position));

        String itemName = getItem(position).getName();
        holder.btn_docClass.setText(itemName);
        TransitionDrawable transition = (TransitionDrawable) holder.btn_docClass.getBackground();

        // TODO
        if (currentItem != null) {
            if (itemName.equals(currentItem.getName()))
                transition.startTransition(0);
            else
                transition.startTransition(200);
        }

        return convertView;
    }

    public class ViewHolder {
        Button btn_docClass;
    }
}
