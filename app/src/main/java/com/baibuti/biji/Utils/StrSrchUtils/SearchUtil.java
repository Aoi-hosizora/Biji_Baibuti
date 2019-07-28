package com.baibuti.biji.Utils.StrSrchUtils;

import android.content.Context;
import android.util.Log;

import com.baibuti.biji.Interface.ISearchEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jackmego.com.jieba_android.JiebaSegmenter;

public class SearchUtil {

    /**
     * 初始化结巴分词
     * @param context
     */
    public static void initJieba(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JiebaSegmenter.init(context);
            }
        }).start();
    }

    /**
     * 进行结巴分词
     * @param str
     * @return
     */
    public static List<String> jieba(String str) {
        return JiebaSegmenter.getJiebaSegmenterSingleton().getDividedString(str);
    }


    /**
     * 从实现了 ISearchEntity 的元素列表中利用分词搜索包含关键词的子列表
     * @param listItems
     * @param keyword
     * @return
     */
    public static <T> ArrayList<T> getSearchItems(T[] listItems, String keyword) {
        if (listItems == null || !(listItems[0] instanceof ISearchEntity))
            return null;

        ArrayList<T> ret = new ArrayList<>();

        List<String> jiebaList = jieba(keyword);

        Log.e("getSearchItems“", "getSearchItems: " + jiebaList);

        // forList:
        for (T item : listItems) {
            String content = ((ISearchEntity)item).getSearchContent();
            boolean flag = true; // 是否全部包含
            for (String token : jiebaList) {
                if (!content.toLowerCase().contains(token.toLowerCase())) {
                    flag = false;
                }
            }
            if (flag) ret.add(item); // 满足所有切词
        }
        return ret;
    }
}