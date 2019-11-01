package com.baibuti.biji.util.imgDocUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    /**
     * 通过文件路径来获取Bitmap
     */
    public static Bitmap getBitmapFromFile(String pathName) {
        return BitmapFactory.decodeFile(pathName);
    }

    /**
     * 存储 bitmap
     */
    public static void saveBitmap(Bitmap bitmap, String pathname) {
        File bf = new File(pathname);
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
     * 计算图片的缩放值
     * @param options BitmapFactory.Options
     * @param reqWidth 目标 Width
     * @param reqHeight 目标 Height
     * @return 当前 / 目标
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 根据路径压缩返回 bitmap
     */
    public static Bitmap getSmallBitmap(String filePath, int newWidth, int newHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap newBitmap = compressImage(bitmap, 500);

        if (!bitmap.isRecycled())
            bitmap.recycle();
        return newBitmap;
    }

    /**
     * 质量压缩
     * @param maxSize 最大大小
     */
    private static Bitmap compressImage(Bitmap image, int maxSize){
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // scale
        int options = 80;

        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);

        // Compress by loop: Clean up os
        while (os.toByteArray().length / 1024 > maxSize) {
            os.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        Bitmap bitmap = null;
        byte[] b = os.toByteArray();
        if (b.length != 0)
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

        return bitmap;
    }
}
