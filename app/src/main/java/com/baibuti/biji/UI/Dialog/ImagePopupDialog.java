package com.baibuti.biji.UI.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.PhotoView.PhotoView;
import com.baibuti.biji.Utils.BitmapUtils;

import java.util.ArrayList;

public class ImagePopupDialog extends Dialog implements IShowLog {

    private ViewPager mPager;
    private TextView mPageIndexTextView;

    private Bitmap[] imgs;
    private int index;

    private onLongClickImageListener m_onLongClickImageListener;

    public interface onLongClickImageListener {
        void onLongClick(View v, int index);
    }

    public void setOnLongClickImageListener(onLongClickImageListener m_onLongClickImageListener) {
        this.m_onLongClickImageListener = m_onLongClickImageListener;
    }

    /**
     * IShowLog 接口，全局设置 Log 格式
     * @param FunctionName
     * @param Msg
     */
    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ImagePopupDialog";
        Log.e(getContext().getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 以图片为输入，备用
     * @param context
     * @param imgs Bitmap[]
     * @param index 当前图片
     */
    public ImagePopupDialog(Context context, Bitmap[] imgs, int index) {
        super(context);
        this.imgs = imgs;
        this.index = index;
    }

    /**
     * 以url为输入
     * @param context
     * @param urls String[] 图片路径
     * @param index 当前图片
     */
    public ImagePopupDialog(Context context, String[] urls, int index) {
        super(context);

        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (String url : urls)
            bitmaps.add(BitmapUtils.getBitmapFromFile(url));

        this.imgs = bitmaps.toArray(new Bitmap[bitmaps.size()]);
        this.index = index;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_imagepopup_viewpager);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mPager = (ViewPager) findViewById(R.id.id_image_popup_dialog_pager);
        mPageIndexTextView = (TextView) findViewById(R.id.id_image_popup_dialog_index);

        setupPager();
    }

    /**
     * 显示底层的页码
     * @param position
     */
    private void RefreshPageIndexTextView(int position) {
        String tip = String.format("%d / %d", position + 1, imgs.length);
        mPageIndexTextView.setText(tip);
    }


    /**
     * 设置显示的 PageView
     */
    private void setupPager() {

        mPager.setPageMargin((int) (getContext().getResources().getDisplayMetrics().density * 15));

        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imgs.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // ShowLogE("setupPager", "instantiateItem: " + position);

                PhotoView view = new PhotoView(getContext());
                view.enable();
                view.enableRotate();

                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                view.setImageResource(imgs[position]);

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (m_onLongClickImageListener != null)
                            m_onLongClickImageListener.onLongClick(v, index);
                        return true;
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        mPager.setCurrentItem(index);
        RefreshPageIndexTextView(index);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                index = position;
                RefreshPageIndexTextView(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

    }


}
