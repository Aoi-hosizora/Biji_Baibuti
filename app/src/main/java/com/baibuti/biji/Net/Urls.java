package com.baibuti.biji.Net;

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
    public static final String tmpLAN = "http://192.168.1.117";

    /**
     * OCR 服务器的 POST ocr/upload/
     */
    public static final String OCRServerUrl = tmpLAN + ":1627/ocr/upload/";
}