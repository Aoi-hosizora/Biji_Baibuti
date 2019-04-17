package com.baibuti.biji.Data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
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


    // 选中当前选项时，让其他选项不被选中
    public void select(int position) {

    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.modulelayout_groupdialog_grouplistitem, null);

            holder.GroupSelectRadio = (RadioButton) convertView.findViewById(R.id.id_adapter_group_radioButton);
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
        public RadioButton GroupSelectRadio;
        public TextView GroupName;
        public ImageView GroupColor;
    }
}


/*
HashMap<String,Boolean> states=new HashMap<String,Boolean>();//用于记录每个RadioButton的状态，并保证只可选一个
    HashMap<Integer, String> textChange = new HashMap<Integer, String>(); //储存已改变的选项数据

    OnRadioButtonSelect mOnRadioButtonSelect;

//获取字符串接口
    public interface OnRadioButtonSelect{
        void onSelect(String s);
    }

    //////

  final RadioButton raButton = (RadioButton) convertView.findViewById(R.id.radioButton);
        holder.raButton = raButton;

        holder.advice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textChange.put(position, editable.toString());
            }
        });


        //当RadioButton被选中时，将其状态记录进States中，并更新其他RadioButton的状态使它们不被选中
        holder.raButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Toast.makeText(context, list.get(position), Toast.LENGTH_LONG).show();
                //重置，确保最多只有一项被选中
                for(String key:states.keySet()){
                    states.put(key, false);
                }
                //states.put(String.valueOf(position), raButton.isChecked());
                states.put(list.get(position), raButton.isChecked());
                MyAdapter.this.notifyDataSetChanged();
                if(mOnRadioButtonSelect!=null){
                    mOnRadioButtonSelect.onSelect(list.get(position).toString());
                }
            }
        });

        boolean res=false;
        if(states.get(list.get(position)) == null || states.get(list.get(position))== false){
            res=false;
            states.put(list.get(position), false);
        } else
            res = true;

        holder.raButton.setChecked(res);
//////

//----------获取选中的radio的值-----------
    public String getValue() {
        String value = "";
        Iterator iter = states.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();//找到所有key-value对集合
            if(entry.getValue().equals(true)) {//通过判断是否有该value值
                value = (String) entry.getKey();//取得key值
            }
        }

        return value;
    }

 */