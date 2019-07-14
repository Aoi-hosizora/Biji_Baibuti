package com.baibuti.biji.Utils;

import com.baibuti.biji.Data.Models.SearchItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchNetUtil {

    /**
     * 百度搜索页
     */
    private static String BaiduUrl = "https://www.baidu.com/s?wd=%s";

    /**
     * 通过关键词搜索百度
     * @param KeyWord
     * @return
     */
    public static ArrayList<SearchItem> SearchBaidu(String KeyWord) {
        String url = String.format(Locale.CHINA, BaiduUrl, KeyWord);
        return parseBaiduRet(getResponse("https://www.bilibili.com"));
    }

    /**
     * 获得 url 的响应 html
     * @param url
     * @return
     */
    private static String getResponse(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String htmlData = "";
        try {
            Request request = new Request.Builder().url(url).build();

            Response response = okHttpClient.newCall(request).execute();
            htmlData = response.body().string();
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
        return htmlData;
    }

    /**
     * 解析百度响应 获得 SearchItem List
     * @param Resp
     * @return
     */
    private static ArrayList<SearchItem> parseBaiduRet(String Resp) {
        ArrayList<SearchItem> searchItems = new ArrayList<>();

        Document document = Jsoup.parse(Resp);
        // Elements elements = document.select("div.panel").first().select("ul.title a");

//        for (Element element : elements) {
//            searchItems.add(element.text());
//        }
        searchItems.add(new SearchItem("1", "2", "3"));

        return searchItems;
    }
}
