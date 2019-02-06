package com.baibuti.biji;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
    private LinearLayout mTabHome;
    private LinearLayout mTabNote;
    private LinearLayout mTabAlarm;
    private LinearLayout mTabSetting;

    //四个Tab对应的ImageButton
    private ImageButton mImgHome;
    private ImageButton mImgNote;
    private ImageButton mImgAlarm;
    private ImageButton mImgSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initViews();//初始化控件
        initEvents();//初始化事件
        initDatas();//初始化数据


    }

    private void initDatas() {
        mFragments = new ArrayList<>();
        //将四个Fragment加入集合中
        mFragments.add(new HomeFragment());
        mFragments.add(new NoteFragment());
        mFragments.add(new AlertFragment());
        mFragments.add(new SettingFragment());

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
        mTabHome.setOnClickListener(this);
        mTabNote.setOnClickListener(this);
        mTabAlarm.setOnClickListener(this);
        mTabSetting.setOnClickListener(this);
    }

    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        //////////
        mTabHome = (LinearLayout) findViewById(R.id.id_tab_home);
        mTabNote = (LinearLayout) findViewById(R.id.id_tab_note);
        mTabAlarm = (LinearLayout) findViewById(R.id.id_tab_alarm);
        mTabSetting = (LinearLayout) findViewById(R.id.id_tab_setting);

        //////////
        mImgHome = (ImageButton) findViewById(R.id.id_tab_home_img);
        mImgNote = (ImageButton) findViewById(R.id.id_tab_note_img);
        mImgAlarm = (ImageButton) findViewById(R.id.id_tab_alarm_img);
        mImgSetting = (ImageButton) findViewById(R.id.id_tab_setting_img);

    }

    @Override
    public void onClick(View v) {
        //先将四个ImageButton置为灰色
        resetImgs();

        //根据点击的Tab切换不同的页面及设置对应的ImageButton为绿色
        switch (v.getId()) {
            case R.id.id_tab_home:
                selectTab(0);
                break;
            case R.id.id_tab_note:
                selectTab(1);
                break;
            case R.id.id_tab_alarm:
                selectTab(2);
                break;
            case R.id.id_tab_setting:
                selectTab(3);
                break;
        }
    }

    private void selectTab(int i) {
        //根据点击的Tab设置对应的ImageButton为绿色
        switch (i) {
            case 0:
                mImgHome.setImageResource(R.mipmap.tab_weixin_pressed);
                break;
            case 1:
                mImgNote.setImageResource(R.mipmap.tab_find_frd_pressed);
                break;
            case 2:
                mImgAlarm.setImageResource(R.mipmap.tab_address_pressed);
                break;
            case 3:
                mImgSetting.setImageResource(R.mipmap.tab_settings_pressed);
                break;
        }
        //设置当前点击的Tab所对应的页面
        mViewPager.setCurrentItem(i);
    }

    private void resetImgs() {
        mImgHome.setImageResource(R.mipmap.tab_weixin_normal);
        mImgNote.setImageResource(R.mipmap.tab_find_frd_normal);
        mImgAlarm.setImageResource(R.mipmap.tab_address_normal);
        mImgSetting.setImageResource(R.mipmap.tab_settings_normal);
    }
}
