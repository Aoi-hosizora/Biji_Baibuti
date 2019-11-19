package com.baibuti.biji.model.po;

import com.baibuti.biji.model.vo.ISearchEntity;

import java.io.Serializable;

import lombok.Data;

@Data
public class Document implements Serializable, ISearchEntity {

    private int id;
    private String filename;
    private DocClass docClass;

    public Document(int id, String filePath, DocClass docClass) {
        this.id = id;
        this.filename = filePath;
        this.docClass = docClass;
    }

    /**
     * 文件路径中的文件名
     */
    public String getBaseFilename() {
        String[] sp = filename.split("[/\\\\]");
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
    public String getSearchContent() {
        return filename + " " + docClass.getName();
    }
}
