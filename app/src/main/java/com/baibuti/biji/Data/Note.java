package com.baibuti.biji.Data;

import android.media.Image;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class Note implements Serializable {

    private String Title;
    private Date MakeTime;
    private String Content;

    public Note(String Title, String Content, Date MakeTime) {
        this.Title = Title;
        this.MakeTime = MakeTime;
        this.Content = Content;
    }

    public Note(String Title, String Content) {
        this(Title, Content, new Date());
    }

    public String getTitle() {
        return Title;
    }

    public String getContent() {
        return Content;
    }

    public Date getMakeTime() {
        return MakeTime;
    }

    public String getMakeTimeShortString() {
        SimpleDateFormat df;
        if (new Date().getDate() == MakeTime.getDate())
            df = new SimpleDateFormat("HH:mm");
        else
            df = new SimpleDateFormat("MM-dd");

        return df.format(MakeTime);
    }

    public String getMakeTimeString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(MakeTime);
    }

    public void setTitle(String Title) {
        this.Title = Title;
        this.MakeTime = new Date();
    }

    public void setContent(String Content) {
        this.Content = Content;
        this.MakeTime = new Date();
    }

}
