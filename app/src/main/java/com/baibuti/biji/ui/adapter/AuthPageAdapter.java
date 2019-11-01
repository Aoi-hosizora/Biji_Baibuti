package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.baibuti.biji.R;
import com.baibuti.biji.ui.fragment.LoginFragment;
import com.baibuti.biji.ui.fragment.RegisterFragment;

public class AuthPageAdapter extends FragmentPagerAdapter {

    @StringRes
    public static final int[] TAB_TITLES = new int[] {
        R.string.RegLogAct_Login,
        R.string.RegLogAct_Register
    };

    private final Context m_Context;

    public AuthPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        m_Context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LoginFragment();
            case 1:
                return new RegisterFragment();
        }
        return new Fragment();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return m_Context.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}