package com.baibuti.biji.Utils;

import android.util.Log;

import com.baibuti.biji.Data.Models.SearchItem;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchNetUtil {

    /**
     * 百度搜索页
     */
    private static String BaiduUrl = "https://www.baidu.com/s?wd=%s&pn=%s1";

    /**
     * UA 头
     */
    private static String UserAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/75.0.3770.100 Safari/537.36";

    /**
     * 连接超时时间
     */
    private static int TIME_CONN_SEC = 2;
    /**
     * 读取超时时间
     */
    private static int TIME_READ_SEC = 2;


    /**
     * 通过关键词搜索百度
     * @param KeyWord wd
     * @param page pn 1 2 ...
     * @return
     */
    public static ArrayList<SearchItem> getSearchBaiduRet(String KeyWord, int page) {
        String url = String.format(Locale.CHINA, BaiduUrl, KeyWord, page - 1);
        return getParseBaiduRet(getResponse(url));
    }

    /**
     * 获得 url 的响应 html
     * @param url
     * @return
     */
    private static String getResponse(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_CONN_SEC, TimeUnit.SECONDS)
                .readTimeout(TIME_READ_SEC, TimeUnit.SECONDS)
                .build();

        String Ret = "";

        // Reader htmlData = null;
        try {
            Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", UserAgent)
                .build();

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
     * 解析百度响应
     * @param Resp
     * @return
     */
    private static ArrayList<SearchItem> getParseBaiduRet(String Resp) {
        // Log.e( ""+Resp.length(), "parseBaiduRet: " + Resp );
        ArrayList<SearchItem> searchItems = new ArrayList<>();

        try {

            // 预处理响应
            int content_left_lineCnt = Resp.indexOf("<div id=\"content_left\">");
            int rs_lineCnt = Resp.indexOf("<div id=\"rs\">");
            Resp = Resp.substring(content_left_lineCnt, rs_lineCnt);

            // Log.e("", "parseBaiduRet: " + Resp);

            // 解析
            Document document = Jsoup.parse(Resp);

            Elements elements = document.select("div#content_left").first().children();

            // 添加
            for (Element element : elements) {

                try {
                    String title = element.select("div h3").text();

                    Elements cnt = element.select("div div.c-abstract");
                    String content;
                    if (cnt != null)
                        content = cnt.text();
                    else
                        content = element.select("div p").next().text();

                    String url = element.select("div h3 a").attr("href");

                    if (!url.contains("http://") && !url.contains("https://"))
                        throw new Exception();

                    Log.e("", "addToParseBaiduRet: " + url);

                    searchItems.add(new SearchItem(title, content, getShinUrlFromBaiduUrl(url)));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    // continue;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return searchItems;
    }

    /**
     * 从 百度 Url 转换成 真正的 Url
     * @param baiduUrl
     * @return
     */
    private static String getShinUrlFromBaiduUrl(String baiduUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_CONN_SEC, TimeUnit.SECONDS)
                .readTimeout(TIME_READ_SEC, TimeUnit.SECONDS)
                .build();

        baiduUrl = baiduUrl.replace("http://", "https://");
        try {
            Request request = new Request.Builder()
                    .url(baiduUrl)
                    .addHeader("User-Agent", UserAgent)
                    .build()
                    ;

            Response response = okHttpClient.newCall(request).execute();

//            Log.e("", "getShinUrlFromBaiduUrl: " + response.request().url().toString());

            return response.request().url().toString();
        }
        catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            return baiduUrl;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return baiduUrl;
    }
}
