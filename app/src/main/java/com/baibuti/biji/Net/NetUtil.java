package com.baibuti.biji.Net;

import com.baibuti.biji.Net.Models.RespType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";

    public static Map<String, String> getOneHeader(String key, String value) {
        Map<String, String> header = new HashMap<>();
        header.put(key, value);
        return header;
    }

    public static RespType httpGetSync(String url) {
        return httpGetSync(url, null, NO_TIME, NO_TIME);
    }

    public static RespType httpGetSync(String url, Map<String, String> headers) {
        return httpGetSync(url, headers, NO_TIME, NO_TIME);
    }

    public static RespType httpGetSync(String url, int TIME_CONN_SEC, int TIME_READ_SEC) {
        return httpGetSync(url, null, TIME_CONN_SEC, TIME_READ_SEC);
    }

    public static RespType httpGetSync(String url, Map<String, String> headers, int TIME_CONN_SEC, int TIME_READ_SEC) {

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

            if (headers != null && !(headers.isEmpty()))
                for (Map.Entry<String, String> header : headers.entrySet())
                    requestBuilder.addHeader(header.getKey(), header.getValue());

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


    ////////////////////////////////////////


    public static RespType httpPostSync(String url, String json) {
        return httpPostSync(url, json, null);
    }

    public static RespType httpPostSync(String url, String json, Map<String, String> headers) {
        return httpPostPutDeleteSync(url, POST, json, headers);
    }

    public static RespType httpPostPutDeleteSync(String url, String Method, String json, Map<String, String> headers) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RespType Ret = null;

        try {
            RequestBody body;
            if (json != null && !(json.isEmpty()))
                body = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            else
                return null;

            // Req Resp
            Request.Builder builder = new Request.Builder().url(url);

            if (Method.equals(PUT)) builder.put(body);
            else if (Method.equals(DELETE)) builder.delete(body);
            else builder.post(body);


            if (headers != null && !(headers.isEmpty()))
                for (Map.Entry<String, String> header : headers.entrySet())
                    builder.addHeader(header.getKey(), header.getValue());

            Request request = builder.build();

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

    ////////////////////////////////////////

    public static RespType httpPostFileSync(String url, String key, File file) {
        return httpPostFileSync(url, key, file, null);
    }

    public static RespType httpPostFileSync(String url, String key, File file, Map<String, String> headers) {
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
            Request.Builder builder = new Request.Builder().url(url).post(requestBody.build());

            if (headers != null && !(headers.isEmpty()))
                for (Map.Entry<String, String> header : headers.entrySet())
                    builder.addHeader(header.getKey(), header.getValue());

            Request request = builder.build();

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
