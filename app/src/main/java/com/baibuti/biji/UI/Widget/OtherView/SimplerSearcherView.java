package com.baibuti.biji.UI.Widget.OtherView;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.baibuti.biji.R;

public class SimplerSearcherView extends AppCompatEditText {

    private int mDrawblePadding = 6;
    private OnSearcherClickListener mOnSearcherClickListener;

    public SimplerSearcherView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SimplerSearcherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        // 设置键盘按钮事件
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // 搜索键
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 隐藏软件盘
                    hideKeyBody();
                    if (mOnSearcherClickListener != null) {
                        mOnSearcherClickListener.onSearcherClick(getText().toString().trim());
                    }
                }

                if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    //关闭搜索
                    clearFocus();
                }
                return false;
            }
        });
    }

    protected void hideKeyBody() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(this, 0);

    }

    public SimplerSearcherView(Context context)
    {
        this(context, null);
    }

    private void init() {
        setSingleLine(true);
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        //设置边框
        setBackgroundResource(R.drawable.shap_search_bg);

        // 设置搜索图标
        Drawable drawableLeft = getResources().getDrawable(R.drawable.search);
        drawableLeft.setBounds(0, 0, drawableLeft.getMinimumWidth(),
                drawableLeft.getMinimumHeight());
        setCompoundDrawables(null, null, drawableLeft, null);

        // 设置图标边距
        mDrawblePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mDrawblePadding, getContext().getResources().getDisplayMetrics());
        setCompoundDrawablePadding(mDrawblePadding);
    }

    public void setOnSearcherClickListener(OnSearcherClickListener onSearcherClickListener) {
        this.mOnSearcherClickListener = onSearcherClickListener;
    }

    public interface OnSearcherClickListener {
        void onSearcherClick(String content);
    }
}
