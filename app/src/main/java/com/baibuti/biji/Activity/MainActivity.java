package com.baibuti.biji.Activity;

import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baibuti.biji.Fragment.ClassFragment;
import com.baibuti.biji.Fragment.NoteFragment;
import com.baibuti.biji.Fragment.SearchFragment;
import com.baibuti.biji.Fragment.FileFragment;
import com.baibuti.biji.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnClickListener {
    //声明ViewPager
    private ViewPager mViewPager;
    //适配器
    private FragmentPagerAdapter mAdapter;
    //装载Fragment的集合
    private List<Fragment> mFragments;

    //四个Tab对应的布局
    private LinearLayout mTabNote;
    private LinearLayout mTabSearch;
    private LinearLayout mTabClass;
    private LinearLayout mTabFile;

    //四个Tab对应的ImageButton
    private ImageButton mImgNote;
    private ImageButton mImgSearch;
    private ImageButton mImgClass;
    private ImageButton mImgFile;

    private TextView mTextNote;
    private TextView mTextSearch;
    private TextView mTextClass;
    private TextView mTextFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();//初始化控件
        initEvents();//初始化事件
        initDatas();//初始化数据
    }

    private void initDatas() {

        mFragments = new ArrayList<>();
        //将四个Fragment加入集合中
        mFragments.add(new NoteFragment());
        mFragments.add(new SearchFragment());
        mFragments.add(new ClassFragment());
        mFragments.add(new FileFragment());

        //初始化适配器
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {//从集合中获取对应位置的Fragment
                return mFragments.get(position);
            }

            @Override
            public int getCount() {//获取集合中Fragment的总数
                return mFragments.size();
            }

        };
        //不要忘记设置ViewPager的适配器
        mViewPager.setAdapter(mAdapter);
        //设置ViewPager的切换监听
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            //页面滚动事件
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //页面选中事件
            @Override
            public void onPageSelected(int position) {
                //设置position对应的集合中的Fragment
                mViewPager.setCurrentItem(position);
                resetImgs();
                selectTab(position);
            }

            @Override
            //页面滚动状态改变事件
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initEvents() {
        //设置四个Tab的点击事件
        mTabNote.setOnClickListener(this);
        mTabSearch.setOnClickListener(this);
        mTabClass.setOnClickListener(this);
        mTabFile.setOnClickListener(this);
    }

    private void initViews() {
        //获取屏幕宽度
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //添加侧拉菜单
        SlidingMenu slidingMenu =new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        //设置预留屏幕宽度
        slidingMenu.setBehindOffset(width/4);
        //全屏都可以拖拽触摸
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        //附加到当前的Activity上去
        slidingMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);
        //设置阴影的宽度
        slidingMenu.setShadowWidthRes(R.dimen.drawer_shadow);
        //设置渐入渐出效果的值
        slidingMenu.setFadeDegree(0.35f);
        //为侧滑菜单设置布局
        slidingMenu.setMenu(R.layout.left_menu);

        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        //////////
        mTabNote = (LinearLayout) findViewById(R.id.tab_note);
        mTabSearch = (LinearLayout) findViewById(R.id.tab_search);
        mTabClass = (LinearLayout) findViewById(R.id.tab_class);
        mTabFile = (LinearLayout) findViewById(R.id.tab_file);

        //////////
        mImgNote = (ImageButton) findViewById(R.id.tab_note_img);
        mImgSearch = (ImageButton) findViewById(R.id.tab_search_img);
        mImgClass = (ImageButton) findViewById(R.id.tab_class_img);
        mImgFile = (ImageButton) findViewById(R.id.tab_file_img);

        mTextNote = (TextView) findViewById(R.id.tab_note_text);
        mTextSearch = (TextView) findViewById(R.id.tab_search_text);
        mTextClass = (TextView) findViewById(R.id.tab_class_text);
        mTextFile = (TextView) findViewById(R.id.tab_file_text);

        selectTab(0);
    }

    @Override
    public void onClick(View v) {
        //先将四个ImageButton置为灰色
        resetImgs();

        //根据点击的Tab切换不同的页面及设置对应的ImageButton为绿色
        switch (v.getId()) {
            case R.id.tab_note:
                selectTab(0);
                break;
            case R.id.tab_search:
                selectTab(1);
                break;
            case R.id.tab_class:
                selectTab(2);
                break;
            case R.id.tab_file:
                selectTab(3);
                break;
        }
    }

    private void selectTab(int i) {
        //根据点击的Tab设置对应的ImageButton为绿色
        switch (i) {
            case 0:
                mTextNote.setTextColor(getResources().getColor(R.color.colorPrimary));
                //mTabNote.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                mImgNote.setImageResource(R.drawable.tab_note_pressed);
                break;
            case 1:
                mTextSearch.setTextColor(getResources().getColor(R.color.colorPrimary));
                //mTabSearch.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                mImgSearch.setImageResource(R.drawable.tab_search_pressed);
                break;
            case 2:
                mTextClass.setTextColor(getResources().getColor(R.color.colorPrimary));
                //mTabClass.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                mImgClass.setImageResource(R.drawable.tab_class_pressed);
                break;
            case 3:
                mTextFile.setTextColor(getResources().getColor(R.color.colorPrimary));
                //mTabFile.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                mImgFile.setImageResource(R.drawable.tab_file_pressed);
                break;
        }
        //设置当前点击的Tab所对应的页面
        mViewPager.setCurrentItem(i);
    }

    private void resetImgs() {
        mImgNote.setImageResource(R.drawable.tab_note_normal);
        mImgSearch.setImageResource(R.drawable.tab_search_normal);
        mImgClass.setImageResource(R.drawable.tab_class_normal);
        mImgFile.setImageResource(R.drawable.tab_file_normal);

        mTextNote.setTextColor(getResources().getColor(R.color.half_black));
        mTextSearch.setTextColor(getResources().getColor(R.color.half_black));
        mTextClass.setTextColor(getResources().getColor(R.color.half_black));
        mTextFile.setTextColor(getResources().getColor(R.color.half_black));
    }
}
