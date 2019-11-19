package com.baibuti.biji.ui.widget.listView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class RecyclerViewEmptySupport extends RecyclerView {

    // private static final String TAG = "RecyclerViewEmptySupport";

    private View mEmptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        // @SuppressLint("LongLogTag")
        @Override
        public void onChanged() {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {

                if (adapter.getItemCount() == 0) {
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    // @SuppressLint("LongLogTag")
    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        // Log.i(TAG, "setAdapter: adapter::" + adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param emptyView 展示的空view
     */
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }

}