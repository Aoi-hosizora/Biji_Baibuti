package com.baibuti.biji.Data;

import android.media.Image;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class Note implements Serializable {

    private int Id;

    private String Title;
    private String Content;

    private Group GroupLabel;

    private Date CreateTime;
    private Date UpdateTime;


    public Note(String Title, String Content) {
        this.Title = Title;
        this.Content = Content;
        this.UpdateTime = new Date();
    }

    //////////////////////////////////////////////////

    public void setId(int id) {
        this.Id = id;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public void setGroupLabel(Group GroupLabel) {
        this.GroupLabel = GroupLabel;
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

    public String getUpdateTimeShortString() {
        SimpleDateFormat df;
        if (new Date().getDate() == UpdateTime.getDate())
            df = new SimpleDateFormat("HH:mm");
        else
            df = new SimpleDateFormat("MM-dd");

        return df.format(UpdateTime);
    }

    public String getUpdateTimeString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(UpdateTime);
    }


}
