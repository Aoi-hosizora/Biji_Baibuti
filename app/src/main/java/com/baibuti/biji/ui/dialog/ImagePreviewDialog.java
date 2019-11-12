package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.imageView.PhotoView;
import com.baibuti.biji.util.imgTextUtil.ImageUtil;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class ImagePreviewDialog extends Dialog {

    @BindView(R.id.id_image_popup_dialog_pager)
    private ViewPager m_view_pager;

    @BindView(R.id.id_image_popup_dialog_index)
    private TextView m_txt_page_idx;

    private PagerAdapter m_pager_adapter;

    private volatile Bitmap[] bitmaps;

    private int currIndex;

    private onLongClickImageListener m_onLongClickImageListener;

    public interface onLongClickImageListener {
        void onLongClick(View v, int index);
    }

    public void setOnLongClickImageListener(onLongClickImageListener m_onLongClickImageListener) {
        this.m_onLongClickImageListener = m_onLongClickImageListener;
    }

    // /**
    //  * 以图片为输入，备用
    //  * @param activity 当前活动，用于 runOnUiThread
    //  * @param bitmaps Bitmap[]
    //  * @param currIndex 当前图片
    //  */
    // public ImagePreviewDialog(Activity activity, Bitmap[] bitmaps, int currIndex) {
    //     super(activity);
    //     this.bitmaps = bitmaps;
    //     this.currIndex = currIndex;
    // }

    /**
     * 以 path 为输入
     * @param activity 当前活动，用于 runOnUiThread
     * @param urls String[] 图片路径
     * @param currIndex 当前图片
     */
    public ImagePreviewDialog(Activity activity, String[] urls, int currIndex) {
        super(activity);

        // 异步加载数据
        final ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];

            // 本地
            Bitmap bitmap = ImageUtil.getBitmapFromPath(url);
            if (bitmap == null) {
                // TODO 网络
                bitmaps.add(null);
                final int ki = i;
                new Thread(() ->
                    ImageUtil.getImgAsync(activity, url, (netBitmap) -> {
                        // Log.e("", "onGetImg: " + url);
                        this.bitmaps[ki] = netBitmap;
                        m_pager_adapter.notifyDataSetChanged();
                    })
                ).start();
            } else
                bitmaps.add(bitmap);
        }

        this.bitmaps = bitmaps.toArray(new Bitmap[0]);
        this.currIndex = currIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_imagepopup_viewpager);

        if (getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(lp);
        }

        m_view_pager = findViewById(R.id.id_image_popup_dialog_pager);
        m_txt_page_idx = findViewById(R.id.id_image_popup_dialog_index);

        setupPager();
    }

    /**
     * 底层的页码
     */
    private void setPageIndexLabel(int position) {
        String tip = String.format(Locale.CHINA, "%d / %d", position + 1, bitmaps.length);
        m_txt_page_idx.setText(tip);
    }


    /**
     * 设置显示的 PageView
     */
    private void setupPager() {

        m_view_pager.setPageMargin((int) (getContext().getResources().getDisplayMetrics().density * 15));

        m_pager_adapter = new PagerAdapter() {

            @Override
            public int getCount() { return bitmaps.length; }
            @Override
            @EverythingIsNonNull
            public boolean isViewFromObject(View view, Object object) { return view == object; }
            @Override
            public int getItemPosition(@NonNull Object object) { return POSITION_NONE; }

            @Override
            @EverythingIsNonNull
            public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((View) object); }

            /**
             * !!!
             */
            @Override
            public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {

                PhotoView view = new PhotoView(getContext());
                view.enable();
                view.enableRotate();
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // !!!
                view.setImageResource(bitmaps[position]);
                view.setOnLongClickListener((View v) -> {
                    if (m_onLongClickImageListener != null)
                        m_onLongClickImageListener.onLongClick(v, currIndex);
                    return true;
                });
                view.setOnClickListener((View v) -> dismiss());
                container.addView(view);
                return view;
            }
        };

        m_view_pager.setAdapter(m_pager_adapter);
        m_view_pager.setCurrentItem(currIndex);
        setPageIndexLabel(currIndex);

        m_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currIndex = position;
                // 更新页码
                setPageIndexLabel(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

    }
}
