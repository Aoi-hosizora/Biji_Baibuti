package com.baibuti.biji.UI.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;

import android.view.ViewGroup;
import android.widget.Toast;
import android.view.MenuItem;

import com.baibuti.biji.UI.Fragment.ClassFragment;
import com.baibuti.biji.UI.Fragment.NoteFragment;
import com.baibuti.biji.UI.Fragment.SearchFragment;
import com.baibuti.biji.UI.Fragment.FileFragment;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.OtherView.SimplerSearcherView;
import com.facebook.stetho.Stetho;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import  com.baibuti.biji.Utils.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements SimplerSearcherView.OnSearcherClickListener, IShowLog {
    //声明ViewPager
    private ViewPager mViewPager;
    //适配器
    private FragmentStatePagerAdapter mAdapter;
    //装载Fragment的集合
    private List<Fragment> mFragments;

    //侧拉菜单
    private SlidingMenu slidingMenu;
    //底部导航栏
    private BottomNavigationView  bottomNavigationView;

    private NoteFragment mNoteFrag;
    private SearchFragment mSearchFrag;
    private ClassFragment mClassFrag;
    private FileFragment mFileFrag;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //对Android 6.0以上版本动态申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            };
        }

        Stetho.initializeWithDefaults(this);

        initViews();
        initadpts();
        initsmenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
        }
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


    private void initViews() {

        mNoteFrag = new NoteFragment();
        mSearchFrag = new SearchFragment();
        mClassFrag = new ClassFragment();
        mFileFrag = new FileFragment();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.id_bottomnavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.item1:
                        mViewPager.setCurrentItem(0);
                    break;
                    case R.id.item2:
                        mViewPager.setCurrentItem(1);
                    break;
                    case R.id.item3:
                        mViewPager.setCurrentItem(2);
                    break;
                    case R.id.item4:
                        mViewPager.setCurrentItem(3);
                    break;
                }
                return true;
            }
        });
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);

        mViewPager = findViewById(R.id.id_viewpager);

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }


        });
    }


    private void initadpts() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(mNoteFrag);
        mFragments.add(mSearchFrag);
        mFragments.add(mClassFrag);
        mFragments.add(mFileFrag);

        ShowLogE("initadpts", "mFragments: " + mFragments.size());

        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
            }
        };

        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
    }

    private void initsmenu() {

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
        slidingMenu.setMenu(R.layout.modulelayout_leftmenu);

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
