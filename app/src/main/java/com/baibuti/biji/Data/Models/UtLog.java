package com.baibuti.biji.Data.Models;

import java.io.Serializable;
import java.util.Date;

public class UtLog implements Serializable {

    public final static String Log_Note = "Note";
    public final static String Log_Group = "Group";
    public final static String Log_Star = "Star";
    public final static String Log_File = "File";
    public final static String Log_Schedule = "Schedule";

    /**
     * 所有模块集，只用在 UtLogDao
     */
    public final static String[] Log_Modules = {
        Log_Note, Log_Group, Log_Star, Log_File, Log_Schedule
    };

    private String module;
    private Date updateTime;

    public UtLog(String module, Date updateTime) {
        this.module = module;
        this.updateTime = updateTime;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
