package com.baibuti.biji.util.filePathUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 统一设置 保存文件名
 */
public class SaveNameUtil {

    /**
     * 获得当前时间序列
     */
    private static String getTimeToken() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA);
        return sdf.format(new Date());
    }

    /**
     * 应用中涉及到的几种 文件保存处理格式
     */
    public enum SaveType {
        PHOTO, EDITED, SMALL, OCR
    }

    /**
     * 统一返回存储图片的路径
     * @return "_TYPE.jpg"
     */
    public static String getImageFileName(SaveType type) {
        String dir = AppPathUtil.getPictureDir();

        String filename = dir + getTimeToken();
        switch (type) {
            case SMALL:
                // ModifyNoteAct::insertImagesSync() -> compress + saveBitmap 保存
                filename += "_SMALL.jpg";
                break;
            case PHOTO:
                // ModifyNoteAct::takePhone() -> 系统 自行保存
                // NoteFrag::takePhone() -> 系统 自行保存
                filename += "_PHOTO.jpg";
                break;
            case EDITED:
                // ModifyNoteAct::StartEditImg() -> IMGEditActivity 保存
                // NoteFrag::StartEditImg() -> IMGEditActivity 保存
                filename += "_EDITED.jpg";
                break;
            case OCR:
                // OCRAct::initBG() -> compress + saveBitmap 保存
                filename += "_OCR.jpg";
                break;
        }
        return filename;
    }

    /**
     * 默认本地用户保存课程表名
     */
    public static final String LOCAL = "local";

    /**
     * 统一返回存储课程表路径
     * @param userName 用户名，empty for local
     */
    public static String getScheduleFileName(String userName) {
        if (userName == null)
            throw new NullPointerException();

        if (userName.isEmpty()) userName = LOCAL;

        String dir = AppPathUtil.getScheduleDir();
        return dir + userName + ".json";
    }
}
