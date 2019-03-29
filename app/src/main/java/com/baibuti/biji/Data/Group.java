package com.baibuti.biji.Data;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {

    private int id;//ID
    private String name;//分组名称

    private int order;//排列顺序

    private String color;//分类颜色，存储颜色代码

    private Date createTime;//创建时间
    private Date updateTime;//修改时间

    public Group() {
        id = order = 0;
        name = "默认笔记";
        color = "#FFFFFF";
        updateTime = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.updateTime = new Date();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updateTime = new Date();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
        this.updateTime = new Date();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.updateTime = new Date();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
