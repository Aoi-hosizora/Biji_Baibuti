package com.baibuti.biji.util.imgDocUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        return compressImage(bitmap, 90);
    }

    /**
     * 根据质量压缩图片
     */
    private static Bitmap compressImage(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] bytes = stream.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 根据等比率压缩图片
     */
    public static Bitmap compressImage(Bitmap bitmap, double rate) {
        return compressImage(bitmap, rate, rate);
    }

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
     * @param isEqualMatrix 是否等比例，如果不等比例则按照更小的尺寸压缩
     */
    public static Bitmap compressImage(Bitmap bitmap, int newWidth, int newHeight, boolean isEqualMatrix) {
        int motoHeight = bitmap.getHeight();
        int motoWidth = bitmap.getWidth();
        double rateX = (double) motoWidth / newWidth;
        double rateY = (double) motoHeight / newHeight;

        if (!isEqualMatrix)
            return compressImage(bitmap, rateX, rateY);
        else {
            double rateMin = Math.min(rateX, rateY);
            return compressImage(bitmap, rateMin, rateMin);
        }
    }
}
