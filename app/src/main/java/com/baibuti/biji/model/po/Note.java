package com.baibuti.biji.model.po;

import android.support.annotation.NonNull;

import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

public class Note implements Serializable, Comparable<Note>, ISearchEntity {

    @Getter
    private int id;

    @Getter
    private String title;
    @Getter
    private String content;

    @Getter @Setter
    private Group group;

    @Getter @Setter
    private Date createTime;
    @Getter @Setter
    private Date updateTime;

    public Note() {
        this("", "");
    }

    public Note(Note n) {
        this(n.getId(), n.getTitle(), n.getContent(), n.getGroup(), n.getCreateTime(), n.getUpdateTime());
    }

    public Note(String title, String content) {
        this(0, title, content, new Group(), new Date(), new Date());
    }

    public Note(int id, String title, String content, Group group) {
        this(id, title, content, group, new Date(), new Date());
    }

    public Note(int id, String title, String content, Group group, Date createTime, Date updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.group = group;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    public int compareTo(@NonNull Note o) {
        int ret = o.getUpdateTime().compareTo(this.getUpdateTime());
        if (ret == 0)
            return o.getTitle().compareTo(this.getTitle());
        return ret;
    }

    //////////////////////////////////////////////////

    public void setId(int id) {
        this.id = id;
        this.updateTime = new Date();
    }

    public void setTitle(String title) {
        this.title = title;
        this.updateTime = new Date();
    }

    public void setContent(String content) {
        this.content = content;
        this.updateTime = new Date();
    }

    public void setGroup(Group group, boolean isChangeUt) {
        this.group = group;
        if (isChangeUt)
            this.updateTime = new Date();
    }

    //////////////////////////////////////////////////


    public String getUpdateTime_FullString() {
        return DateColorUtil.Date2Str(this.updateTime);
    }

    public String getCreateTime_FullString() {
        return DateColorUtil.Date2Str(this.createTime);
    }

    private String getUpdateTime_TimeString() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.CHINA);
        return df.format(this.updateTime);
    }

    private String getUpdateTime_DateString() {
        SimpleDateFormat df = new SimpleDateFormat("MM-dd", Locale.CHINA);
        return df.format(this.updateTime);
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
        return this.title + this.content + this.group.getName();
    }
}
