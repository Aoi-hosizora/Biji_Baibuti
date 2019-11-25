package com.baibuti.biji.common.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class JsDownloadInterceptor implements Interceptor {
    private JsDownloadListener downloadListener;

    JsDownloadInterceptor(JsDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    @EverythingIsNonNull
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
            new JsResponseBody(response.body(), downloadListener)).build();
    }
}