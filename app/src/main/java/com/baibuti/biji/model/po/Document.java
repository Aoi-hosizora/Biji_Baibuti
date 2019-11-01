package com.baibuti.biji.model.po;

import java.io.Serializable;

import lombok.Data;

@Data
public class Document implements Serializable {

    private int id;
    private String filePath;
    private String className;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document)) return false;
        return filePath.equals(((Document) obj).getFilePath());
    }

    // public Document() {
    //     this(0, "", "");
    // }
    //
    // public Document(String filePath, String className) {
    //     this(0, filePath, className);
    // }

    public Document(int id, String filePath, String className) {
        this.id = id;
        this.filePath = filePath;
        this.className = className;
    }
}
