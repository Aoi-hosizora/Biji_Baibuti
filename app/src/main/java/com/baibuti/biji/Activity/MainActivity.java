package com.baibuti.biji.Activity;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;

import android.widget.Toast;
import android.view.MenuItem;

import com.baibuti.biji.Fragment.ClassFragment;
import com.baibuti.biji.Fragment.NoteFragment;
import com.baibuti.biji.Fragment.SearchFragment;
import com.baibuti.biji.Fragment.FileFragment;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.View.SimplerSearcherView;
import com.facebook.stetho.Stetho;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import  com.baibuti.biji.util.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements SimplerSearcherView.OnSearcherClickListener, IShowLog {
    //声明ViewPager
    private ViewPager mViewPager;
    //适配器
    private FragmentPagerAdapter mAdapter;
    //装载Fragment的集合
    private List<Fragment> mFragments;

    //侧拉菜单
    private SlidingMenu slidingMenu;
    //底部导航栏
    private BottomNavigationView  bottomNavigationView;

    private NoteFragment mNoteFrag = new NoteFragment();
    private SearchFragment mSearchFrag = new SearchFragment();
    private ClassFragment mClassFrag = new ClassFragment();
    private FileFragment mFileFrag = new FileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Stetho.initializeWithDefaults(this);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();//初始化控件
        initDatas();//初始化数据
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "MainActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    @Override
    public void onSearcherClick(String content) {
        // Toast.makeText(this, "This is searcher", Toast.LENGTH_LONG).show();
    }

    private void initDatas() {

        mFragments = new ArrayList<>();
        //将四个Fragment加入集合中
        mFragments.add(mNoteFrag);
        mFragments.add(mSearchFrag);
        mFragments.add(mClassFrag);
        mFragments.add(mFileFrag);

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
                switch(position){
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.item1);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.item2);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.item3);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.item4);
                        break;
                }
            }

            @Override
            //页面滚动状态改变事件
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initViews() {
        //获取屏幕宽度
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //添加侧拉菜单
        slidingMenu =new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        //设置预留屏幕宽度
        slidingMenu.setBehindOffset(width/5);
        //全屏都可以拖拽触摸
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        //附加到当前的Activity上去
        slidingMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);
        //设置阴影
        slidingMenu.setOffsetFadeDegree(0.4f);
        //设置渐入渐出效果的值
        slidingMenu.setFadeDegree(0.35f);
        //为侧滑菜单设置布局
        slidingMenu.setMenu(R.layout.left_menu);

        mViewPager = findViewById(R.id.id_viewpager);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.id_bottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.item1:
                        //设置当前点击的Tab所对应的页面
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.item2:
                        //设置当前点击的Tab所对应的页面
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.item3:
                        //设置当前点击的Tab所对应的页面
                        mViewPager.setCurrentItem(2);
                        break;
                    case R.id.item4:
                        //设置当前点击的Tab所对应的页面
                        mViewPager.setCurrentItem(3);
                        break;
                }
                return true;
            }
        });
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }

    public SlidingMenu getSlidingMenu() {
        return slidingMenu;
    }


    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            // 判断是否处于笔记搜索页面
            if (mNoteFrag.getIsSearching()) {
                mNoteFrag.SearchFracBack();
                return true;
            }
            // 不在笔记搜索页面，退出程序
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.onKeyDownExit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
