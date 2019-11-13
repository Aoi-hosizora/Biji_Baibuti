package com.baibuti.biji.ui.widget.ocrView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.baibuti.biji.service.ocr.dto.OCRFrame;

public class OCRRegionView extends View {

    // DEF
    public static final int DEF_OPACITY_CHECKED = 120;
    public static final int DEF_OPACITY_UNCHECKED = 50;

    public static final int DEF_WIDTH = 200;
    public static final int DEF_HEIGHT = 100;

    public static final int DEF_MAINCOLOR = Color.BLUE;
    public static final int DEF_BORDERCOLOR = Color.YELLOW;
    public static final float DEF_BORDERWIDTH = 3;

    // ATTR
    private int Opacity_Checked = DEF_OPACITY_CHECKED;
    private int Opacity_UnChecked = DEF_OPACITY_UNCHECKED;
    private int MainColor = DEF_MAINCOLOR;
    private int BorderColor = DEF_BORDERCOLOR;
    private float BorderWidth = DEF_BORDERWIDTH;

    // OTHER
    private onClickRegionListener m_onClickRegionListener;

    private boolean isChecked = false;

    private OCRFrame frame;

    private Paint mPaint = new Paint();
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public OCRFrame getFrame() {
        return frame;
    }

    public void setFrame(OCRFrame frame) {
        this.frame = frame;
        refreshLayoutBG(false);
    }

    public interface onClickRegionListener {
        /**
         * 点击区域后取消选中时的事件
         */
        void onClickAfterUp(OCRFrame frame);

        /**
         * 点击区域后选中时的事件
         */
        void onClickAfterDown(OCRFrame frame);
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

    /**
     * 绘制边框
     */
    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(BorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(BorderWidth);
        mPaint.setAntiAlias(true);

        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        super.onDraw(canvas);
    }

    public void setOnClickRegionListener(onClickRegionListener m_onClickRegionListener) {
        this.m_onClickRegionListener = m_onClickRegionListener;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        refreshLayoutBG(checked);
    }

    /**
     * 点击后更新背景
     */
    private void refreshLayoutBG(boolean isChecked) {
        if (getBackground() == null)
            return;

        if (isChecked)
            getBackground().setAlpha(Opacity_Checked);
        else
            getBackground().setAlpha(Opacity_UnChecked);
    }

    public int getOpacity_Checked() {
        return Opacity_Checked;
    }

    /**
     * 设置选中时的透明度
     * @param opacity_Checked 0-255
     */
    public void setOpacity_Checked(int opacity_Checked) {
        Opacity_Checked = opacity_Checked;
        refreshLayoutBG(isChecked);
    }

    public int getOpacity_UnChecked() {
        return Opacity_UnChecked;
    }

    /**
     * 设置未选中时的透明度
     * @param opacity_UnChecked 0-255
     */
    public void setOpacity_UnChecked(int opacity_UnChecked) {
        Opacity_UnChecked = opacity_UnChecked;
        refreshLayoutBG(isChecked);
    }

    public int getMainColor() {
        return MainColor;
    }

    /**
     * 设置主要背景色
     * @param mainColor
     */
    public void setMainColor(int mainColor) {
        MainColor = mainColor;
        setBackgroundColor(MainColor);
    }

    public int getBorderColor() {
        return BorderColor;
    }

    /**
     * 设置边框颜色
     * @param borderColor
     */
    public void setBorderColor(int borderColor) {
        BorderColor = borderColor;
        invalidate();
    }

    public float getBorderWidth() {
        return BorderWidth;
    }

    /**
     * 设置边框大小
     * @param borderWidth
     */
    public void setBorderWidth(float borderWidth) {
        BorderWidth = borderWidth;
        invalidate();
    }
}
