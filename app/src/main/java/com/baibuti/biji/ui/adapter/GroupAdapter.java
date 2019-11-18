package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.R;

import java.util.List;

public class GroupAdapter extends BaseAdapter {

    private Context context;
    private List<Group> list;

    public GroupAdapter(Context context) {
        this.context = context;
    }

    public List<Group> getList() {
        return list;
    }

    public void setList(List<Group> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public Group getItem(int i) {
        if (i == getCount() || list == null)
            return null;
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {

            view = LayoutInflater.from(context).inflate(R.layout.adapter_group_item, viewGroup, false);

            holder = new ViewHolder();
            holder.GroupName = view.findViewById(R.id.id_adapter_group_name);
            holder.GroupColor = view.findViewById(R.id.id_adapter_group_color);

            view.setTag(holder);
        }
        else
            holder = (ViewHolder) view.getTag();

        holder.GroupName.setText(getItem(position).getName());
        holder.GroupColor.setBackgroundColor(Color.parseColor(getItem(position).getColor()));

        return view;
    }

    public static class ViewHolder {
        TextView GroupName;
        ImageView GroupColor;
    }
}