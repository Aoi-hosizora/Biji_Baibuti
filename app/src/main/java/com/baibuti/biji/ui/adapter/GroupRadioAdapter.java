package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.R;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Single Checkable
 */
public class GroupRadioAdapter extends BaseAdapter {

    private Context context;

    @Getter @Setter
    private List<Group> groupList;

    @Getter
    private Group currentItem;

    public void setCurrentItem(Group currentItem) {
        this.currentItem = currentItem;
        notifyDataSetChanged();
    }

    public GroupRadioAdapter(Context context) {
       this.context = context;
    }

    public interface OnRadioButtonClickListener {
        /**
         * 选择了某项
         */
        void onSelect(int position);
    }

    @Setter
    private OnRadioButtonClickListener onRadioButtonClickListener;

    ////////////////

    @Override
    public int getCount() {
        if (groupList == null)
            return 0;
        return groupList.size();
    }

    @Override
    public Group getItem(int i) {
        if (i >= getCount() || groupList == null)
            return null;
        return groupList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    ////////////////

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.adapter_group_radio_item, viewGroup, false);

            holder = new ViewHolder();
            holder.m_img = view.findViewById(R.id.id_adapter_radiogroup_color);
            holder.m_btn_radio = view.findViewById(R.id.id_adapter_radiogroup_radioButton);

            view.setTag(holder);
        }
        else
            holder = (ViewHolder) view.getTag();

        ////////////

        // Text Color
        holder.m_btn_radio.setText(getItem(position).getName());
        holder.m_img.setBackgroundColor(Color.parseColor(getItem(position).getColor()));

        // State
        holder.m_btn_radio.setChecked(currentItem == groupList.get(position));

        // Event
        holder.m_btn_radio.setOnClickListener((v) -> {
            currentItem = groupList.get(position);
            if (onRadioButtonClickListener != null)
                onRadioButtonClickListener.onSelect(position);

            notifyDataSetChanged();
        });
        return view;
    }

    public static class ViewHolder {
        RadioButton m_btn_radio;
        ImageView m_img;
    }
}