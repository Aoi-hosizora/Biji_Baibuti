package com.baibuti.biji.model.po;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class Group implements Serializable, Comparable<Group> {

    private int id;
    private String name;
    private int order;
    private String color;

    private static String DEF_GROUP_NAME = "默认分组";
    private static String DEF_GROUP_COLOR = "#F0F0F0";

    /**
     * 默认分组
     */
    public static Group DEF_GROUP = new Group(0, DEF_GROUP_NAME, 0, DEF_GROUP_COLOR);

    public static final Group AllGroups = new Group("所有分组", -1, "#00000000");

    public Group() {
        this(0, "", 0, DEF_GROUP_NAME);
    }

    public Group(int id) {
        this(id, "", 0, DEF_GROUP_COLOR);
    }

    public Group(String name, int order, String color) {
        this(0, name, order, color);
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
        Group that = (Group) obj;

        return this.id == that.id &&
            this.name.equals(that.name) &&
            this.order == that.order &&
            this.color.equals(that.color);
    }

    /////////////////////////////////////////////////

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStringColor() {
        return getColor();
    }

    public int getIntColor() {
        return Color.parseColor(getColor());
    }

}
