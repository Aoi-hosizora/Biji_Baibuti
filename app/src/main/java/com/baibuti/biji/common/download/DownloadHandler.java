package com.baibuti.biji.common.download;

import com.baibuti.biji.common.retrofit.ServerApi;
import com.baibuti.biji.common.retrofit.ServerUrl;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class DownloadHandler {

    private static final int DEFAULT_TIMEOUT = 15;
    private Retrofit retrofit;
    private JsDownloadListener listener;

    public DownloadHandler(JsDownloadListener listener) {
        this.listener = listener;
        JsDownloadInterceptor mInterceptor = new JsDownloadInterceptor(listener);
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(mInterceptor)
            .retryOnConnectionFailure(true)
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build();

        retrofit = new Retrofit.Builder()
            .baseUrl(ServerUrl.BaseServerEndPoint)
            .client(httpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
    }

    public Observable<InputStream> download(String uuid, final File file) {
        return retrofit.create(ServerApi.class)
            .getRawFile(uuid)
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .map(ResponseBody::byteStream)
            .observeOn(Schedulers.computation()) // 用于计算任务
            .doOnNext((stream) -> writeFile(stream, file))
            .observeOn(AndroidSchedulers.mainThread());
    }

    private void writeFile(InputStream inputString, File file) {
        if (file.exists()) {
            AppPathUtil.deleteFile(file.getAbsolutePath());
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int len;
            while ((len = inputString.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            inputString.close();
            fos.close();

        } catch (FileNotFoundException e) {
            listener.onFail("FileNotFoundException");
        } catch (IOException e) {
            listener.onFail("IOException");
        }
    }
}