package com.baibuti.biji.util.strSrchUtil;

import android.content.Context;

import com.baibuti.biji.model.po.ISearchEntity;

import java.util.ArrayList;
import java.util.List;

import jackmego.com.jieba_android.JiebaSegmenter;

public class SearchUtil {

    /**
     * 初始化结巴分词
     */
    public static void initJieba(Context context) {
        new Thread(() -> JiebaSegmenter.init(context)).start();
    }

    /**
     * 进行结巴分词
     * @param str 长句
     * @return 切出来的词组
     */
    private static List<String> jieba(String str) {
        return JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(str);
    }

    /**
     * 从实现了 ISearchEntity 的元素列表中利用分词搜索包含关键词的子列表
     * @param listItems 多个搜索项
     * @param keyword 搜索关键词
     * @return 符合关键词的搜索项
     */
    public static <T> List<T> getSearchItems(T[] listItems, String keyword) {
        if (listItems == null || !(listItems[0] instanceof ISearchEntity))
            return null;

        // 返回的词组
        List<T> words = new ArrayList<>();

        // 符合的搜索项
        List<String> jiebaList = jieba(keyword);

        for (T item : listItems) {
            String content = ((ISearchEntity)item).getSearchContent();
            boolean flag = true;

            // 是否不包含关键词
            for (String token : jiebaList)
                if (!content.toLowerCase().contains(token.toLowerCase()))
                    flag = false;

            if (flag)
                words.add(item);
        }
        return words;
    }
}