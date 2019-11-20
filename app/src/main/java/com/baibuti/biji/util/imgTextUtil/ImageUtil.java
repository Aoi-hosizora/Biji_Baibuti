package com.baibuti.biji.util.imgTextUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * 读取写入 Bitmap & 压缩图片
 */
public class ImageUtil {

    /**
     * 通过文件路径来获取Bitmap
     */
    public static Bitmap getBitmapFromPath(String pathName) {
        return BitmapFactory.decodeFile(pathName);
    }

    /**
     * 存储 bitmap
     */
    public static void saveBitmap(Bitmap bitmap, String pathName) {
        File bf = new File(pathName);
        if (bf.exists() && !bf.delete())
            return;

        try {
            FileOutputStream out = new FileOutputStream(bf);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 通过路径返回图片大小
     */
    static Size getImgSize(String pathName) {
        // 图片信息
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opt);

        // 图片尺寸
        return new Size(opt.outWidth, opt.outHeight);
    }

    /**
     * 根据默认质量 (90) 压缩图片
     */
    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] bytes = stream.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // /**
    //  * 根据质量压缩图片
    //  */
    // private static Bitmap compressImage(Bitmap bitmap, int quality) {
    //     ByteArrayOutputStream stream = new ByteArrayOutputStream();
    //     bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
    //     byte[] bytes = stream.toByteArray();
    //     return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    // }

    // /**
    //  * 根据等比率压缩图片
    //  */
    // public static Bitmap compressImage(Bitmap bitmap, double rate) {
    //     return compressImage(bitmap, rate, rate);
    // }

    /**
     * 根据不等比率压缩图片
     */
    private static Bitmap compressImage(Bitmap bitmap, double rateX, double rateY) {
        Matrix matrix = new Matrix();
        matrix.setScale((float) rateX, (float) rateY);
        return Bitmap.createBitmap(bitmap,
            0, 0,
            bitmap.getWidth(), bitmap.getHeight(),
            matrix, true);
    }

    /**
     * 根据大小不等比例压缩图片
     * @param isKeep 是否等比例，如果不等比例则按照更小的尺寸压缩
     */
    public static Bitmap compressImage(Bitmap bitmap, int newWidth, int newHeight, boolean isKeep) {
        int motoHeight = bitmap.getHeight();
        int motoWidth = bitmap.getWidth();
        double rateX = (double) motoWidth / newWidth;
        double rateY = (double) motoHeight / newHeight;

        if (!isKeep)
            return compressImage(bitmap, rateX, rateY);
        else {
            double rateMin = Math.min(rateX, rateY);
            return compressImage(bitmap, rateMin, rateMin);
        }
    }

    public interface IImageBack {

        /**
         * @param bitmap nullable
         */
        @UiThread
        void onGetImg(@Nullable Bitmap bitmap);
    }

    /**
     * ImgPopupDialog OCRActivity 用
     * 异步获取图片
     * @param activity runOnUiThread
     * @param url 网络连接
     * @param imageBack 异步回调
     */
    @WorkerThread
    public static void getImgAsync(Activity activity, String url, IImageBack imageBack) {
        URL fileUrl;
        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            activity.runOnUiThread(() -> imageBack.onGetImg(null));
            return;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();

            InputStream is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            activity.runOnUiThread(() -> imageBack.onGetImg(bitmap));
            is.close();
        } catch (IOException | NullPointerException e) {
            activity.runOnUiThread(() -> imageBack.onGetImg(null));
            e.printStackTrace();
        }
    }
}
