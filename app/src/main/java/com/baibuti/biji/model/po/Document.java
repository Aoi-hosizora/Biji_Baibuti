package com.baibuti.biji.model.po;

import com.baibuti.biji.model.vo.ISearchEntity;

import java.io.Serializable;
import java.util.HashMap;

import lombok.Data;

@Data
public class Document implements Serializable, ISearchEntity {

    private int id;
    private String filename;
    private DocClass docClass;

    /**
     * 服务器端的标识
     */
    private String uuid;

    public Document(int id, String filePath, DocClass docClass) {
        this(id, filePath, docClass, "");
    }

    public Document(int id, String filePath, DocClass docClass, String uuid) {
        this.id = id;
        this.filename = filePath;
        this.docClass = docClass;
        this.uuid = uuid;
    }

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
