package com.baibuti.biji.Data;

import android.media.Image;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class Note implements Serializable, Comparator<Note> {

    private int Id;

    private String Title;
    private String Content;

    private Group GroupLabel;

    private Date CreateTime;
    private Date UpdateTime;

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
        this.GroupLabel = new Group();
    }


    @Override
    public int compare(Note o1, Note o2) {
        return o1.getUpdateTime().compareTo(o2.getUpdateTime());
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

    public void setGroupLabel(Group GroupLabel) {
        this.GroupLabel = GroupLabel;
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(this.UpdateTime);
    }

    public String getCreateTime_FullString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(this.CreateTime);
    }

    public String getUpdateTime_TimeString() {
        SimpleDateFormat df;
        if (new Date().getDate() == this.UpdateTime.getDate())
            df = new SimpleDateFormat("HH:mm");
        else
            df = new SimpleDateFormat("MM-dd");

        return df.format(this.UpdateTime);
    }

    public String getUpdateTime_DateString() {
        SimpleDateFormat df = new SimpleDateFormat("MM-dd");

        return df.format(this.UpdateTime);
    }

    public String getUpdateTime_ShortString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if (fmt.format(new Date()).equals(fmt.format(getUpdateTime())))
            return getUpdateTime_TimeString();
        else
            return getUpdateTime_DateString();
    }

}
