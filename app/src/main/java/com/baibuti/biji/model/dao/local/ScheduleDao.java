package com.baibuti.biji.model.dao.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.baibuti.biji.util.filePathUtil.FileNameUtil;
import com.baibuti.biji.util.imgTextUtil.StringUtil;

import java.io.File;
import java.io.IOException;

public class ScheduleDao {

    private static final String SP_SCHEDULE = "schedule";
    private static final String SP_KEY_WEEK = "curr_week";

    private Context context;

    public ScheduleDao(Context context) {
        this.context = context;
    }

    /**
     * 查询本地课表与当前周
     * @return Json, empty for error
     */
    public Pair<String, Integer> querySchedule() {
        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        String content = StringUtil.readFromFile(filename);

        SharedPreferences weekSP = context.getSharedPreferences(SP_SCHEDULE, Context.MODE_PRIVATE);
        int currWeek = weekSP.getInt(SP_KEY_WEEK, 1);

        if (content == null)
            content = "";

        return new Pair<>(content, currWeek);
    }

    /**
     * 新建 / 更新 本地课表文件 并恢复 SP
     * @param json Json 格式课表
     * @param currWeek 同时更新当前周
     * @return 是否新建 / 更新成功
     */
    public boolean updateSchedule(String json, int currWeek) {
        if (currWeek <= 0) return false;

        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        boolean ok = StringUtil.writeIntoFile(filename, json);

        SharedPreferences weekSP = context.getSharedPreferences(SP_SCHEDULE, Context.MODE_PRIVATE);
        weekSP.edit().putInt(SP_KEY_WEEK, currWeek).apply();
        boolean ok2 = weekSP.getInt(SP_KEY_WEEK, -1) == currWeek;

        return ok && ok2;
    }

    /**
     * 删除本地课表文件
     * @return 是否成功删除
     */
    public boolean deleteSchedule() {
        String filename = FileNameUtil.getScheduleFileName(FileNameUtil.LOCAL);
        try {
            File file = new File(filename);
            if (file.exists() && !file.delete())
                throw new IOException();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
