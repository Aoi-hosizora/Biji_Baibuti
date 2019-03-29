package com.baibuti.biji.Data;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {

    private int id;//ID
    private String name;//分组名称

    private int order;//排列顺序

    private String color;//分类颜色，存储颜色代码

    public Group() {
        id = order = 0;
        name = "默认笔记";
        color = "#FFFFFF";
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
}
