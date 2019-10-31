package com.baibuti.biji.net;

public class Urls {

    // TODO dev env
    // https://blog.csdn.net/mazaiting/article/details/72822948

    /**
     * 谷歌模拟器连接电脑本机
     */
    public static final String GOOGLElocalhost = "http://10.0.2.15";

    /**
     * 夜神模拟器连接电脑本机 (无法连接)
     */
    public static final String NOXlocalhost = "http://127.17.100.15";

    /**
     * 临时设置的本地局域网 (调试记得设置)
     */
    public static final String tmpLAN = "http://110.64.87.48";

    public static final String ServerIP = tmpLAN;

    /**
     * 百度搜索页
     */
    public static String BaiduUrl = "https://www.baidu.com/s?wd=%s&pn=%s1";

    /**
     * OCR 服务器的 POST ocr/upload/
     */
    public static final String OCRServerUrl = ServerIP + ":1627/ocr/upload/";

    /**
     * 后端登录注册 URL
     */
    public static final String AuthUrl = ServerIP + ":8001/auth";

    /**
     * 后端笔记 URL
     */
    public static final String NoteUrl = ServerIP + ":8001/note";

    /**
     * 后端分组 URL
     */
    public static final String GroupUrl = ServerIP + ":8001/group";

    /**
     * 后端图片 URL
     */
    public static final String ImageUrl = ServerIP + ":8001/note/img";

    /**
     * 后端收藏 URL
     */
    public static final String StarUrl = ServerIP + ":8001/star";

    /**
     * 后端日志 URL
     */
    public static final String LogUrl = ServerIP + ":8001/log";

    /**
     * 后端文件分类 URL
     */
    public static final String FileClassUrl = ServerIP + ":8001/fileclass";

    /**
     * 后端文件 URL
     */
    public static final String FileUrl = ServerIP + ":8001/file";

    /**
     * 后端课表 URL
     */
    public static final String Schedule = ServerIP + ":8001/schedule";

}