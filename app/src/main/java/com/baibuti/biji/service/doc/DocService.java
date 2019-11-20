package com.baibuti.biji.service.doc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * 根据 https://blog.csdn.net/shaoyezhangliwei/article/details/78273737 修改
 */
public class DocService {

    public static boolean openFile(Context context, String path) {

        String format = path.substring(path.lastIndexOf(".") + 1);
        File file = new File(path);
        try {
            if (file.exists()) {
                switch (format.toLowerCase()) {
                    case "txt":
                        context.startActivity(getTextFileIntent(path));
                        break;
                    case "doc":
                    case "docx":
                        context.startActivity(getWordFileIntent(path));
                        break;
                    case "xls":
                    case "xlsx":
                        context.startActivity(getExcelFileIntent(path));
                        break;
                    case "ppt":
                    case "pptx":
                        context.startActivity(getPptFileIntent(path));
                        break;
                    case "pdf":
                        context.startActivity(getPdfFileIntent(path));
                    case "zip":
                    case "rar":
                        context.startActivity(getZipRarFileIntent(path));
                        break;
                    case "png":
                    case "jpg":
                    case "jpeg":
                    case "bmp":
                        context.startActivity(getPictureFileIntent(path));
                        break;
                    default:
                        return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Intent getPptFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    private static Intent getExcelFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    private static Intent getWordFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    private static Intent getChmFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    private static Intent getTextFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.parse(param);
        intent.setDataAndType(uri, "text/plain");
        return intent;
    }

    private static Intent getPdfFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    private static Intent getPictureFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    private static Intent getZipRarFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/x-gzip");
        return intent;
    }
}
