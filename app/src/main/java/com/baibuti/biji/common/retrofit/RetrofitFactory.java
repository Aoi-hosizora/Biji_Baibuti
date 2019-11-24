package com.baibuti.biji.common.retrofit;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
     * !!!!!!
     * 重载 EndPoint，使用默认 API 地址，与 ServerApi 接口连接
     * @param kv 委托设置请求头
     * @return Retrofit 实例
     */
    public synchronized ServerApi createRequest(Map<String, String> kv) {
        return createRequest(kv, ServerUrl.BaseServerEndPoint, ServerApi.class);
    }

    /**
     * !!!!!!
     * 公开接口，与 ServerApi 接口连接
     * @param kv 委托设置请求头
     * @param endPoint 接口地址
     * @param tClass 接口 Class
     * @return Retrofit 实例
     */
    public synchronized <T> T createRequest(Map<String, String> kv, String endPoint, Class<T> tClass) {
        return getRetrofit(kv, endPoint).create(tClass);
    }

    /**
     * !!!!!!
     * 设置序列化和回调处理
     * @param kv 委托设置请求头
     * @return Retrofit.Builder().build()
     */
    private synchronized Retrofit getRetrofit(Map<String, String> kv, String endPoint) {
        return new Retrofit.Builder()
            .baseUrl(endPoint)
            .client(getOkHttpClient(kv))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
    }

    /**
     * !!!!!!
     * 设置请求头和 OkHttpClient 配置
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
}
