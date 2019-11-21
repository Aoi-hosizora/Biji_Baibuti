package com.baibuti.biji.service.doc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.baibuti.biji.R;

import java.io.File;

/**
 * 根据 https://blog.csdn.net/shaoyezhangliwei/article/details/78273737 修改
 */
public class DocService {

    public static boolean openFile(Context context, File file) {

        // FileUriExposedException
        // android.content.ActivityNotFoundException:
        // No Activity found to handle Intent
        // { act=android.intent.action.VIEW cat=[android.intent.category.DEFAULT] dat=content://com.baibuti.biji.FileProvider/ }

        // SDK24 以上，不允许使用 file:// 传递 url

        String path = file.getAbsolutePath();
        String format = path.substring(path.lastIndexOf(".") + 1);
        try {
            if (file.exists()) {
                switch (format.toLowerCase()) {
                    case "txt":
                        context.startActivity(getTextFileIntent(context, file));
                        break;
                    case "doc":
                    case "docx":
                        context.startActivity(getWordFileIntent(context, file));
                        break;
                    case "xls":
                    case "xlsx":
                        context.startActivity(getExcelFileIntent(context, file));
                        break;
                    case "ppt":
                    case "pptx":
                        context.startActivity(getPptFileIntent(context, file));
                        break;
                    case "pdf":
                        context.startActivity(getPdfFileIntent(context, file));
                    case "zip":
                    case "rar":
                        context.startActivity(getZipRarFileIntent(context, file));
                        break;
                    case "png":
                    case "jpg":
                    case "jpeg":
                    case "bmp":
                        context.startActivity(getPictureFileIntent(context, file));
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

    private static Uri getUriForFile(Context context, File file) {
        Uri uri = null;
        if (context != null && file != null) {
            if (Build.VERSION.SDK_INT >= 24)
                uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getString(R.string.file_provider), file);
            else
                uri = Uri.fromFile(file);
        }
        return uri;
    }

    private static Intent getPptFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    private static Intent getExcelFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    private static Intent getWordFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    private static Intent getTextFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "text/plain");
        return intent;
    }

    private static Intent getPdfFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    private static Intent getPictureFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    private static Intent getZipRarFileIntent(Context context, File file) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = getUriForFile(context, file);
        intent.setDataAndType(uri, "application/x-gzip");
        return intent;
    }
}
