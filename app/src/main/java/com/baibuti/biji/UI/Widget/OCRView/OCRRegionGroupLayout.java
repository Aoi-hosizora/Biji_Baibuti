package com.baibuti.biji.UI.Widget.OCRView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.Net.Models.Region;
import com.baibuti.biji.R;
import com.baibuti.biji.Utils.BitmapUtils;


public class OCRRegionGroupLayout extends ViewGroup implements IShowLog {

    private Region region;
    private String imgUrl;

    public OCRRegionGroupLayout(Context context) {
        this(context, null);
    }

    public OCRRegionGroupLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /**
     * 获取属性集
     * @param context
     * @param attributeSet
     * @param defStyle
     */
    public OCRRegionGroupLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        // TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.CascadeLayout);
        // horizontalSpacing = a.getDimensionPixelSize(R.styleable.CascadeLayout_horizontal_spacing, (int) getResources().getDimension(R.dimen.Card_Horizontal_Spacing));
        // verticalSpacing = a.getDimensionPixelSize(R.styleable.CascadeLayout_vertical_spacing, R.dimen.Card_Vertical_Spacing);
        // a.recycle();
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

    @Override
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

                // TODO 非正四边形

                int pnt1X = regionView.getFrame().getPoints()[0].getX();
                int pnt1Y = regionView.getFrame().getPoints()[0].getY();

                int pnt3X = regionView.getFrame().getPoints()[2].getX();
                int pnt3Y = regionView.getFrame().getPoints()[2].getY();

                child.layout(pnt1X, pnt1Y, pnt3X, pnt3Y);
            }
            else if (child instanceof ImageView) { // BG
                ImageView imageView = (ImageView) child;

                // TODO 图片位置

                child.layout(l, t, r, b);
            }
        }
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

            regionView.setBackgroundColor(OCRRegionView.DEF_BGCOLOR);
            regionView.setFrame(frame);

            regionView.setOnClickRegionListener(new OCRRegionView.onClickRegionListener() {

                @Override
                public void onClickAfterUp(Region.Frame frame) {
                    ShowLogE("onClickAfterDown", "Up: " + frame.getOcr());
                }

                @Override
                public void onClickAfterDown(Region.Frame frame) {
                    ShowLogE("onClickAfterDown", "Down: " + frame.getOcr());
                }
            });

            addView(regionView);
        }
    }

    public String getImgUrl() {
        return imgUrl;
    }

    /**
     * 设置背景
     * @param imgUrl
     */
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        setupBG();
    }

    /**
     * 配置背景
     */
    private void setupBG() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(BitmapUtils.getBitmapFromFile(imgUrl));

        addView(imageView);
    }
}
