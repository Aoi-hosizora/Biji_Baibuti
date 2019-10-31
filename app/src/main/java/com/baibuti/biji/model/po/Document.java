package com.baibuti.biji.model.po;

import java.io.Serializable;

import lombok.Data;

@Data
public class Document implements Serializable {

    private int id;
    private String path;
    private String className;
    private String docName;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Document)) return false;
        return path.equals(((Document) obj).getPath());
    }

    // public Document(String path, String className) {
    //     this(0, path, className, "");
    // }
    //
    // public Document(String path, String className, String docName) {
    //     this(0, path, className, docName);
    // }

    public Document(int id, String path, String className, String docName) {
        this.id = id;
        this.path = path;
        this.className = className;
        this.docName = docName;
    }
}
