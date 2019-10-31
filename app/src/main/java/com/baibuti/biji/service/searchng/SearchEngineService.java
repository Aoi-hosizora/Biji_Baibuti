package com.baibuti.biji.service.searchng;

import android.util.Log;

import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.net.NetHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchEngineService {

    /**
     * 百度搜索页
     */
    private static final String BaiduUrl = "https://www.baidu.com/s?wd=%s&pn=%s1";

    /**
     * 默认 UA
     */
    private static final String DEF_UserAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/75.0.3770.100 Safari/537.36";

    /**
     * 连接超时时间
     */
    private static int TIME_CONN_1SEC = 1;

    /**
     * 读取超时时间
     */
    private static int TIME_READ_1SEC = 1;

    /**
     * 通过关键词搜索百度
     * @param keyword wd
     * @param page pn 1 2 ...
     * @return List(Of SearchItem)
     */
    public static List<SearchItem> getBaiduSearchResult(String keyword, int page) {
        String url = String.format(Locale.CHINA, BaiduUrl, keyword, page - 1);

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIME_CONN_1SEC, TimeUnit.SECONDS)
            .readTimeout(TIME_READ_1SEC, TimeUnit.SECONDS)
            .build();

        try {
            Request request = new Request.Builder()
                .get().url(url)
                .addHeader("User-Agent", DEF_UserAgent)
                .build();

            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null)
                    return parseBaiduSearchResult(body.string());
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 解析百度响应
     * @param Resp http 文档
     * @return List(Of SearchItem)
     */
    private static List<SearchItem> parseBaiduSearchResult(String Resp) {
        List<SearchItem> searchItems = new ArrayList<>();

        try {
            // 预处理响应 (百度的结果可能不存在 rs 块，尽可能缩短结果)
            int content_left_lineCnt = Resp.indexOf("<div id=\"content_left\">");
            int rs_lineCnt = Resp.indexOf("<div id=\"rs\">");

            if (rs_lineCnt > content_left_lineCnt)
                Resp = Resp.substring(content_left_lineCnt, rs_lineCnt);
            else {
                int content_bottom_lineCnt = Resp.indexOf("<div id=\"content_bottom\">");

                if (content_bottom_lineCnt > content_left_lineCnt)
                    Resp = Resp.substring(content_left_lineCnt, content_bottom_lineCnt);
                else
                    Resp = Resp.substring(content_left_lineCnt);
            }

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

                    searchItems.add(new SearchItem(title, content, getBaiduResultRealUrl(url)));
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
     * @param baiduUrl www.baidu.com/link? url=xxx & wd=xxx & eqid=xxx
     */
    private static String getBaiduResultRealUrl(String baiduUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_CONN_1SEC, TimeUnit.SECONDS)
                .readTimeout(TIME_READ_1SEC, TimeUnit.SECONDS)
                .build();

        baiduUrl = baiduUrl.replace("http://", "https://");
        try {
            Request request = new Request.Builder()
                .url(baiduUrl)
                .addHeader("User-Agent", NetHelper.DEF_UserAgent)
                .build();

            Response response = okHttpClient.newCall(request).execute();
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
