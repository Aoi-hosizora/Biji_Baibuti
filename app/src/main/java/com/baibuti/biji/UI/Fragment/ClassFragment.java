package com.baibuti.biji.UI.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baibuti.biji.R;

public class ClassFragment extends Fragment implements View.OnClickListener {

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
            view = inflater.inflate(R.layout.fragment_classtab, container, false);

            ///
            initView();
        }
        return view;
    }

    private void initView() {
        initToolbar(view);


        Button mClassBtn = view.findViewById(R.id.class_button);
        mClassBtn.setOnClickListener(this);
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);

        m_toolbar = view.findViewById(R.id.ClassFrag_Toolbar);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "ddd", Toast.LENGTH_SHORT).show();
            }
        });
        m_toolbar.setTitle(R.string.ClassFrag_Header);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.class_button:
                Toast.makeText(getContext(), "This is class button", Toast.LENGTH_LONG).show();

                break;
        }
    }

}
