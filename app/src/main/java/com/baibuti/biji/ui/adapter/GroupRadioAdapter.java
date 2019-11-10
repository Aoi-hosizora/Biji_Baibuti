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

import java.util.HashMap;
import java.util.List;

public class GroupRadioAdapter extends BaseAdapter {

    private Context context;
    private List<Group> list;
    private OnRadioButtonClickListener onRadioButtonClickListener;

    public GroupRadioAdapter(Context context) {
       this.context = context;
    }

    public List<Group> getList() {
        return list;
    }

    public void setList(List<Group> list) {
        this.list = list;
    }

    public interface OnRadioButtonClickListener {
        void onSelect(int position);
    }

    public OnRadioButtonClickListener getOnRadioButtonClickListener() {
        return onRadioButtonClickListener;
    }

    public void setOnRadioButtonClickListener(OnRadioButtonClickListener onRadioButtonClickListener) {
        this.onRadioButtonClickListener = onRadioButtonClickListener;
    }

    //////

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

    private HashMap<Group, Boolean> states = new HashMap<>();

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.adapter_group_radio_item, viewGroup, false);

            holder = new ViewHolder();
            holder.GroupColor = view.findViewById(R.id.id_adapter_radiogroup_color);
            holder.GroupSelectRadio = view.findViewById(R.id.id_adapter_radiogroup_radioButton);

            view.setTag(holder);
        }
        else
            holder = (ViewHolder) view.getTag();

        holder.GroupSelectRadio.setText(getItem(position).getName());
        holder.GroupColor.setBackgroundColor(Color.parseColor(getItem(position).getColor()));

        final RadioButton raButton = holder.GroupSelectRadio;

        holder.GroupSelectRadio.setOnClickListener((v) -> {
            raButton.setChecked(true);

            for (Group key : states.keySet())
                states.put(key, false);
            states.put(list.get(position), raButton.isChecked());
            GroupRadioAdapter.this.notifyDataSetChanged();

            if (onRadioButtonClickListener != null)
                onRadioButtonClickListener.onSelect(position);
        });

        if (states.get(list.get(position)) == null || !states.get(list.get(position))) {
            holder.GroupSelectRadio.setChecked(false);
            states.put(list.get(position), false);
        } else {
            holder.GroupSelectRadio.setChecked(true);
        }

        return view;
    }

    /**
     * 选择项
     */
    public void setChecked(int position) {
        states.put(list.get(position), true);
    }

    /**
     * 选择项
     */
    public void setChecked(Group group) {
        states.put(group, true);
    }

    public static class ViewHolder {
        RadioButton GroupSelectRadio;
        ImageView GroupColor;
    }
}