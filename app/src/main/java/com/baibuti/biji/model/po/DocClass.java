package com.baibuti.biji.model.po;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class DocClass implements Serializable {

    @Getter @Setter
    private int id;
    @Getter @Setter
    private String name;

    /**
     * 默认分类
     */
    public static final String DEF_CLASS_NAME = "默认分组";

    public DocClass() {
        this(0, DEF_CLASS_NAME);
    }

    public DocClass(String name) {
        this(0, name);
    }

    public DocClass(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DocClass)) return false;

        DocClass that = (DocClass) obj;
        return id == that.getId() &&
            name.equals(that.getName());
    }
}
