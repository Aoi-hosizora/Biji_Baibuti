package com.baibuti.biji.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.lang.Runnable;

import com.googlecode.tesseract.android.TessBaseAPI;

import static com.baibuti.biji.util.SDUtils.assets2SD;

import java.io.File;

public class ExtractUtil {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录:外部储存目录+分隔符。
     */
    public static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录在DATAPATH中创建tessdata目录。
     */
    public static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    public static String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名：DEFAULT_LANNGUAGE+后缀名。
     */
    public static String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    public static String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;


    public String TAG = "MainActivity";



    /**
     * 识别图像
     *
     * @param bitmap
     */
    public static String recognition(final Bitmap bitmap, final Context context) {
        String text = "";
        if (!checkTraineddataExists()) {
            //text += LANGUAGE_PATH + "不存在，开始复制\r\n";
            //Log.i(TAG, "run: " + LANGUAGE_PATH + "不存在，开始复制\r\n");
            assets2SD(context, LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        }
        //text += LANGUAGE_PATH + "已经存在，开始识别\r\n";
        //Log.i(TAG, "run: " + LANGUAGE_PATH + "已经存在，开始识别\r\n");
        long startTime = System.currentTimeMillis();
       // Log.i(TAG, "run: kaishi " + startTime);
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(bitmap);
        text = tessBaseAPI.getUTF8Text();
        long finishTime = System.currentTimeMillis();
        //Log.i(TAG, "run: jieshu " + finishTime);
        //Log.i(TAG, "run: text " + text);
        final String finalText = text;
        final Bitmap finalBitmap = bitmap;
        tessBaseAPI.end();
        return finalText;
    }


    public static boolean checkTraineddataExists() {
        File file = new File(LANGUAGE_PATH);
        return file.exists();
    }
}
