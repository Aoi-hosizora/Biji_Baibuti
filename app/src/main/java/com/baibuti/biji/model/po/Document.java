package com.baibuti.biji.model.po;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baibuti.biji.model.vo.ISearchEntity;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class Document implements Serializable, Comparable<Document>, ISearchEntity {

    private int id;
    private String filename; // 本地 / 下载: 完整路径, 服务器: 文件名
    private DocClass docClass;
    private String uuid; // 服务器的标识
    private Date downloadTime; // 本地下载的时间

    /**
     * 本地存储用构造函数
     */
    public Document(int id, String filePath, DocClass docClass) {
        this(id, filePath, docClass, "", new Date());
    }

    /**
     * DTO 用 构造函数
     */
    public Document(int id, String filePath, DocClass docClass, String uuid) {
        this(id, filePath, docClass, uuid, new Date());
    }

    /**
     * 下载项构造函数
     */
    public Document(String filePath, Date date) {
        this(-1, filePath, null, "", date);
    }

    /**
     * 默认构造函数
     */
    public Document(int id, String filename, DocClass docClass, String uuid, Date downloadTime) {
        this.id = id;
        this.filename = filename;
        this.docClass = docClass;
        this.uuid = uuid;
        this.downloadTime = downloadTime;
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
        return filename.equals(that.getFilename()) &&
            docClass.getId() == that.getDocClass().getId();
    }

    @Override
    public int hashCode() {
        return (filename + docClass.getId()).hashCode();
    }

    @Override
    public String getSearchContent() {
        return filename + " " + docClass.getName();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(@NonNull Document o) {
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
    public static Document fromDownloadContent(String downloadContent) {
        try {
            JSONObject jsonObject = new JSONObject(downloadContent);
            return new Document(jsonObject.getString("filename"), DateColorUtil.Str2Date(jsonObject.getString("download_time")));
        } catch (JSONException ex) {
            return null;
        }
    }
}
