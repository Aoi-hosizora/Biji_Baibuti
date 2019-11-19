package com.baibuti.biji.util.otherUtil;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class CommonUtil {

    /**
     * 关闭软键盘
     */
    public static void closeSoftKeyInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive() && activity.getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 复制文本
     * @return 是否存在 CLIPBOARD_SERVICE 服务
     */
    public static boolean copyText(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", text);

        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
            return true;
        }
        return false;
    }

    // /**
    //  * 分享文字笔记
    //  */
    // public static void shareText(Context context, String content){
    //     Intent shareIntent = new Intent();
    //     shareIntent.setAction(Intent.ACTION_SEND);
    //     shareIntent.putExtra(Intent.EXTRA_TEXT, content);
    //     shareIntent.setType("text/plain");
    //     //设置分享列表的标题，并且每次都显示分享列表
    //     context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    // }
    //
    // /**
    //  * 分享单张图片
    //  * @param context
    //  * @param imagePath
    //  */
    // public static void shareImage(Context context, String imagePath) {
    //     //String imagePath = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
    //     Uri imageUri = Uri.fromFile(new File(imagePath));//由文件得到uri
    //     Log.d("share", "uri:" + imageUri);  //输出：file:///storage/emulated/0/test.jpg
    //
    //     Intent shareIntent = new Intent();
    //     shareIntent.setAction(Intent.ACTION_SEND);
    //     shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
    //     shareIntent.setType("image/*");
    //     context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    // }

    /**
     * 分享功能
     *
     * @param context   上下文
     * @param msgTitle  消息标题
     * @param msgText   消息内容
     * @param imgPath   图片路径，不分享图片则传null
     */
    public static void shareTextAndImage(Context context, String msgTitle, String msgText, String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals(""))
            intent.setType("text/plain"); // 纯文本
        else {
            File f = new File(imgPath);
            if (f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "分享到"));
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 生成二位码
     * @return null for error
     */
    public static Bitmap generateQrCode(String content, int size, int color) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    if (matrix.get(x, y))
                        pixels[y * width + x] = color;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
