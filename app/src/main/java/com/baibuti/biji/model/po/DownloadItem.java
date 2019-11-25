package com.baibuti.biji.model.po;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baibuti.biji.util.otherUtil.DateColorUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DownloadItem implements Serializable, Comparable<DownloadItem> {

    private String filename;
    private Date downloadTime;

    /**
     * 下载项构造函数
     */
    public DownloadItem(String filePath, Date date) {
        this.downloadTime = date;
        this.filename = filePath;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 文件路径中的文件名
     */
    public String getBaseFilename() {
        String[] sp = filename.split("[/\\\\]");
        if (sp.length == 0) return "";
        return sp[sp.length - 1];
    }

    /**
     * 获取文件后缀名
     */
    public String getFileExtension() {
        String basename = getBaseFilename();
        String[] sp = basename.split("\\.");
        if (sp.length <= 1) return "";
        else
            return sp[sp.length - 1];
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document)) return false;

        Document that = (Document) obj;
        return filename.equals(that.getFilename());
    }

    @Override
    public int hashCode() {
        return (filename).hashCode();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(@NonNull DownloadItem o) {
        int ret = o.getDownloadTime().compareTo(this.getDownloadTime());
        if (ret == 0)
            return o.getFilename().compareTo(this.getFilename());
        return ret;
    }

    /**
     * 下载项专用，内容存储
     */
    public String toDownloadContent() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("filename", this.filename);
            jsonObject.put("download_time", DateColorUtil.Date2Str(this.downloadTime));
            return jsonObject.toString();
        } catch (JSONException ex) {
            return "";
        }
    }

    /**
     * 下载项专用，解析内容
     */
    @Nullable
    public static DownloadItem fromDownloadContent(String downloadContent) {
        try {
            JSONObject jsonObject = new JSONObject(downloadContent);
            return new DownloadItem(jsonObject.getString("filename"), DateColorUtil.Str2Date(jsonObject.getString("download_time")));
        } catch (JSONException ex) {
            return null;
        }
    }
}
