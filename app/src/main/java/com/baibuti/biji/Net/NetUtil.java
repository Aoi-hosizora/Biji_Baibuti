package com.baibuti.biji.Net;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
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
    public static String httpGetSync(String url, String UA) {
        return httpGetSync(url, UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP sync GET url (not UA)
     * @param url
     * @return
     */
    public static String httpGetSync(String url) {
        return httpGetSync(url, NO_UA, NO_TIME, NO_TIME);
    }

    /**
     * HTTP sync GET url (not UA)
     * @param url
     * @param TIME_CONN_SEC
     * @param TIME_READ_SEC
     * @return
     */
    public static String httpGetSync(String url, int TIME_CONN_SEC, int TIME_READ_SEC) {
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
    public static String httpGetSync(String url, String UA, int TIME_CONN_SEC, int TIME_READ_SEC) {

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
     * HTTP sync Post (no file, has json)
     * @param url
     * @param json
     * @return
     */
    public static String httpPostSync(String url, Map<String, String> json) {
        return httpPostSync(url, json, null, null);
    }

    /**
     * HTTP sync Post (no json, has file)
     * @param url
     * @param key
     * @param file
     * @return
     */
    public static String httpPostSync(String url, String key, File file) {
        return httpPostSync(url, null, key, file);
    }

    /**
     * HTTP sync Post (has file, has file)
     * @param url
     * @param json
     * @param key
     * @param file
     * @return
     */
    public static String httpPostSync(String url, Map<String, String> json, String key, File file) {

        OkHttpClient okHttpClient = new OkHttpClient();
        String Ret = "";

        try {
            // File
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);

            if (key != null && !key.isEmpty() && file != null) {
                RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
                requestBody.addFormDataPart(key, file.getName(), body);
            }

            if (json != null && !json.isEmpty()) {
                for (Map.Entry<String, String> item : json.entrySet()) {
                    RequestBody body = RequestBody.create(null, "none");
                    requestBody.addPart(Headers.of(item.getKey(), item.getValue()), body);
                }
            }

            // Req Builder
            Request.Builder requestBuilder = new Request.Builder().url(url);

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
