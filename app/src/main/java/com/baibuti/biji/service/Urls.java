package com.baibuti.biji.service;

public class Urls {

    // TODO dev env
    // https://blog.csdn.net/mazaiting/article/details/72822948

    /**
     * 谷歌模拟器连接电脑本机
     */
    public static final String Google_Adb_Localhost = "http://10.0.2.15";

    /**
     * 夜神模拟器连接电脑本机 (无法连接)
     */
    public static final String Nox_Adb_Localhost = "http://127.17.100.15";

    /**
     * 临时设置的本地局域网 (调试记得设置)
     */
    private static final String tmpLAN = "http://110.64.87.48";

    /**
     * !!! 服务器后端 API 地址
     */
    public static final String BaseServerEndPoint = tmpLAN;

    /**
     * !!! OCR 服务器的 POST ocr/upload/
     */
    public static final String OCRServerEndPoint = BaseServerEndPoint + ":1627/ocr/upload/";

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 后端图片 URL
     */
    @Deprecated
    public static final String ImageUrl = BaseServerEndPoint + ":8001/note/img";

    /**
     * 后端日志 URL
     */
    @Deprecated
    public static final String LogUrl = BaseServerEndPoint + ":8001/log";

    /**
     * 后端文件分类 URL
     */
    @Deprecated
    public static final String FileClassUrl = BaseServerEndPoint + ":8001/fileclass";

    /**
     * 后端文件 URL
     */
    @Deprecated
    public static final String FileUrl = BaseServerEndPoint + ":8001/file";

    /**
     * 后端课表 URL
     */
    @Deprecated
    public static final String Schedule = BaseServerEndPoint + ":8001/schedule";

}