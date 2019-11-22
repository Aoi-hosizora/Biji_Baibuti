package com.baibuti.biji.service.scut;

import android.util.Log;

import com.baibuti.biji.model.po.MySubject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleService {

    private static Map<String, Integer> text_day = new HashMap<String, Integer>() {{
        put("星期一", 1);
        put("星期二", 2);
        put("星期三", 3);
        put("星期四", 4);
        put("星期五", 5);
        put("星期六", 6);
        put("星期日", 7);
    }};

    /**
     * 解析 html
     */
    public static List<MySubject> parseHtml(String html) {

        List<MySubject> mySubjects = new ArrayList<>();

        try {
            Document scheduleHtml = Jsoup.parse(html);

            Element scheduleTbl = scheduleHtml.selectFirst("#kblist_table");
            Elements divs = scheduleTbl.select("div.timetable_con");
            int subjectIdx = 0;
            for (Element div : divs) {
                MySubject subject = new MySubject();

                String timeSpan = div.parent().parent().child(0).select("span").text().trim(); // div -> td -> tr -> td -> span (1-2)
                String weekInDay = div.parent().parent().parent().child(0).select("td span").text().trim(); // div -> td -> tr -> tbody -> tr -> td -> span (星期一)
                String title = div.selectFirst("span font").text().trim(); // (软件测试与维护)

                Elements fonts = div.select("p").select("font");
                List<String> information = new ArrayList<>();
                for (Element font : fonts) {
                    font.select("span").remove();
                    String content = font.text();
                    Log.i("", "parseHtml: " + content.trim());
                    if (content.trim().contains("上课地点")) { // 特殊
                        String[] sp = content.trim().split("上课地点");
                        information.add(sp[0]);
                        information.add("上课地点" + sp[1]);
                    } else {
                        information.addAll(Arrays.asList(content.trim().split("\n")));
                    }
                }
                /*
                     周数：2-4周(双),5-12周,16周
                     校区：大学城校区
                     上课地点：A2409
                     教师：陆璐(教授)
                     教学班：(2019-2020-1)-055100272-1
                     考核方式：考试
                     选课备注：无
                     课程学时组成：理论:48,实验:16
                     周学时：4
                     总学时：56
                     学分：3.5
                 */
                String weekStr = "";
                subject.setId(subjectIdx);
                subject.setName(title); // 软件测试与维护
                subject.setDay(text_day.get(weekInDay)); // 星期一
                for (String info : information) {
                    String[] kv = info.split("[:：]");
                    if (kv.length != 2) continue;
                    switch (kv[0]) {
                        case "周数": // !!! 2-4周(双),5-12周,16周
                            weekStr = kv[1].trim();
                        case "上课地点": // A2409
                            subject.setRoom(kv[1].trim());
                        case "教师": // 陆璐(教授)
                            subject.setTeacher(kv[1].trim());
                        case "周学时": // 4
                            subject.setTime(kv[1].trim());
                    }
                }

                if (!timeSpan.contains("-")) {
                    subject.setStart(Integer.valueOf(timeSpan));
                    subject.setStep(1);
                } else { // 1-2
                    String[] se = timeSpan.split("-");
                    subject.setStart(Integer.valueOf(se[0]));
                    subject.setStep(Integer.valueOf(se[1]) - Integer.valueOf(se[0]) + 1);
                }
                subject.setWeekList(getNumberFromWeekSpan(weekStr));

                mySubjects.add(subject);
                subjectIdx++;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }

        return mySubjects;
    }

    /**
     * 2-4周(双),5-12周,16周 -> 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 16
     */
    private static List<Integer> getNumberFromWeekSpan(String weekStr) {
        if (weekStr.trim().isEmpty())
            return new ArrayList<>();

        List<Integer> weekList = new ArrayList<>();
        weekStr = weekStr.replaceAll("周", "");
        // 2-4(双),5-12,16
        String[] weekSpans = weekStr.split(",");
        for (String span : weekSpans) { // 2-4(双)
            String[] numbers = span.replaceAll("\\([单双]\\)", "").split("-");

            if (numbers.length == 1)
                weekList.add(Integer.valueOf(numbers[0]));
            else {
                int start = Integer.valueOf(numbers[0]);
                int end = Integer.valueOf(numbers[1]);
                if (span.contains("(双)")) {
                    for (int i = start; i <= end; i++)
                        if (i % 2 == 0)
                            weekList.add(i);
                } else if (span.contains("(单)")) {
                    for (int i = start; i <= end; i++)
                        if (i % 2 != 0)
                            weekList.add(i);
                } else {
                    for (int i = start; i <= end; i++)
                        weekList.add(i);
                }
            }
        }
        return weekList;
    }
}
