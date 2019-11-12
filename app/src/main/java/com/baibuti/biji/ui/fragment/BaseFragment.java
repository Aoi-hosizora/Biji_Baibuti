package com.baibuti.biji.ui.fragment;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    /**
     * 主活动按返回键时的操作，需要只执行一个操作并返回是否执行了
     * @return 是否响应了返回键
     */
    public abstract boolean onBackPressed();
}
