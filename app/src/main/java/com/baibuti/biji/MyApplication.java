package com.baibuti.biji;

import android.app.Application;

import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.facebook.stetho.Stetho;

import jackmego.com.jieba_android.JiebaSegmenter;
import rx_activity_result2.RxActivityResult;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxActivityResult.register(this);

        // 初始化结巴分词
        // JiebaSegmenter.init(getApplicationContext());

        // FB 数据库查看
        Stetho.initializeWithDefaults(getApplicationContext());
    }
}
