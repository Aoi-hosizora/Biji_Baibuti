package com.baibuti.biji.model.po;

import com.baibuti.biji.model.vo.ISearchEntity;

import java.io.Serializable;

import lombok.Data;

@Data
public class Document implements Serializable, ISearchEntity {

    private int id;
    private String filename; // 本地: 完整路径, 服务器: 文件名
    private DocClass docClass;
    private String uuid; // 服务器的标识

    /**
     * 本地存储用构造函数
     */
    public Document(int id, String filePath, DocClass docClass) {
        this(id, filePath, docClass, "");
    }

    /**
     * DTO 用 构造函数
     */
    public Document(int id, String filename, DocClass docClass, String uuid) {
        this.id = id;
        this.filename = filename;
        this.docClass = docClass;
        this.uuid = uuid;
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

    /**
     * 是否为本地文件
     */
    public boolean isLocalFile() {
        return getFilename().startsWith("/storage/");
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

}
