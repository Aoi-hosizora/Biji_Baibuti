package com.baibuti.biji.service.wps;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;

import com.baibuti.biji.util.otherUtil.DefineString;

import java.io.File;

public class WpsService {

    /**
     * 通过 WPS 打开文档
     */
    public static boolean OpenDocumentThroughWPS(Activity activity, File file) {
        try {
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");
            if (intent == null)
                throw new Exception();

            Bundle bundle = new Bundle();

            // 打开模式
            bundle.putString(DefineString.OPEN_MODE, DefineString.NORMAL);
            bundle.putBoolean(DefineString.ENTER_REVISE_MODE, true); // 修订模式
            bundle.putBoolean(DefineString.SEND_SAVE_BROAD, true);
            bundle.putBoolean(DefineString.SEND_CLOSE_BROAD, true);
            bundle.putBoolean(DefineString.HOME_KEY_DOWN, true);
            bundle.putBoolean(DefineString.BACK_KEY_DOWN, true);
            bundle.putBoolean(DefineString.ENTER_REVISE_MODE, true);
            bundle.putBoolean(DefineString.IS_SHOW_VIEW, false);
            bundle.putBoolean(DefineString.AUTO_JUMP, true);
            bundle.putString(DefineString.THIRD_PACKAGE, activity.getPackageName()); // 设置广播
            intent.setAction(Intent.ACTION_VIEW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(activity, "com.baibuti.biji.FileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(contentUri, "*/*");
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
            }
            intent.putExtras(bundle);
            activity.startActivity(intent);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * https://blog.csdn.net/luoyayun361/article/details/82746576
     * 获取 Mime
     */
    public static String getMIMEType(File file) {
        String type = "*/*";
        int dotIndex = file.getName().lastIndexOf(".");
        if (dotIndex < 0)
            return type;

        String fileType = file.getName().substring(dotIndex).toLowerCase();

        for (String[] kv : MIME_MapTable)
            if (fileType.equals(kv[0]))
                type = kv[1];
        return type;
    }

    /**
     * Ext + MIME
     */
    private static final String[][] MIME_MapTable = {
        {".3gp", "video/3gpp"},
        {".apk", "application/vnd.android.package-archive"},
        {".asf", "video/x-ms-asf"},
        {".avi", "video/x-msvideo"},
        {".bin", "application/octet-stream"},
        {".bmp", "image/bmp"},
        {".c", "text/plain"},
        {".class", "application/octet-stream"},
        {".conf", "text/plain"},
        {".cpp", "text/plain"},
        {".doc", "application/msword"},
        {".docx", "application/msword"},
        {".exe", "application/octet-stream"},
        {".gif", "image/gif"},
        {".gtar", "application/x-gtar"},
        {".gz", "application/x-gzip"},
        {".h", "text/plain"},
        {".htm", "text/html"},
        {".html", "text/html"},
        {".jar", "application/java-archive"},
        {".java", "text/plain"},
        {".jpeg", "image/jpeg"},
        {".JPEG", "image/jpeg"},
        {".jpg", "image/jpeg"},
        {".js", "application/x-javascript"},
        {".log", "text/plain"},
        {".m3u", "audio/x-mpegurl"},
        {".m4a", "audio/mp4a-latm"},
        {".m4b", "audio/mp4a-latm"},
        {".m4p", "audio/mp4a-latm"},
        {".m4u", "video/vnd.mpegurl"},
        {".m4v", "video/x-m4v"},
        {".mov", "video/quicktime"},
        {".mp2", "audio/x-mpeg"},
        {".mp3", "audio/x-mpeg"},
        {".mp4", "video/mp4"},
        {".mpc", "application/vnd.mpohun.certificate"},
        {".mpe", "video/mpeg"},
        {".mpeg", "video/mpeg"},
        {".mpg", "video/mpeg"},
        {".mpg4", "video/mp4"},
        {".mpga", "audio/mpeg"},
        {".msg", "application/vnd.ms-outlook"},
        {".ogg", "audio/ogg"},
        {".pdf", "application/pdf"},
        {".png", "image/png"},
        {".pps", "application/vnd.ms-powerpoint"},
        {".ppt", "application/vnd.ms-powerpoint"},
        {".pptx", "application/vnd.ms-powerpoint"},
        {".prop", "text/plain"},
        {".rar", "application/x-rar-compressed"},
        {".rc", "text/plain"},
        {".rmvb", "audio/x-pn-realaudio"},
        {".rtf", "application/rtf"},
        {".sh", "text/plain"},
        {".tar", "application/x-tar"},
        {".tgz", "application/x-compressed"},
        {".txt", "text/plain"},
        {".wav", "audio/x-wav"},
        {".wma", "audio/x-ms-wma"},
        {".wmv", "audio/x-ms-wmv"},
        {".wps", "application/vnd.ms-works"},
        //{".xml",    "text/xml"},
        {".xml", "text/plain"},
        {".z", "application/x-compress"},
        {".zip", "application/zip"},
        {"", "*/*"}
    };
}
