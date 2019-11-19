package com.baibuti.biji.model.po;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Group implements Serializable, Comparable<Group> {

    @Getter @Setter
    private int id;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private int order;
    @Getter @Setter
    private String color;

    private static String DEF_GROUP_NAME = "默认分组";
    private static String DEF_GROUP_COLOR = "#A5A5A5";

    /**
     * 默认分组
     */
    public static Group DEF_GROUP = new Group(1, DEF_GROUP_NAME, 0, DEF_GROUP_COLOR);

    public static final Group AllGroups = new Group("所有分组", -1, "#00000000");

    public Group() {
        this(0, DEF_GROUP_NAME, 0, DEF_GROUP_COLOR);
    }

    public Group(int id) {
        this(id, DEF_GROUP_NAME, 0, DEF_GROUP_COLOR);
    }

    public Group(String name, int order, String color) {
        this(0, name, order, color);
    }

    public Group(String name, String color) {
        this(0, name, 0, color);
    }

    public Group(int id, String name, int order, String color) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    @Override
    public int compareTo(@NonNull Group o) {
        return Integer.compare(o.order, this.order);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) return false;

        Group that = (Group) obj;

        return id == that.id &&
            name.equals(that.name) &&
            order == that.order &&
            color.equals(that.color);
    }

    public String getStringColor() {
        return getColor();
    }

    public int getIntColor() {
        return Color.parseColor(getColor());
    }

}
