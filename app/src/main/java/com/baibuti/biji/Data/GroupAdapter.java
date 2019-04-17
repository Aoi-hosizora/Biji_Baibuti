package com.baibuti.biji.Data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.R;

import java.util.List;

public class GroupAdapter extends BaseAdapter {

    public List<Group> list;
    LayoutInflater inflater;

    public GroupAdapter(Context context, List<Group> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Group getItem(int i) {
        if (i == getCount() || list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.modulelayout_groupdialog_grouplistitem, null);

            holder.GroupName = (TextView) convertView.findViewById(R.id.id_adapter_group_name);
            holder.GroupColor = (ImageView) convertView.findViewById(R.id.id_adapter_group_color);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.GroupName.setText(getItem(position).getName());
        holder.GroupColor.setBackgroundColor(Color.parseColor(getItem(position).getColor()));

        return convertView;
    }

    public static class ViewHolder {
        public TextView GroupName;
        public ImageView GroupColor;
    }
}
