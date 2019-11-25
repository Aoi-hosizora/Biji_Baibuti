package com.baibuti.biji.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.ui.IContextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity implements IContextHelper {

    @BindView(R.id.id_about_txt_author)
    TextView m_txt_author;

    @BindView(R.id.id_about_txt_version)
    TextView m_txt_version;

    @BindView(R.id.id_about_txt_copyright)
    TextView m_txt_copyright;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("关于笔迹");

        m_txt_author.setText("By SCUT 2017\\n" +
            "郑东润 郭晓凡 刘莹灿 蔡镇峰 陈楷婷\\n" +
            "Of Baibuti 2018-2019\\n" +
            "https://github.com/Aoi-hosizora/Biji_Baibuti");
        m_txt_version.setText("V1.2.0");
        m_txt_copyright.setText("Copyright © 2018-2019 | All Rights Reserved.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    /**
     * 检查版本
     */
    @OnClick(R.id.id_about_btn_version)
    void ButtonCheckVersion_Clicked() {
        showBrowser(this, new String[]{"https://github.com/Aoi-hosizora/Biji_Baibuti/releases"});
    }

    /**、
     *反馈
     */
    @OnClick(R.id.id_about_btn_feedback)
    void ButtonFeedback_Clicked() {
        showBrowser(this, new String[]{"https://github.com/Aoi-hosizora/Biji_Baibuti/issues"});

    }
}
