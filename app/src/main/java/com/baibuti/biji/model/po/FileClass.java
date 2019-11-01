package com.baibuti.biji.model.po;

import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class FileClass implements Serializable, Comparable<FileClass> {

    @Getter @Setter
    private int id;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private int order;

    // /**
    //  * 默认分类
    //  */
    // public static final String DEF_CLASS_NAME = "PDF";
    //
    // public FileClass() {
    //     this(0, DEF_CLASS_NAME, 0);
    // }

    public FileClass(int id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileClass)) return false;

        FileClass that = (FileClass) obj;
        return id == that.getId() &&
            name.equals(that.getName()) &&
            order == that.getOrder() ;
    }

    @Override
    public int compareTo(@NonNull FileClass o) {
        return Integer.compare(o.order, this.order);
    }
}
