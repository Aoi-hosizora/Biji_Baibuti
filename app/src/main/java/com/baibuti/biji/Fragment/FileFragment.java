package com.baibuti.biji.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baibuti.biji.R;

public class FileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filetab, container, false);

        initToolBar(view);

        return view;
    }

    private void initToolBar(View view){
        Toolbar mToolBar = view.findViewById(R.id.tab_file_toolbar);
        mToolBar.setTitle(R.string.file_header);
        mToolBar.setNavigationIcon(R.drawable.tab_file_menu);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"This is a menu",Toast.LENGTH_LONG).show();
                //添加菜单逻辑
            }
        });
    }
}