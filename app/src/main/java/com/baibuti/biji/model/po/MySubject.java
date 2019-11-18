package com.baibuti.biji.model.po;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MySubject implements ScheduleEnable {

    @JSONField(name = "id")         private int id = 0;

    // 信息
    @JSONField(name = "docName")    private String name;            // 课程名
    @JSONField(name = "time")       private String time;            // 周学时
    @JSONField(name = "room")       private String room;            // 教室
    @JSONField(name = "teacher")    private String teacher;         // 教师

    // 时间
    @JSONField(name = "start")      private int start;              // 开始上课 <<<
    @JSONField(name = "step")       private int step;               // 上课节数 <<<

    // 日期
    @JSONField(name = "day")        private int day;                // 周几上
    @JSONField(name = "weekList")   private List<Integer> weekList; // 上课周次 <<<

    //////////////////

    public static String toJsons(List<MySubject> list) {
        try {
            return JSON.toJSONString(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static List<MySubject> fromJson(String json) {
        try {
            return JSON.parseArray(json, MySubject.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    //////////////////

    public MySubject() { }

    private static final String EXTRAS_ID = "extras_id";

    /**
     * 统一接口
     */
    @Override
    @JSONField(serialize = false, deserialize = false)
    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setDay(this.day);
        schedule.setName(this.name);
        schedule.setRoom(this.room);
        schedule.setStart(this.start);
        schedule.setStep(this.step);
        schedule.setTeacher(this.teacher);
        schedule.setWeekList(this.weekList);
        schedule.setColorRandom(2);
        schedule.putExtras(EXTRAS_ID, getId());
        return schedule;
    }
}