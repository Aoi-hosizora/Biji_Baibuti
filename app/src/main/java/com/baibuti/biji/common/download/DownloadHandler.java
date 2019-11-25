package com.baibuti.biji.common.download;

import com.baibuti.biji.common.auth.AuthManager;
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

/**
 * https://blog.csdn.net/qq_34261214/article/details/81487110
 */
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

    /**
     * 下载文件
     * @param url 端点地址
     * @param file 包含本地文件路径
     * @param hasToken 是否需要认证
     */
    public Observable<InputStream> download(String url, final File file, boolean hasToken) {
        Observable<ResponseBody> observable;
        if (hasToken) {
            String token = "Bearer " + AuthManager.getInstance().getToken();
            observable = retrofit.create(ServerApi.class).downloadWithToken(token, url);
        } else {
            observable = retrofit.create(ServerApi.class).download(url);
        }

        return observable
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .map(ResponseBody::byteStream)
            .observeOn(Schedulers.computation())
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