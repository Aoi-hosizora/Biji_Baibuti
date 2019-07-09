package com.baibuti.biji.Data.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baibuti.biji.Data.Models.FileClass;
import com.baibuti.biji.R;

import java.util.List;

public class FileClassAdapter extends BaseAdapter {

    public List<FileClass> list;
    LayoutInflater inflater;
    Context context;

    public FileClassAdapter(Context context, List<FileClass> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public FileClass getItem(int i) {
        if (i == getCount() || list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    // 选中当前选项时，让其他选项不被选中
    public void select(int position) {

    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        FileClassAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new FileClassAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.modulelayout_filefrag_fileclasslistitem, viewGroup, false);

            holder.fileClassListItemName = (TextView) convertView.findViewById(R.id.id_adapter_fileclasslistitem_name);

            convertView.setTag(holder);
        } else {
            holder = (FileClassAdapter.ViewHolder) convertView.getTag();
        }

        holder.fileClassListItemName.setText(getItem(position).getFileClassName());

        return convertView;
    }

    public static class ViewHolder {
        public TextView fileClassListItemName;
    }
}
