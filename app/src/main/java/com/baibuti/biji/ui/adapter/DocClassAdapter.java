package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.R;

import java.util.List;

public class DocClassAdapter extends BaseAdapter {

    private Context context;
    public List<DocClass> list;

    public DocClassAdapter(Context context, List<DocClass> list) {
        this.list = list;
        this.context = context;
    }

    //////

    private boolean firstStart = true;
    private Button lastButton;
    private int lastPosition;
    public boolean isDeleting = false;

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public DocClass getItem(int i) {
        if (i == getCount() || list == null) {
            return null;
        }
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

            holder = new DocClassAdapter.ViewHolder();
            holder.fileClassListItemName = convertView.findViewById(R.id.id_adapter_fileclasslistitem_name);
            holder.fileClassListItemName.setBackground(context.getResources().getDrawable(R.drawable.button_transition));

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.fileClassListItemName.setText(getItem(position).getName());
        holder.fileClassListItemName.setOnClickListener((v) -> {

            if (!((Button) v).getText().toString().equals("+")) { // 如果不是"+"按钮
                // 按钮颜色动画
                TransitionDrawable transition = (TransitionDrawable) v.getBackground();
                transition.startTransition(200);
                // 上一个按下的按钮恢复颜色
                try {
                    if (lastButton != null
                        && lastPosition >= ((ListView) viewGroup).getFirstVisiblePosition()
                        && lastPosition <= ((ListView) viewGroup).getLastVisiblePosition()
                        && lastButton != v) {
                        if (!lastButton.getText().toString().equals("+")) {
                            TransitionDrawable lastButtonTransition = (TransitionDrawable) lastButton.getBackground();
                            lastButtonTransition.reverseTransition(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                lastButton = (Button) v;
                lastPosition = position;
                ((ListView) viewGroup).performItemClick(viewGroup.getChildAt(position), position, getItemId(position));
            } else // 如果是"+"按钮
                ((ListView) viewGroup).performItemClick(viewGroup.getChildAt(position), position, getItemId(position));
        });

        if (lastButton == holder.fileClassListItemName && lastPosition == position) {
            if (!holder.fileClassListItemName.getText().toString().equals("+") && !isDeleting) {
                TransitionDrawable transitionDrawable = (TransitionDrawable) holder.fileClassListItemName.getBackground();
                transitionDrawable.startTransition(0);
            }
        }

        if (firstStart && position == 0) {
            firstStart = false;
            lastButton = null;
            lastPosition = -1;
        }

        return convertView;
    }

    public class ViewHolder {
        Button fileClassListItemName;
    }
}
