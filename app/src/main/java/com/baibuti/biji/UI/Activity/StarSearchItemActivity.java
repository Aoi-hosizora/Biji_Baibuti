package com.baibuti.biji.UI.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.baibuti.biji.R;
import com.baibuti.biji.Utils.CommonUtil;

public class StarSearchItemActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starsearchitem);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setTitle(R.string.StarSearchItemAct_Title);
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 点击顶部菜单项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        CommonUtil.closeSoftKeyInput(this);

        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.id_menu_modifynote_cancel:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
