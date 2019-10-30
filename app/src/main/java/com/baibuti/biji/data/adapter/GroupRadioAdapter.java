package com.baibuti.biji.data.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.R;

import java.util.HashMap;
import java.util.List;

public class GroupRadioAdapter extends BaseAdapter {

    public List<Group> list;
    LayoutInflater inflater;
    OnRadioButtonSelect mOnRadioButtonSelect;

    public interface OnRadioButtonSelect{
        // void onSelect(Group g);
        void onSelect(int position);
    }

    public GroupRadioAdapter(Context context, List<Group> list, OnRadioButtonSelect mOnRadioButtonSelect) {
        this.list = list;
        inflater = LayoutInflater.from(context);
        this.mOnRadioButtonSelect=mOnRadioButtonSelect;
    }

    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "GroupRadioAdapter";
        Log.e("BijiLogE",
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
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

    public HashMap<Group, Boolean> states = new HashMap<Group, Boolean>();

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.modulelayout_groupdialog_grouplistradioitem, null);
            holder.GroupColor = (ImageView) convertView.findViewById(R.id.id_adapter_radiogroup_color);
            holder.GroupSelectRadio = (RadioButton) convertView.findViewById(R.id.id_adapter_radiogroup_radioButton);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
//        ShowLogE("getView", "position:" + position );
//        ShowLogE("getView", "list:" + list.isEmpty() );
//        ShowLogE("getView", "GroupSelectRadio:" + (holder.GroupSelectRadio == null) );

        holder.GroupSelectRadio.setText(getItem(position).getName());
        holder.GroupColor.setBackgroundColor(Color.parseColor(getItem(position).getColor()));

        final RadioButton raButton = holder.GroupSelectRadio;

        holder.GroupSelectRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                raButton.setChecked(true);

                for(Group key:states.keySet())
                    states.put(key, false);
                states.put(list.get(position), raButton.isChecked());
                GroupRadioAdapter.this.notifyDataSetChanged();

                if (mOnRadioButtonSelect!=null)
                    mOnRadioButtonSelect.onSelect(position);
            }
        });

        boolean res = false;
        if (states.get(list.get(position)) == null || !states.get(list.get(position))) {
            res = false;
            states.put(list.get(position), false);
        } else
            res = true;

        holder.GroupSelectRadio.setChecked(res);

        return convertView;
    }

    /**
     * 选择项
     * @param position
     */
    public void setChecked(int position) {
        states.put(list.get(position), true);
    }

    /**
     * 选择项
     * @param group
     */
    public void setChecked(Group group) {
        states.put(group, true);
    }

    public static class ViewHolder {
        public RadioButton GroupSelectRadio;
        public ImageView GroupColor;
    }
}