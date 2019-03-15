package com.baibuti.biji.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baibuti.biji.R;

public class ClassFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.classtab, container, false);

        initView(view);

        return view;
    }

    private void initView(View view){
        Button mClassBtn = view.findViewById(R.id.class_button);
        mClassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "This is class button", Toast.LENGTH_LONG).show();
                //这里处理逻辑
            }
        });
    }
}
