package com.baibuti.biji.Utils;

import android.util.Log;

import com.baibuti.biji.Data.Models.SearchItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
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
        Reader resp = getResponse(url);
        return parseBaiduRet(resp);
    }

    /**
     * 获得 url 的响应 html
     * @param url
     * @return
     */
    private static Reader getResponse(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Reader htmlData = null;
        try {
            Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0.1; MI 4LTE Build/MMB29M; wv) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Version/4.0 Chrome/51.0.2704.81 Mobile Safari/537.36")
                .build();

            Response response = okHttpClient.newCall(request).execute();
            htmlData = response.body().charStream();
            // response.close();
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
     * 从 Reader 获取 String
     * @param reader
     * @param pattern
     */
    private static String getUsefulStringFromCharReader(Reader reader, String pattern, int start, int length) {
        char[] cbuf = new char[pattern.length()];
        try {
            Log.e("", "getUsefulStringFromCharReader: " + start + ", " +  length);
            int len = reader.read(cbuf, 0, length);
            String Mokuzen = String.valueOf(cbuf);
            if (Mokuzen.contains(pattern))
                return Mokuzen.substring(Mokuzen.indexOf(pattern)) + reader.read(cbuf);
            else {
                return getUsefulStringFromCharReader(reader, pattern, start + len, length);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 解析百度响应 获得 SearchItem List
     * @param Resp
     * @return
     */
    private static ArrayList<SearchItem> parseBaiduRet(Reader Resp) {
        String pattern = "<div id=\"content_left\"";
        String HTMLDoc = getUsefulStringFromCharReader(Resp, pattern, 0, pattern.length());

        Log.e("", "parseBaiduRet: " + HTMLDoc + ", " + HTMLDoc.length());

        ArrayList<SearchItem> searchItems = new ArrayList<>();
        Document document = Jsoup.parse(HTMLDoc);

        try {
            Elements elements = document.selectFirst("#content_left").children();
            // Log.e("", "parseBaiduRet: " + elements.text() );
            for (Element element : elements) {
                String title = element.selectFirst("div h3").text();
                Element cnt = element.selectFirst("div div.c-abstract");
                String content;
                if (cnt != null)
                    content = cnt.text();
                else
                    content = element.selectFirst("div p").nextElementSibling().text();

                String url = element.selectFirst("div h3 a").attr("href");

                Log.e("", "parseBaiduRet: " + title );
                searchItems.add(new SearchItem(title, url, content));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return searchItems;
    }
}
