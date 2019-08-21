package com.baibuti.biji.UI.Widget.OCRView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.baibuti.biji.Net.Models.RespObj.Region;
import com.baibuti.biji.R;
import com.baibuti.biji.Utils.LayoutUtils.OCRRegionUtil;

import java.util.ArrayList;
import java.util.List;


public class OCRRegionGroupLayout extends ViewGroup {

    private Region region;
    private Bitmap imgBG;
    private onClickFramesListener m_onClickFramesListener;

    // attr
    private int checkedOpacity;
    private int uncheckedOpacity;
    private int mainColor;
    private int borderColor;
    private float borderWidth;

    // ocrlayout:mainColor="@color/image_color_blue"
    // ocrlayout:borderColor="@color/image_color_yellow"
    // ocrlayout:borderWidth="5dp"
    // ocrlayout:uncheckedOpacity="75"
    // ocrlayout:checkedOpacity="150"

    public interface onClickFramesListener {
        void onClickFrames(Region.Frame[] frames);
    }

    public OCRRegionGroupLayout(Context context) {
        this(context, null);
    }

    public OCRRegionGroupLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.OCRRegionGroupLayout);
        checkedOpacity = typedArray.getInt(R.styleable.OCRRegionGroupLayout_checkedOpacity, OCRRegionView.DEF_OPACITY_CHECKED);
        uncheckedOpacity = typedArray.getInt(R.styleable.OCRRegionGroupLayout_uncheckedOpacity, OCRRegionView.DEF_OPACITY_UNCHECKED);
        mainColor = typedArray.getColor(R.styleable.OCRRegionGroupLayout_mainColor, OCRRegionView.DEF_MAINCOLOR);
        borderColor = typedArray.getColor(R.styleable.OCRRegionGroupLayout_borderColor, OCRRegionView.DEF_BORDERCOLOR);
        borderWidth = typedArray.getDimension(R.styleable.OCRRegionGroupLayout_borderWidth, OCRRegionView.DEF_BORDERWIDTH);

        typedArray.recycle();
    }

    /**
     * 获取属性集
     * @param context
     * @param attributeSet
     * @param defStyle
     */
    public OCRRegionGroupLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    /**
     * 保存子视图的位置
     */
    public class LayoutParams extends ViewGroup.LayoutParams {
        int x;
        int y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source, int x, int y) {
            super(source);
            this.x = x;
            this.y = y;
        }
    }

    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "OCRRegionGroupLayout";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(OCRRegionView.DEF_WIDTH, OCRRegionView.DEF_HEIGHT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child instanceof OCRRegionView) { // Region
                OCRRegionView regionView = (OCRRegionView) child;
                if (regionView.getFrame() == null)
                    continue;

                // 如果背景为空，直接填充呈白色
                if (imgBG == null) {
                    imgBG = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
                    imgBG.eraseColor(Color.parseColor("#FFFFFF"));
                }

                Region.Point[] point4 = OCRRegionUtil.parsePnts(
                    regionView.getFrame().getPoints(),
                    getWidth(), getHeight(),
                    imgBG.getWidth(), imgBG.getHeight()
                );

                setRotationAndLayoutOfOCRRegion(regionView, point4);

            }
            else if (child instanceof ImageView) { // BG
                child.layout(l, t, r, b);
            }
        }
    }

    /**
     * 设置布局和旋转 推导重要!!
     * @param child
     * @param point4
     */
    private void setRotationAndLayoutOfOCRRegion(OCRRegionView child, Region.Point[] point4) {

        // 获得三点
        int pnt1X = point4[0].getX();
        int pnt1Y = point4[0].getY();

        int pnt2X = point4[1].getX();
        int pnt2Y = point4[1].getY();

        int pnt3X = point4[2].getX();
        int pnt3Y = point4[2].getY();

        // 旋转角
        float rad = (float) Math.atan2(pnt2Y - pnt1Y, pnt2X - pnt1X);
        float deg = (float) Math.toDegrees(rad);

        double cos = Math.cos(-rad);
        double sin = Math.sin(-rad);

        // 俩向量旋转
        double vec1X = pnt3X - pnt1X;
        double vec1Y = pnt3Y - pnt1Y;

        double vec2X = vec1X * cos - vec1Y * sin;
        double vec2Y = vec1X * sin + vec1Y * cos;

        int new3X = (int) (pnt1X + vec2X);
        int new3Y = (int) (pnt1Y + vec2Y);

        // 新布局
        child.layout(pnt1X, pnt1Y, new3X, new3Y);

        // 角度
        child.setPivotX(0);
        child.setPivotY(0);
        child.setRotation(deg);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
    }

    public Region getRegion() {
        return region;
    }

    /**
     * 设置区域数据
     * @param region
     */
    public void setRegion(Region region) {
        this.region = region;
        setupRegion();
    }

    /**
     * 配置 区域集合
     */
    private void setupRegion() {
        if (region == null)
            return;

        Region.Frame[] frames = region.getFrames();

        for (Region.Frame frame : frames) {
            OCRRegionView regionView = new OCRRegionView(getContext());

            // attr
            regionView.setMainColor(mainColor);
            regionView.setOpacity_Checked(checkedOpacity);
            regionView.setOpacity_UnChecked(uncheckedOpacity);
            regionView.setBorderColor(borderColor);
            regionView.setBorderWidth(borderWidth);

            // data
            regionView.setFrame(frame);

            regionView.setOnClickRegionListener(new OCRRegionView.onClickRegionListener() {

                @Override
                public void onClickAfterUp(Region.Frame frame) {
                    ShowLogE("onClickAfterDown", "Up: " + frame.getOcr());
                    onChangeChecked();
                }

                @Override
                public void onClickAfterDown(Region.Frame frame) {
                    ShowLogE("onClickAfterDown", "Down: " + frame.getOcr());
                    onChangeChecked();
                }
            });

            addView(regionView);
        }
    }

    public void setOnClickRegionsListener(onClickFramesListener m_onClickFramesListener) {
        this.m_onClickFramesListener = m_onClickFramesListener;
    }

    /**
     * 当选择修改时，通知订阅
     * @return
     */
    private void onChangeChecked() {
        if (m_onClickFramesListener != null) {
            List<Region.Frame> frames = new ArrayList<>();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof OCRRegionView) { // Region
                    if (((OCRRegionView) child).isChecked())
                        frames.add(((OCRRegionView) child).getFrame());
                }
            }
            m_onClickFramesListener.onClickFrames(frames.toArray(new Region.Frame[0]));
        }
    }

    public Bitmap getImgBG() {
        return imgBG;
    }

    /**
     * 设置背景
     * @param imgBG
     */
    public void setImgBG(Bitmap imgBG) {
        this.imgBG = imgBG;
        setupBG();
    }

    /**
     * 配置背景
     */
    private void setupBG() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(imgBG);

        addView(imageView);
    }

    /**
     * 选择全部区域或者取消选择全部区域
     * @param isSelect
     */
    public void setAllFramesChecked(boolean isSelect) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof OCRRegionView) // Region
                ((OCRRegionView) child).setChecked(isSelect);
        }
        onChangeChecked();
    }
}
