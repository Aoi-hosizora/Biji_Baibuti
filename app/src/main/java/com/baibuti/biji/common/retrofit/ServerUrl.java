package com.baibuti.biji.common.retrofit;

public class ServerUrl {

    // https://blog.csdn.net/mazaiting/article/details/72822948

    // /**
    //  * 谷歌模拟器连接电脑本机
    //  */
    // public static final String Google_Adb_Localhost = "http://10.0.2.15";

    /**
     * 临时设置的本地局域网 (调试记得设置)
     */
    private static final String tmpLAN = "http:/132.232.142.7:8001/";

    /**
     * !!! 服务器后端 API 地址
     */
    public static final String BaseServerEndPoint = tmpLAN;

    /**
     * !!! OCR 服务器的 POST ocr/upload/
     */
    public static final String OCRServerEndPoint = BaseServerEndPoint + ":1627/ocr/upload/";

    /**
     * 根据 uuid 获取文档下载地址
     * GET /raw/file/:uuid
     */
    public static String getRawUrl(String uuid) {
        return "/raw/file/" + uuid;
    }

    /**
     * 根据 分享码 获取文档分享地址
     * GET /share/:sc
     */
    public static String getShareCodeUrl(String sc) {
        return "/share/" + sc;
    }
}