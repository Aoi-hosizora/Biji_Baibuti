package com.baibuti.biji.UI.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.baibuti.biji.R;

public class SearchFragment extends Fragment {

    private View view;
    private Toolbar m_toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_searchtab, container, false);


            initView();

        }
        return view;
    }

    /**
     * 初始化界面
     */
    private void initView() {
        initToolbar(view);
    }

    /**
     * 设置标题工具栏 包括菜单
     * @param view
     */
    private void initToolbar(View view) {
        setHasOptionsMenu(true);

        m_toolbar = view.findViewById(R.id.tab_search_toolbar);
        // m_toolbar.inflateMenu(R.menu.xxxx);
//        m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//            switch (item.getItemId()) {
//                //
//            }
//            return true;
//            }
//        });
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "ddd", Toast.LENGTH_SHORT).show();
            }
        });
        m_toolbar.setTitle(R.string.SearchFrag_Header);
    }
}
