package com.baibuti.biji.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Windows 10 on 016 2019/02/16.
 */

public class Alarm {

    private String Title;
    private Date MakeTime;
    private String Content;

    public Alarm(String Title, String Content, Date MakeTime) {
        this.Title = Title;
        this.MakeTime = MakeTime;
        this.Content = Content;
    }

    public Alarm(String Title, String Content) {
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
}
