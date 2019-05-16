package me.kareluo.imaging;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import me.kareluo.imaging.core.IMGMode;
import me.kareluo.imaging.core.IMGText;
import me.kareluo.imaging.core.file.IMGAssetFileDecoder;
import me.kareluo.imaging.core.file.IMGDecoder;
import me.kareluo.imaging.core.file.IMGFileDecoder;
import me.kareluo.imaging.core.util.IMGUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by felix on 2017/11/14 下午2:26.
 *
 * onDoneClick(): Modified By Aoihosizora
 * saveToSdCard(): Add By AoiHosizora
 */

public class IMGEditActivity extends IMGEditBaseActivity {

    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";
    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";

    /**
     * Add By AoiHosizora
     * 修改后的图片保存位置
     */
    private static String Edited_Image_Save_Path;

    @Override
    public void onCreated() {

    }

    @Override
    public Bitmap getBitmap() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Uri uri = intent.getParcelableExtra(EXTRA_IMAGE_URI);
        Edited_Image_Save_Path = intent.getStringExtra(EXTRA_IMAGE_SAVE_PATH);

        if (uri == null) {
            return null;
        }

        IMGDecoder decoder = null;

        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            switch (uri.getScheme()) {
                case "asset":
                    decoder = new IMGAssetFileDecoder(this, uri);
                    break;
                case "file":
                    decoder = new IMGFileDecoder(uri);
                    break;
            }
        }

        if (decoder == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        decoder.decode(options);

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = IMGUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH));
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    IMGUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)));
        }

        options.inJustDecodeBounds = false;

        Bitmap bitmap = decoder.decode(options);
        if (bitmap == null) {
            return null;
        }

        return bitmap;
    }

    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

        if (mode == IMGMode.CLIP) {
            setOpDisplay(OP_CLIP);
        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        finish();
    }
//
//    public static Uri getImageStreamFromExternal(String imageName) {
//        File picPath = new File(SDCardUtil.getPictureDir(), imageName);
//        Uri uri = null;
//        if(picPath.exists()) {
//            uri = Uri.fromFile(picPath);
//        }
//
//        return uri;
//    }


    /**
     * Add By AoiHosizora
     * 保存编辑后的图片
     * @param bitmap
     * @return
     */
    public static String saveToSdCard(Bitmap bitmap) {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date());
        String imageUrl = Edited_Image_Save_Path + time + "_Edited.jpg"; //////////
        File file = new File(imageUrl);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }


    @Override
    public void onDoneClick() {
//        String path = getIntent().getStringExtra(EXTRA_IMAGE_SAVE_PATH);
//
//        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = mImgView.saveBitmap();
            if (bitmap != null) {

                String str = saveToSdCard(bitmap);

//
//                FileOutputStream fout = null;
//                try {
//                    fout = new FileOutputStream(path);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } finally {
//                    if (fout != null) {
//                        try {
//                            fout.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
                Intent intent = new Intent();
                //////////////////////////////////////////////////

//                Log.i("//////////////////////", "onDoneClick: "+ str+" '''''"+ Uri.parse(str));
                intent.setData(Uri.parse(str));

                //////////////////////////////////////////////////

                setResult(RESULT_OK, intent);
                finish();
                return;
//            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }
}
