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
     * 应用中涉及到的几种 图片处理格式
     */
    public enum SaveImageType {
        PHOTO, EDITED, SMALL
    }

    /**
     * 统一返回存储图片的路径
     * @return "_TYPE.jpg"
     */
    public static String getImageFileName(SaveImageType type) {
        String dir = FilePathUtil.getPictureDir();

        String filename = dir + getTimeToken();
        switch (type) {
            case SMALL:
                // ModifyNoteAct::insertImagesSync() -> 委托保存
                // NoteFrag::OpenOCRAct() -> 委托保存
                filename += "_SMALL.jpg";
                break;
            case PHOTO:
                // ModifyNoteAct::takePhone() -> 系统 自行保存
                // NoteFrag::takePhone() -> 系统 自行保存
                filename += "_PHOTO.jpg";
                break;
            case EDITED:
                // ModifyNoteAct::StartEditImg() -> IMGEditActivity 自行保存
                // NoteFrag::StartEditImg() -> IMGEditActivity 自行保存
                filename += "_EDITED.jpg";
                break;
        }
        return filename;
    }

    /**
     * 保存图片
     */
    public static String saveSmallImgToSdCard(Bitmap bitmap) {
        String imageUrl = getImageFileName(SaveImageType.SMALL);
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
