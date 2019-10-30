package com.baibuti.biji.ui.widget.listView;

import android.support.v7.widget.RecyclerView;

// https://blog.csdn.net/gaoxiaoweiandy/article/details/85016776

public class RecyclerListScrollHelper extends RecyclerView.OnScrollListener {

    private static final int THRESHOLD = 35;
    private int distance = 0;
    private OnShowHideScrollListener m_onShowHideScrollListener;
    private boolean visible = true;

    public interface OnShowHideScrollListener {
       void onHide();
       void onShow();
    }

    public RecyclerListScrollHelper(OnShowHideScrollListener hideScrollListener) {
        // TODO Auto-generated constructor stub
        this.m_onShowHideScrollListener = hideScrollListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (distance > THRESHOLD && visible) {
            m_onShowHideScrollListener.onHide();
            visible = false;
            distance = 0;
        }
        else if (distance<-20 && !visible) {
            m_onShowHideScrollListener.onShow();
            visible = true;
            distance = 0;
        }

        if (visible && dy > 0 || (!visible && dy < 0))
            distance += dy;
    }

}
