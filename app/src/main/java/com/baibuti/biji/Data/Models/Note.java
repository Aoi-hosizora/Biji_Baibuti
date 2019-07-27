package com.baibuti.biji.Data.Models;

import android.support.annotation.NonNull;

import com.baibuti.biji.Interface.ISearchEntity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class Note implements Serializable, Comparable<Note>, ISearchEntity {

    private int Id;

    private String Title;
    private String Content;

    private Group GroupLabel;

    private Date CreateTime;
    private Date UpdateTime;

    public static String GetDefaultNoteName = "默认笔记";

    public Note(Note n) {
        this.Id = n.Id;
        this.Title = n.Title;
        this.Content = n.Content;
        this.GroupLabel = n.GroupLabel;
        this.CreateTime = n.CreateTime;
        this.UpdateTime = n.UpdateTime;
    }

    public Note(String Title, String Content) {
        this.Title = Title;
        this.Content = Content;
        this.UpdateTime = new Date();
        this.CreateTime = new Date(); // //////////
        this.GroupLabel = new Group();
    }

    public Note(int id, String title, String content, Group groupLabel, Date createTime, Date updateTime) {
        this(title, content);
        this.setId(id);
        this.setGroupLabel(groupLabel, false);
        this.setCreateTime(createTime);
        this.setUpdateTime(updateTime);
    }

    @Override
    public int compareTo(@NonNull Note o) {
        return o.getUpdateTime().compareTo(this.getUpdateTime());
    }
    //////////////////////////////////////////////////

    public void setId(int id) {
        this.Id = id;
        this.UpdateTime = new Date();
    }

    public void setTitle(String Title) {
        this.Title = Title;
        this.UpdateTime = new Date();
    }

    public void setContent(String Content) {
        this.Content = Content;
        this.UpdateTime = new Date();
    }

    public void setGroupLabel(Group GroupLabel, boolean ischangeTime) {
        this.GroupLabel = GroupLabel;
        if (ischangeTime)
            this.UpdateTime = new Date();
    }

    public void setCreateTime(Date CreateTime) {
        this.CreateTime = CreateTime;
    }

    public void setUpdateTime(Date UpdateTime) {
        this.UpdateTime = UpdateTime;
    }

    //////////////////////////////////////////////////

    public int getId() {
        return this.Id;
    }

    public String getTitle() {
        return this.Title;
    }

    public String getContent() {
        return this.Content;
    }

    public Group getGroupLabel() {
        return this.GroupLabel;
    }

    public Date getCreateTime() {
        return this.CreateTime;
    }

    public Date getUpdateTime() {
        return this.UpdateTime;
    }

    //////////////////////////////////////////////////


    public String getUpdateTime_FullString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        return df.format(this.UpdateTime);
    }

    public String getCreateTime_FullString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        return df.format(this.CreateTime);
    }

    public String getUpdateTime_TimeString() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.CHINA);

        return df.format(this.UpdateTime);
    }

    public String getUpdateTime_DateString() {
        SimpleDateFormat df = new SimpleDateFormat("MM-dd", Locale.CHINA);

        return df.format(this.UpdateTime);
    }

    public String getUpdateTime_ShortString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        if (fmt.format(new Date()).equals(fmt.format(getUpdateTime())))
            return getUpdateTime_TimeString();
        else
            return getUpdateTime_DateString() + " " + getUpdateTime_TimeString();
    }

    @Override
    public String getSearchContent() {
        return this.Title + this.Content + this.GroupLabel.getName();
    }
}
