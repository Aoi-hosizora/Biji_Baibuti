package com.baibuti.biji.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.MenuItem;

import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.auth.AuthService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.fragment.BaseFragment;
import com.baibuti.biji.ui.fragment.ScheduleFragment;
import com.baibuti.biji.ui.fragment.NoteFragment;
import com.baibuti.biji.ui.fragment.SearchFragment;
import com.baibuti.biji.ui.fragment.FileFragment;
import com.baibuti.biji.R;
import com.baibuti.biji.util.otherUtil.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements IContextHelper {

    @BindView(R.id.mainAct_view_pager)
    ViewPager m_viewPager;

    @BindView(R.id.mainAct_layout_drawer)
    DrawerLayout m_drawerLayout;

    @BindView(R.id.mainAct_view_left_nav)
    NavigationView m_navigationView;

    // 权限请求：读写，网络，摄像机
    private static final String[] ALL_PERMISSION = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,

        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,

        Manifest.permission.CAMERA
    };
    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Android 6.0 以上版本 -> 动态申请权限

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            List<String> RequirePermission = new ArrayList<>();
            for (String permission : ALL_PERMISSION) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    RequirePermission.add(permission);
            }

            if (RequirePermission.size() > 0) {
                showAlert(this,
                    "授权", "笔迹需要访问本地存储，摄像机以及网络等权限，请授权。",
                    "授权", ((dialog, which) ->
                        ActivityCompat.requestPermissions(this, RequirePermission.toArray(new String[0]), REQUEST_PERMISSION_CODE)),
                    "取消", null
                );
            }
        }

        initViews(); // 布局
        initNav();   // 滑动栏

        // 登陆注销订阅
        AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {

            @Override
            public void onLogin(String username) {
                m_navigationView.getMenu().findItem(R.id.nav_left_login).setTitle(R.string.nav_logout);
                setTitle("笔迹 - " + username); // 无用
            }

            @Override
            public void onLogout() {
                m_navigationView.getMenu().findItem(R.id.nav_left_login).setTitle(R.string.nav_login);
                setTitle("笔迹 - 未登录用户"); // 无用
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                showToast(this, "授权失败");
        }
    }

    /**
     * 初始化布局，导航，适配器
     */
    private void initViews() {
        // Frags
        List<BaseFragment> fragments = new ArrayList<>();

        fragments.add(new NoteFragment());
        fragments.add(new SearchFragment());
        fragments.add(new ScheduleFragment());
        fragments.add(new FileFragment());

        // Navigation
        BottomNavigationView navigationView = findViewById(R.id.mainAct_view_bottom_navi);
        navigationView.setOnNavigationItemSelectedListener((@NonNull MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.nav_bottom_note:
                    m_viewPager.setCurrentItem(0);
                    break;
                case R.id.nav_bottom_search:
                    m_viewPager.setCurrentItem(1);
                    break;
                case R.id.nav_bottom_schedule:
                    m_viewPager.setCurrentItem(2);
                    break;
                case R.id.nav_bottom_file:
                    m_viewPager.setCurrentItem(3);
                    break;
            }
            return true;
        });

        LayoutUtil.disableShiftMode(navigationView);

        // ViewPager
        m_viewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                m_viewPager.setCurrentItem(position);
                switch (position) {
                    case 0:
                        navigationView.setSelectedItemId(R.id.nav_bottom_note);
                        break;
                    case 1:
                        navigationView.setSelectedItemId(R.id.nav_bottom_search);
                        break;
                    case 2:
                        navigationView.setSelectedItemId(R.id.nav_bottom_schedule);
                        break;
                    case 3:
                        navigationView.setSelectedItemId(R.id.nav_bottom_file);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        // Adapter
        FragmentStatePagerAdapter statePagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
            }
        };

        m_viewPager.setOffscreenPageLimit(1);
        m_viewPager.setAdapter(statePagerAdapter);
        m_viewPager.setCurrentItem(0);
    }

    /**
     * 两次返回按键 第一次按下时间
     */
    private long onBackKeyDownTime = 0;

    /**
     * 两次按键间隔 (2s)
     */
    private static final long backKeyDownInternal = 2000;

    /**
     * 返回按键，委托当前碎片以及本活动
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (m_drawerLayout != null && m_drawerLayout.isDrawerOpen(Gravity.START)) {
                m_drawerLayout.closeDrawer(Gravity.START);
                return true;
            }

            // 当前碎片 返回按键
            FragmentStatePagerAdapter statePagerAdapter = (FragmentStatePagerAdapter) m_viewPager.getAdapter();
            if (statePagerAdapter != null) {
                BaseFragment fragment = (BaseFragment) statePagerAdapter.getItem(m_viewPager.getCurrentItem());
                if (fragment != null && fragment.onBackPressed())
                    return true;
            }

            // 退出程序
            if ((System.currentTimeMillis() - onBackKeyDownTime) > backKeyDownInternal) {
                onBackKeyDownTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "再按一次退出笔迹", Toast.LENGTH_SHORT).show();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 侧边栏

    /**
     * 初始化侧边栏 大小与选中
     */
    private void initNav() {

        // 侧滑菜单宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ViewGroup.LayoutParams params = m_navigationView.getLayoutParams();
        params.width = metrics.widthPixels / 3 * 2;
        m_navigationView.setLayoutParams(params);

        // 默认选中
        m_navigationView.setCheckedItem(R.id.nav_left_main);
        m_navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    /**
     * 打开侧边栏, Frags setNavigationOnClickListener 用
     */
    public void openNavMenu() {
        m_drawerLayout.openDrawer(Gravity.START);
    }

    /**
     * 关闭侧边栏, Frags setNavigationOnClickListener 用
     */
    public void closeNavMenu() {
        m_drawerLayout.closeDrawer(Gravity.START);
    }

    /**
     * 侧边栏菜单
     */
    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = (@NonNull MenuItem item) -> {
        switch (item.getItemId()) {
            case R.id.nav_left_login:
                Nav_Login_Selected();
                break;
            case R.id.nav_left_about:
                Nav_About_Selected();
                break;
            case R.id.nav_left_feedback:
                Nav_Feedback_Selected();
                break;
        }
        return false;
    };

    /**
     * 侧边栏 登录注销
     */
    private void Nav_Login_Selected() {
        closeNavMenu();

        if (m_navigationView.getMenu().findItem(R.id.nav_left_login).getTitle().equals(getString(R.string.nav_login))) {
            // 登录
            Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
            startActivity(authIntent);
        }
        else {
            ProgressHandler.process(this, "注销中...", true,
                AuthService.logout(), new InteractInterface<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        if (data) {
                            showToast(MainActivity.this, "注销成功");
                            AuthManager.getInstance().logout();
                            AuthManager.getInstance().setSpToken(MainActivity.this, "");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        showAlert(MainActivity.this, "错误", message);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        showAlert(MainActivity.this, "错误", "网络错误：" + throwable.getMessage());
                    }
                }
            );
        }
    }

    /**
     * 侧边栏 关于
     */
    private void Nav_About_Selected() {
        closeNavMenu();

        startActivity(new Intent(this, AboutActivity.class));
    }

    /**
     * 侧边栏 反馈
     */
    private void Nav_Feedback_Selected() {
        closeNavMenu();

        String feedbackUrl = "https://github.com/Aoi-hosizora/Biji_Baibuti/issues";
        showBrowser(this, new String[] { feedbackUrl });
    }
}
