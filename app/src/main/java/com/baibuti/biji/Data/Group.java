package com.baibuti.biji.Data;

import java.io.Serializable;

public class Group implements Serializable {

    private int id;//ID
    private String name;//分组名称

    private int order;//排列顺序

    private String color;//分类颜色，存储颜色代码

    private String createTime;//创建时间
    private String updateTime;//修改时间

    public Group() {
        id = 0;
        name = "默认笔记";
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
