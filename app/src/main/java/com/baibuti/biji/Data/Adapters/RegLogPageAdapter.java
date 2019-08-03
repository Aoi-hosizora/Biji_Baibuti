package com.baibuti.biji.Data.Adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.baibuti.biji.R;
import com.baibuti.biji.UI.Fragment.LoginFragment;
import com.baibuti.biji.UI.Fragment.RegisterFragment;

public class RegLogPageAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[] {
            R.string.RegLogAct_Login,
            R.string.RegLogAct_Register
    };

    private final Context m_Context;

    public RegLogPageAdapter(Context context, FragmentManager fm) {
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