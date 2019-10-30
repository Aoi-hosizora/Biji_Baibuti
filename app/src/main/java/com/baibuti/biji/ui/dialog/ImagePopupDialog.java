package com.baibuti.biji.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baibuti.biji.net.module.note.ImgUtil;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.imageView.PhotoView;
import com.baibuti.biji.util.imgDocUtil.BitmapUtil;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.internal.annotations.EverythingIsNonNull;

public class ImagePopupDialog extends Dialog {

    private Activity activity;

    private ViewPager mPager;
    private TextView mPageIndexTextView;
    private PagerAdapter mAdapter;

    private volatile Bitmap[] imgs;
    private int index;

    private onLongClickImageListener m_onLongClickImageListener;

    public interface onLongClickImageListener {
        void onLongClick(View v, int index);
    }

    public void setOnLongClickImageListener(onLongClickImageListener m_onLongClickImageListener) {
        this.m_onLongClickImageListener = m_onLongClickImageListener;
    }

    /**
     * 全局设置 Log 格式
     * @param FunctionName
     * @param Msg
     */
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "ImagePopupDialog";
        Log.e(getContext().getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 以图片为输入，备用
     * @param activity
     * @param imgs Bitmap[]
     * @param index 当前图片
     */
    public ImagePopupDialog(Activity activity, Bitmap[] imgs, int index) {
        super(activity);
        this.activity = activity;
        this.imgs = imgs;
        this.index = index;
    }

    /**
     * 以url为输入
     * @param activity
     * @param urls String[] 图片路径
     * @param index 当前图片
     */
    public ImagePopupDialog(Activity activity, String[] urls, int index) {
        super(activity);
        this.activity = activity;

        // TODO
        //  E/: ImagePopupDialog: /storage/emulated/0/Biji/NoteImage/20190809141402253_Small.jpg -> false
        //  E/BitmapFactory: Unable to decode stream: java.io.FileNotFoundException:
        //      https:/raw.githubusercontent.com/Aoi-hosizora/Biji_Baibuti/a5bb15af4098296ace557e281843513b2f672e0f/assets/DB_Query.png (No such file or directory)
        //  E/: ImagePopupDialog: https://raw.githubusercontent.com/Aoi-hosizora/Biji_Baibuti/a5bb15af4098296ace557e281843513b2f672e0f/assets/DB_Query.png -> true

        final ArrayList<Bitmap> bitmaps = new ArrayList<>();

        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];

            // TODO File
            Bitmap file = BitmapUtil.getBitmapFromFile(url);

            if (file == null) {
                bitmaps.add(null);
                final int ki = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImgUtil.GetImgAsync(url, new ImgUtil.IImageBack() {
                            @Override
                            public void onGetImg(Bitmap bitmap) {

                                // TODO
                                //    getItemPosition POSITION_NONE

                                Log.e("", "onGetImg: " + url);
                                imgs[ki] = bitmap;

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });

                    }
                }).start();
            }
            else
                bitmaps.add(file);
        }

        this.imgs = bitmaps.toArray(new Bitmap[0]);
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
        String tip = String.format(Locale.CHINA, "%d / %d", position + 1, imgs.length);
        mPageIndexTextView.setText(tip);
    }


    /**
     * 设置显示的 PageView
     */
    private void setupPager() {

        mPager.setPageMargin((int) (getContext().getResources().getDisplayMetrics().density * 15));

        mAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return imgs.length;
            }

            @Override
            @EverythingIsNonNull
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }

            @Override
            public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
                // ShowLogE("setupPager", "instantiateItem: " + position);

                PhotoView view = new PhotoView(getContext());
                view.enable();
                view.enableRotate();

                view.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // KEY
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
            @EverythingIsNonNull
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        };

        mPager.setAdapter(mAdapter);
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
