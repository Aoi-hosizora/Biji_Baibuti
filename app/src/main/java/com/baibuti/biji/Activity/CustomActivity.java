package com.baibuti.biji.Activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.baibuti.biji.R;
import com.previewlibrary.GPreviewActivity;

public class CustomActivity extends GPreviewActivity {
    /***
     * 重复该方法 *使用你的自定义布局
     ***/
    @Override
    public int setContentLayout() {
        return R.layout.activity_custom_preview;
    }
}