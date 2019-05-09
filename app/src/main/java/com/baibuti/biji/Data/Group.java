package com.baibuti.biji.Data;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable, Comparable<Group> {

    private int id;//ID
    private String name;//分组名称

    private int order;//排列顺序

    private String color;//分类颜色，存储颜色代码

    public static String GetDefaultGroupName = "默认分组";

    @Override
    public boolean equals(Object obj) {
        Group that = (Group) obj;

        if (this.id == that.id &&
            this.name.equals(that.name) &&
            this.order == that.order &&
            this.color.equals(that.color))
            return true;

        return false;
    }

    public Group() {
        id = order = 0;
        name = "";
        color = "#F0F0F0";
    }

    public Group(int id, String name, int order, String color) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    public Group(String name, int order, String color) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    @Override
    public int compareTo(@NonNull Group o) {
        if (o.order < this.order)
            return 1;
        else if (o.order == this.order)
            return 0;
        else
            return -1;
    }

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
