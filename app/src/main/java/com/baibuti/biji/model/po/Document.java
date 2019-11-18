package com.baibuti.biji.model.po;

import java.io.Serializable;

import lombok.Data;

@Data
public class Document implements Serializable {

    private int id;
    private String filename;
    private DocClass docClass;

    public Document(int id, String filePath, DocClass docClass) {
        this.id = id;
        this.filename = filePath;
        this.docClass = docClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document)) return false;

        Document that = (Document) obj;
        return filename.equals(that.getFilename()) &&
            docClass.getId() == that.getDocClass().getId();
    }
}
