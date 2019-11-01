package com.baibuti.biji.util.fileUtil;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.baibuti.biji.R;

import java.io.File;

/**
 * 统一设置 应用路径
 */
public class AppPathUtil {

    public static final String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static final String APP_ROOT = SDCardRoot + "Biji" + File.separator;
    private static final String SUB_PATH_NOTE_IMAGE = "NoteImage" + File.separator;
    private static final String SUB_PATH_NOTE_FILE = "NoteFile" + File.separator;
    private static final String SUB_PATH_OCR_TMP = "OCRTmp" + File.separator;
    private static final String SUB_PATH_SCHEDULE = "Schedule" + File.separator;

    // /**
    //  * 检查是否存在SDCard
    //  */
    // public static boolean hasSdcard() {
    //     String state = Environment.getExternalStorageState();
    //     return state.equals(Environment.MEDIA_MOUNTED);
    // }

    /**
     * 应用基本路径, /Biji/
     */
    private static String getAppRootDir() {
        return mkdirAndGetDir(APP_ROOT);
    }

    /**
     * 笔记图片保存路径, /Biji/NoteImage/
     */
    public static String getPictureDir() {
        return mkdirAndGetDir(APP_ROOT + SUB_PATH_NOTE_IMAGE);
    }

    /**
     * 笔记图片保存路径, /Biji/NoteImage/
     */
    public static String getFileNoteDir() {
        return mkdirAndGetDir(APP_ROOT + SUB_PATH_NOTE_FILE);
    }

    /**
     * OCR 临时保存路径, /Biji/OCRTmp/
     */
    public static String getOCRTmpDir() {
        return mkdirAndGetDir(APP_ROOT + SUB_PATH_OCR_TMP);
    }

    /**
     * 课表文件保存路径, /Biji/Schedule/
     */
    public static String getScheduleDir() {
        return mkdirAndGetDir(APP_ROOT + SUB_PATH_SCHEDULE);
    }

    /**
     * 新建并返回路径
     * @return "" for error
     */
    private static String mkdirAndGetDir(String path) {
        File file = new File(path);
        if (!file.exists() && file.mkdirs())
            return path;
        return "";
    }

    /**
     * 删除文件
     * @return isOk
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        boolean isOk = false;
        if (file.isFile() && file.exists())
            isOk = file.delete();
        return isOk;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    // https://blog.csdn.net/weixin_37577039/article/details/79242455
    // Path -> Uri; File -> Uri; Uri -> Path (!!!)

    /**
     * Path -> Uri
     * 委托 getUriByFile()
     */
    public static Uri getUriByPath(Context context, String path) {
        return getUriByFile(context, new File(path));
    }

    /** !!!!
     * File -> Uri，FileProvider.getUriByFile() / Uri.fromFile()
     * >= 7.0 content://com.xxx.xxx.FileProvider/images/photo_20180824173621.jpg
     * <  6.0 file:///storage/emulated/0/take_photo/photo_20180824171132.jpg
     */
    private static Uri getUriByFile(Context context, File file) {
        if (context == null || file == null)
            throw new NullPointerException();

        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            String provider = context.getString(R.string.FileProvider);
            uri = FileProvider.getUriForFile(context.getApplicationContext(), provider, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 判断是否是当前项目的 FileProvider
     * @param uri -> content://com.baibuti.biji.FileProvider/
     */
    private static boolean isThisAppAuthority(Context context, Uri uri) {
        return context.getString(R.string.FileProvider).equals(uri.getAuthority());
    }

    /** !!!!
     * Uri -> Path, uri.getFilePath() / handle Path
     * Uri.getFilePath() 的返回情况
     *      file:///
     *      content://package
     */
    public static String getFilePathByUri(Context context, Uri uri) {

        // 1. file://
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
            return uri.getPath();

        ///////////////////////////////////////////////
        // 2. content://

        // content://com.baibuti.biji.FileProvider/images/NoteImage/20190518133507370_Photo.jpg
        if (isThisAppAuthority(context, uri)) {
            String uriPath = uri.getPath();
            if (uriPath == null) return null;

            final String[] sp = uriPath.split(File.separator);
            // prefix + / + subtype + /
            String filename = uriPath.substring(uriPath.indexOf(sp[3]) + sp[3].length() + 2);
            return getAppRootDir() + filename;
        }

        // content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        }

        return null;
    }

    /**
     * getDataColumn (getFilePathByUri 用)
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
