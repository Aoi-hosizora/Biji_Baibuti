package com.baibuti.biji.Net;

import com.baibuti.biji.Net.Models.RespType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
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
     * HTTP sync GET url (UA)
     * @param url
     * @param UA
     * @return
     */
    public static RespType httpGetSync(String url, String UA) {
        return httpGetSync(url, UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP sync GET url (not UA)
     * @param url
     * @return
     */
    public static RespType httpGetSync(String url) {
        return httpGetSync(url, NO_UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP sync GET url (not UA)
     * @param url
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static RespType httpGetSync(String url, int TIME_CONN_SEC, int TIME_READ_SEC) {
        return httpGetSync(url, NO_UA, TIME_CONN_SEC, TIME_READ_SEC);
    }

    /**
     * HTTP sync GET url (UA)
     * @param url
     * @param UA
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static RespType httpGetSync(String url, String UA, int TIME_CONN_SEC, int TIME_READ_SEC) {

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        if (TIME_CONN_SEC != NO_TIME)
            okHttpClientBuilder.connectTimeout(TIME_CONN_SEC, TimeUnit.SECONDS);
        if (TIME_READ_SEC != NO_TIME)
            okHttpClientBuilder.readTimeout(TIME_READ_SEC, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        RespType Ret = null;

        try {
            // Req Builder
            Request.Builder requestBuilder = new Request.Builder().url(url);

            if (!UA.isEmpty())
                requestBuilder.addHeader("User-Agent", UA);

            // Req Resp
            Request request = requestBuilder.build();
            Response response = okHttpClient.newCall(request).execute();
            Ret = new RespType(response.code(), response.headers(), response.body().string());
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
     * HTTP sync Post (json)
     * @param url
     * @param json
     * @return
     */
    public static RespType httpPostSync(String url, String json) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RespType Ret = null;

        try {
            RequestBody body;
            if (json != null && !(json.isEmpty()))
                body = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            else
                return null;

            // Req Resp
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            Ret = new RespType(response.code(), response.headers(), response.body().string());
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
     * HTTP sync Post (file)
     * @param url
     * @param key
     * @param file
     * @return
     */
    public static RespType httpPostSync(String url, String key, File file) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RespType Ret = null;

        try {
            // File
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);

            if (key != null && !(key.isEmpty()) && file != null) {
                RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
                requestBody.addFormDataPart(key, file.getName(), body);
            }
            else
                return null;

            // Req Resp
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody.build())
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            Ret = new RespType(response.code(), response.headers(), response.body().string());
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
