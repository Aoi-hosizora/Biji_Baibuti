package com.baibuti.biji.UI.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.baibuti.biji.Data.Adapters.RegLogPageAdapter;
import com.baibuti.biji.R;

import java.util.Locale;

public class RegLogActivity extends AppCompatActivity {

    private ViewPager m_viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reglog);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupTabLayout();
        setTitle("登录笔迹");
    }

    private void setupTabLayout() {
        RegLogPageAdapter m_regLogPageAdapter = new RegLogPageAdapter(this, getSupportFragmentManager());

        m_viewPager = findViewById(R.id.id_RegLogAct_ViewPager);
        m_viewPager.setAdapter(m_regLogPageAdapter);
        m_viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle(String.format(Locale.CHINA, "%s笔迹", getString(RegLogPageAdapter.TAB_TITLES[position])));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout m_tabs = findViewById(R.id.id_RegLogAct_TabLayout);
        m_tabs.setupWithViewPager(m_viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            break;
        }
        return true;
    }

    public void openLogin() {
        m_viewPager.setCurrentItem(0);
    }

    public void openRegister() {
        m_viewPager.setCurrentItem(1);
    }
}
