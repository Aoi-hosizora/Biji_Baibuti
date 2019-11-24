package com.baibuti.biji.model.po;

import java.io.Serializable;
import java.util.HashMap;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DocClass implements Serializable {

    private int id;
    private String name;

    /**
     * 默认分类
     */
    private static final String DEF_CLASS_NAME = "默认分组";

    public static final DocClass DEF_DOCCLASS = new DocClass(0, DEF_CLASS_NAME);

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

    @Override
    public int hashCode() {
        return (id + name).hashCode();
    }
}
