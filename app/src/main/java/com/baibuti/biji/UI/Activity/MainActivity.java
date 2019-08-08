package com.baibuti.biji.UI.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;

import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;

import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Auth.AuthUtil;
import com.baibuti.biji.UI.Fragment.ScheduleFragment;
import com.baibuti.biji.UI.Fragment.NoteFragment;
import com.baibuti.biji.UI.Fragment.SearchFragment;
import com.baibuti.biji.UI.Fragment.FileFragment;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.Utils.StrSrchUtils.SearchUtil;
import com.facebook.stetho.Stetho;
import com.baibuti.biji.Utils.LayoutUtils.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements IShowLog, NavigationView.OnNavigationItemSelectedListener {

    // region 声明显示元素 mViewPager bottomNavigationView m_drawerLayout m_navigationView

    private ViewPager mViewPager;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout m_drawerLayout;
    private NavigationView m_navigationView;

    // endregion 声明显示元素

    // region 声明列表信息 mFragments mxxFrags

    private List<Fragment> mFragments;

    private NoteFragment mNoteFrag;
    private SearchFragment mSearchFrag;
    private ScheduleFragment mClassFrag;
    private FileFragment mFileFrag;

    // endregion 声明列表信息

    // region 声明权限和状态码 PERMISSIONS_STORAGE REQUEST_PERMISSION_CODE

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    private static int REQUEST_PERMISSION_CODE_CAMERA = 2;

    // endregion 声明权限和状态码

    // region 显示 适配器 onCreate initViews initAdpts ShowLogE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //对Android 6.0以上版本动态申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE_CAMERA);
            }
        }


        new Thread(new Runnable() {
            @Override
            public void run() {

                // FB 数据库查看
                Stetho.initializeWithDefaults(getApplicationContext());

                // 初始化结巴分词
                SearchUtil.initJieba(getApplicationContext());

            }
        }).start();

        initViews();
        initAdpts();
        initNav();

        // TODO
         checkLoginStatus();
    }

    /**
     * 初始化布局
     */
    private void initViews() {

        mNoteFrag = new NoteFragment();
        mSearchFrag = new SearchFragment();
        mClassFrag = new ScheduleFragment();
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

    /**
     * 初始化页面适配器
     */
    private void initAdpts() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(mNoteFrag);
        mFragments.add(mSearchFrag);
        mFragments.add(mClassFrag);
        mFragments.add(mFileFrag);

        ShowLogE("initadpts", "mFragments: " + mFragments.size());

        FragmentStatePagerAdapter mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
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

    @Override
    protected void onResume() {
        super.onResume();
        NavMenuDefSelect();
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "MainActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    // endregion 显示和适配器

    // region 权限返回 onRequestPermissionsResult

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
        }
        if (requestCode == REQUEST_PERMISSION_CODE_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
        }
    }

    // endregion 权限返回

    // region 退出记录 exitTime onKeyDown

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            // 是否打开着Drawer
            if (mViewPager.getCurrentItem() == 0 &&
                mNoteFrag.getDrawerIsOpen()) {
                mNoteFrag.closeDrawer();
                return true;
            }

            // 判断是否处于笔记搜索页面或分类界面
            if (mViewPager.getCurrentItem() == 0 &&
                    (mNoteFrag.getIsSearching() || mNoteFrag.getIsGrouping())) {
                mNoteFrag.SearchGroupBack();
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

    // endregion 退出记录

    ////////////////////////////////////////////////

    // region 侧边栏 initNav openNavMenu closeNavMenu onNavigationItemSelected

    /**
     * 初始化侧边栏
     */
    private void initNav() {
        m_drawerLayout = findViewById(R.id.id_mainAct_drawer_layout);

        m_navigationView = findViewById(R.id.id_mainAct_left_nav);
        m_navigationView.setNavigationItemSelectedListener(this);

        // 默认选中
        NavMenuDefSelect();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // 宽度
        ViewGroup.LayoutParams params = m_navigationView.getLayoutParams();
        params.width = metrics.widthPixels / 3 * 2;

        m_navigationView.setLayoutParams(params);
    }

    /**
     * 打开侧边栏
     */
    public void openNavMenu() {
        m_drawerLayout.openDrawer(Gravity.START);
    }

    /**
     * 打开侧边栏
     */
    public void closeNavMenu() {
        m_drawerLayout.closeDrawer(Gravity.START);
    }

    /**
     * 侧边栏默认选中
     */
    private void NavMenuDefSelect() {
        m_navigationView.setCheckedItem(R.id.id_nav_main);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_nav_login:
                closeNavMenu();
                if (m_navigationView.getMenu().findItem(R.id.id_nav_login).getTitle().equals(getString(R.string.nav_login)))
                    toLogin();
                else
                    toLogout();
                return false;
            case R.id.id_nav_about:
                about();
            break;
            case R.id.id_nav_feedback:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://github.com/Aoi-hosizora/Biji_Baibuti/issues"));
                startActivity(browserIntent);
            break;
        }
        closeNavMenu();
        return true;
    }

    private void about() {
        String msg = "SCUT 百步梯项目 - 笔迹\n\n" +
                "开发网站：https://github.com/Aoi-hosizora/Biji_Baibuti\n\n" +
                "作者：17级软件学院xxxxx\n\n" +
                "更多信息详看开发网站。";
        new AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .create().show();
    }

    /**
     * 刷新界面显示用户
     * @param username
     */
    private void refreshUserInfo(String username) {
        TextView usrlabel = m_navigationView.getHeaderView(0).findViewById(R.id.id_nav_username);
        usrlabel.setText(username);
    }

    /**
     * 导航栏 登录
     */
    private void toLogin() {
        Intent reglogIntent = new Intent(MainActivity.this, RegLogActivity.class);
        startActivityForResult(reglogIntent, REQ_LOGIN);
    }

    /**
     * 活动返回 登陆成功
     */
    private void login() {
        m_navigationView.getMenu().findItem(R.id.id_nav_login).setTitle(R.string.nav_logout);
        refreshUserInfo(AuthMgr.getInstance().getUserName());

        // TODO 更新界面
        checkLoginStatus();
    }

    /**
     * 导航栏 注销
     */
    private void toLogout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (AuthUtil.logout()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AuthMgr.getInstance().logout();
                                m_navigationView.getMenu().findItem(R.id.id_nav_login).setTitle(R.string.nav_login);
                                Toast.makeText(MainActivity.this, "注销成功，请重新登录。", Toast.LENGTH_SHORT).show();
                                checkLoginStatus();
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AuthMgr.getInstance().logout();
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("错误")
                                        .setMessage("注销未知错误。")
                                        .setPositiveButton("确定", null)
                                        .create().show();
                                m_navigationView.getMenu().findItem(R.id.id_nav_login).setTitle(R.string.nav_login);
                                checkLoginStatus();
                            }
                        });
                    }
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("注销错误")
                                    .setMessage(ex.getMessage())
                                    .setPositiveButton("确定", null)
                                    .create().show();
                        }
                    });
                }
            }
        }).start();

        // TODO 更新界面

        // AuthMgr.getInstance().addLoginChangeListener(new AuthMgr.OnLoginChangeListener() {
        //
        //     @Override
        //     public void onLogin(String UserName) {
        //
        //     }
        //
        //     @Override
        //     public void onLogout() {
        //
        //     }
        // });
    }

    private void checkLoginStatus() {
        // TODO
        if (!(AuthMgr.getInstance().isLogin()))
            refreshUserInfo("未登录用户");
        else
            refreshUserInfo(AuthMgr.getInstance().getUserName());

    }

    private final int REQ_LOGIN = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_LOGIN:
                if (resultCode == RESULT_OK)
                    login();
            break;
        }
    }

    // endregion 侧边栏 openNavMenu closeNavMenu
}
