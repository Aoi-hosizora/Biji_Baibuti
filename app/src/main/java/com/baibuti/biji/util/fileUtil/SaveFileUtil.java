package com.baibuti.biji.util.fileUtil;

import android.graphics.Bitmap;

import com.baibuti.biji.util.imgDocUtil.ImageUtil;

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
        PHOTO, EDITED, SMALL, OCR
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
                // ModifyNoteAct::insertImagesSync() -> 委托 this 保存
                // NoteFrag::OpenOCRAct() -> 委托 this 保存
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
            case OCR:
                // OCRAct::initBG() -> 委托 this 保存
                filename += ".jpg";
                break;
        }
        return filename;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 保存笔记的小图片
     * @param bitmap 未压缩的图片
     */
    public static String saveSmallImg(Bitmap bitmap) {
        String imageUrl = getImageFileName(SaveImageType.SMALL);
        ImageUtil.saveBitmap(ImageUtil.compressImage(bitmap), imageUrl);
        return imageUrl;
    }

    /**
     * 保存 OCR 临时图片
     * @param bitmap OCR临时图片，已处理大小
     */
    public static String saveOCRTmp(Bitmap bitmap) {
        String fileName = getImageFileName(SaveImageType.OCR);
        ImageUtil.saveBitmap(bitmap, fileName);
        return fileName;
    }
}
