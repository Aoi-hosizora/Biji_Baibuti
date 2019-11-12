package com.baibuti.biji.util.stringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * 切割字符串，将文本和img标签碎片化，ab<img>cd"转换为"ab"、"<img>"、"cd"
     * @param targetStr 要处理的字符串
     */
    public static List<String> cutStringByImgTag(String targetStr) {
        List<String> splitTextList = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img.*?src=\"(.*?)\".*?>");
        Matcher matcher = pattern.matcher(targetStr);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                splitTextList.add(targetStr.substring(lastIndex, matcher.start()));
            }
            splitTextList.add(targetStr.substring(matcher.start(), matcher.end()));
            lastIndex = matcher.end();
        }
        if (lastIndex != targetStr.length()) {
            splitTextList.add(targetStr.substring(lastIndex));
        }
        return splitTextList;
    }

    /**
     * 获取img标签中的src值
     */
    public static String getImgSrc(String content) {
        String str_src = null;
        Pattern p_img = Pattern.compile("<(img|IMG)(.*?)(/>|></img>|>)");
        Matcher m_img = p_img.matcher(content);
        boolean result_img = m_img.find();
        if (result_img) {
            while (result_img) {
                String str_img = m_img.group(2);
                Pattern p_src = Pattern.compile("(src|SRC)=([\"\'])(.*?)([\"\'])");
                Matcher m_src = p_src.matcher(str_img);
                if (m_src.find()) {
                    str_src = m_src.group(3);
                }
                result_img = m_img.find();
            }
        }
        return str_src;
    }

    /**
     * 从html文本中提取图片地址，或者文本内容
     * @param html 传入html文本
     * @param isGetImage true获取图片，false获取文本
     */
    public static List<String> getTextFromHtml(String html, boolean isGetImage){
        List<String> imageList = new ArrayList<>();
        List<String> textList = new ArrayList<>();

        List<String> list = cutStringByImgTag(html);
        for (int i = 0; i < list.size(); i++) {
            String text = list.get(i);
            if (text.contains("<img") && text.contains("src=")) {
                String imagePath = getImgSrc(text);
                imageList.add(imagePath);
            } else {
                textList.add(text);
            }
        }

        if (isGetImage)
            return imageList;
        else
            return textList;
    }
}
