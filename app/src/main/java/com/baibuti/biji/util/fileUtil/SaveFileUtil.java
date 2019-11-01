package com.baibuti.biji.util.fileUtil;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveFileUtil {

    /**
     * 获得当前时间序列
     */
    private static String getTimeToken() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
        return sdf.format(new Date());
    }

    /**
     * 保存图片
     */
    public static String saveSmallImgToSdCard(Bitmap bitmap) {
        String imageUrl = FilePathUtil.getPictureDir() + getTimeToken() + "_Small.jpg";
        File file = new File(imageUrl);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}
