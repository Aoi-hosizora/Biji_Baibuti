package com.baibuti.biji.UI.Widget.OCRView;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.Net.Models.Region;
import com.baibuti.biji.R;

public class OCRRegionView extends View implements IShowLog {

    private onClickRegionListener m_onClickRegionListener;

    private boolean isChecked = false;

    public static final int OPACITY_CHECKED = 100;
    public static final int OPACITY_UNCHECKED = 20;

    public static final int DEF_WIDTH = 200;
    public static final int DEF_HEIGHT = 100;

    public static final int DEF_BGCOLOR = Color.BLUE;

    private Region.Frame frame;

    public Region.Frame getFrame() {
        return frame;
    }

    public void setFrame(Region.Frame frame) {
        this.frame = frame;
        refreshLayoutBG(false);
    }

    public interface onClickRegionListener {
        /**
         * 点击区域后取消选中时的事件
         * @param frame
         */
        void onClickAfterUp(Region.Frame frame);

        /**
         * 点击区域后选中时的事件
         * @param frame
         */
        void onClickAfterDown(Region.Frame frame);
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "OCRRegionView";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    public OCRRegionView(Context context) {
        this(context, null);
    }

    public OCRRegionView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OCRRegionView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        refreshLayoutBG(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isChecked = !isChecked;

            if (m_onClickRegionListener != null)
                if (isChecked)
                    m_onClickRegionListener.onClickAfterDown(this.frame);
                else
                    m_onClickRegionListener.onClickAfterUp(this.frame);

            refreshLayoutBG(isChecked);
        }
        return super.onTouchEvent(event);
    }

    public void setOnClickRegionListener(onClickRegionListener m_onClickRegionListener) {
        this.m_onClickRegionListener = m_onClickRegionListener;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    /**
     * 点击后更新背景
     * @param isChecked
     */
    private void refreshLayoutBG(boolean isChecked) {
        if (getBackground() == null)
            return;

        if (isChecked)
            getBackground().setAlpha(OPACITY_CHECKED);
        else
            getBackground().setAlpha(OPACITY_UNCHECKED);
    }
}
