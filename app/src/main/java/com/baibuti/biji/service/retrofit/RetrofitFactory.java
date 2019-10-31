package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.service.Urls;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    private RetrofitFactory() { }

    private static RetrofitFactory Instance;

    public static RetrofitFactory getInstance() {
        if (Instance == null) {
            Instance = new RetrofitFactory();
        }
        return Instance;
    }

    /**
     * 获取可变变量的头
     * @param key_value 成对 Key Value
     * @return Map(Of String, String)
     */
    public static Map<String, String> getHeader(String... key_value) {
        Map<String, String> kv = new HashMap<>();
        if (key_value.length % 2 == 0)
            for (int i = 0; i < key_value.length / 2; i++)
                kv.put(key_value[i * 2], key_value[i * 2 + 1]);
        return kv;
    }

    /**
     * 公开接口，与 RetrofitInterface 接口连接
     * @param kv 委托设置请求头
     * @return Retrofit 实例
     */
    public synchronized RetrofitInterface createRequest(Map<String, String> kv) {
        return getRetrofit(kv).create(RetrofitInterface.class);
    }

    /**
     * 设置请求头
     * @param kv 请求头
     * @return OkHttpClient.Builder().build()
     */
    private synchronized OkHttpClient getOkHttpClient(Map<String, String> kv) {
        return new OkHttpClient.Builder()
            .addInterceptor((Interceptor.Chain chain) -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                if (kv != null && !(kv.isEmpty()))
                    for (Map.Entry<String, String> header : kv.entrySet())
                        requestBuilder.addHeader(header.getKey(), header.getValue());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            })
            .build();
    }

    /**
     * 设置序列化和回调处理
     * @param kv 委托设置请求头
     * @return Retrofit.Builder().build()
     */
    private synchronized Retrofit getRetrofit(Map<String, String> kv) {
        return new Retrofit.Builder()
            .baseUrl(Urls.ServerHost)
            .client(getOkHttpClient(kv))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();
    }
}
