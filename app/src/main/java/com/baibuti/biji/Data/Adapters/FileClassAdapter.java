package com.baibuti.biji.Data.Adapters;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Data.Models.FileClass;
import com.baibuti.biji.R;

import java.util.List;

public class FileClassAdapter extends BaseAdapter {

    public List<FileClass> list;
    LayoutInflater inflater;
    Context context;
    private boolean firstStart = true;
    private Button lastButton;
    private int lastPosition;

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
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {

        Log.d("test", "getView: enter");

        FileClassAdapter.ViewHolder holder = null;
        if(null == convertView) {
            holder = new FileClassAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.modulelayout_filefrag_fileclasslistitem, viewGroup, false);
            holder.fileClassListItemName = (Button) convertView.findViewById(R.id.id_adapter_fileclasslistitem_name);
            TransitionDrawable buttonTransition = (TransitionDrawable) context.getResources().getDrawable(R.drawable.button_transition);
            holder.fileClassListItemName.setBackground(buttonTransition);
            convertView.setTag(holder);
        }
        else{
            holder = (FileClassAdapter.ViewHolder) convertView.getTag();
            TransitionDrawable buttonTransition = (TransitionDrawable) context.getResources().getDrawable(R.drawable.button_transition);
            holder.fileClassListItemName.setBackground(buttonTransition);
        }

        holder.fileClassListItemName.setText(getItem(position).getFileClassName());

        holder.fileClassListItemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //如果不是"+"按钮
                if(!((Button) view).getText().toString().equals("+")) {
                    //按钮颜色动画
                    TransitionDrawable transition = (TransitionDrawable) view.getBackground();
                    transition.startTransition(200);
                    //上一个按下的按钮恢复颜色
                    try{
                        if (lastPosition >= ((ListView) viewGroup).getFirstVisiblePosition()
                        &&lastPosition <= ((ListView) viewGroup).getLastVisiblePosition()
                        &&lastButton != (Button) view) {
                            if(!lastButton.getText().toString().equals("+")) {
                                TransitionDrawable lastButtonTransition = (TransitionDrawable) lastButton.getBackground();
                                lastButtonTransition.reverseTransition(0);
                            }
                        }
                    }catch(Exception e){
                        Log.e("test_for_onclick", "onClick: error!!!");
                        e.printStackTrace();
                    }

                    lastButton = (Button) view;
                    lastPosition = position;
                    //模拟点击listview item
                    ((ListView) viewGroup).performItemClick(((ListView) viewGroup).getChildAt(position),
                            position,
                            getItemId(position));
                }
                //如果是"+"按钮
                else{
                    //模拟点击listview item
                    ((ListView) viewGroup).performItemClick(((ListView) viewGroup).getChildAt(position),
                            position,
                            getItemId(position));
                }

            }
        });

        if(lastButton == holder.fileClassListItemName && lastPosition == position){
            if(!holder.fileClassListItemName.getText().toString().equals("+")) {
                TransitionDrawable transitionDrawable = (TransitionDrawable) holder.fileClassListItemName.getBackground();
                transitionDrawable.startTransition(0);
            }

        }

        if(firstStart && position == 0) {
            TransitionDrawable transition = (TransitionDrawable) holder.fileClassListItemName.getBackground();
            transition.startTransition(0);
            firstStart = false;
            lastButton = holder.fileClassListItemName;
            lastPosition = 0;
        }

        return convertView;
    }

    public static class ViewHolder {
        public Button fileClassListItemName;
    }
}
