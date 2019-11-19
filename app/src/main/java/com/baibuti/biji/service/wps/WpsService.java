package com.baibuti.biji.service.wps;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;

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
}
