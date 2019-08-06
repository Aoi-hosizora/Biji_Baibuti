package com.baibuti.biji.Net.Modules.Search;

import android.util.Log;

import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchNetUtil {

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
     * @param KeyWord wd
     * @param page pn 1 2 ...
     * @return
     */
    public static ArrayList<SearchItem> getSearchBaiduRet(String KeyWord, int page) {
        String url = String.format(Locale.CHINA, Urls.BaiduUrl, KeyWord, page - 1);
        RespType resp = NetUtil.httpGetSync(url, NetUtil.getOneHeader("User-Agent", NetUtil.DEF_UserAgent), TIME_CONN_1SEC, TIME_READ_1SEC);
        if (resp == null)
            return new ArrayList<>();
        return getParseBaiduRet(resp.getBody());
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
                .connectTimeout(TIME_CONN_1SEC, TimeUnit.SECONDS)
                .readTimeout(TIME_READ_1SEC, TimeUnit.SECONDS)
                .build();

        baiduUrl = baiduUrl.replace("http://", "https://");
        try {
            Request request = new Request.Builder()
                    .url(baiduUrl)
                    .addHeader("User-Agent", NetUtil.DEF_UserAgent)
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
