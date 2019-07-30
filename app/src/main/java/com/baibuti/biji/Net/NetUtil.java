package com.baibuti.biji.Net;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetUtil {

    /**
     * UA 头
     */
    public static final String DEF_UserAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/75.0.3770.100 Safari/537.36";

    /**
     * 不设置时间
     */
    public static int NO_TIME = 0;

    /**
     * 不设置用户代理
     */
    public static String NO_UA = "";

    /**
     * HTTP GET url (UA)
     * @param url
     * @param UA
     * @return
     */
    public static String httpGet(String url, String UA) {
        return httpGet(url, UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP GET url (not UA)
     * @param url
     * @return
     */
    public static String httpGet(String url) {
        return httpGet(url, NO_UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP GET url (not UA)
     * @param url
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static String httpGet(String url, int TIME_CONN_SEC, int TIME_READ_SEC) {
        return httpGet(url, NO_UA, TIME_CONN_SEC, TIME_READ_SEC);
    }

    /**
     * HTTP GET url (UA)
     * @param url
     * @param UA
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static String httpGet(String url, String UA, int TIME_CONN_SEC, int TIME_READ_SEC) {

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        if (TIME_CONN_SEC != NO_TIME)
            okHttpClientBuilder.connectTimeout(TIME_CONN_SEC, TimeUnit.SECONDS);
        if (TIME_READ_SEC != NO_TIME)
            okHttpClientBuilder.readTimeout(TIME_READ_SEC, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        String Ret = "";

        try {
            // Req Builder
            Request.Builder requestBuilder = new Request.Builder().url(url);

            if (!UA.isEmpty())
                requestBuilder.addHeader("User-Agent", UA);

            // Req Resp
            Request request = requestBuilder.build();
            Response response = okHttpClient.newCall(request).execute();
            Ret = response.body().string();
            response.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return Ret;
    }

    /**
     * HTTP Post File (no UA)
     * @param url
     * @param key
     * @param img
     * @return
     */
    public static String httpPostImg(String url, String key, File img) {
        return httpPostImg(url, key, img, NO_UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP Post File (UA)
     * @param url
     * @param key
     * @param img
     * @param UA
     * @return
     */
    public static String httpPostImg(String url, String key, File img, String UA) {
        return httpPostImg(url, key, img, UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP Post File (no UA)
     * @param url
     * @param key
     * @param img
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static String httpPostImg(String url, String key, File img, int TIME_CONN_SEC, int TIME_READ_SEC) {
        return httpPostImg(url, key, img, NO_UA, TIME_CONN_SEC, TIME_READ_SEC);
    }

    /**
     * HTTP Post File (UA)
     * @param url
     * @param key String
     * @param img File
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static String httpPostImg(String url, String key, File img, String UA, int TIME_CONN_SEC, int TIME_READ_SEC) {
        if (img == null)
            return "";

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        if (TIME_CONN_SEC != NO_TIME)
            okHttpClientBuilder.connectTimeout(TIME_CONN_SEC, TimeUnit.SECONDS);
        if (TIME_READ_SEC != NO_TIME)
            okHttpClientBuilder.readTimeout(TIME_READ_SEC, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        String Ret = "";

        try {
            // File
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), img);
            requestBody.addFormDataPart(key, img.getName(), body);

            // Req Builder
            Request.Builder requestBuilder = new Request.Builder().url(url);

            if (!UA.isEmpty())
                requestBuilder.addHeader("User-Agent", UA);

            requestBuilder.post(requestBody.build());

            // Req Resp
            Request request = requestBuilder.build();
            Response response = okHttpClient.newCall(request).execute();
            Ret = response.body().string();
            response.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return Ret;
    }
}