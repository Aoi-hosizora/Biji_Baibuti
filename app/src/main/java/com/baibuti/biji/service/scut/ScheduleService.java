package com.baibuti.biji.service.scut;

import com.baibuti.biji.model.po.MySubject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static List<MySubject> parseHtml(String html) {
        try{
            //Document scheduleHtml = Jsoup.connect("url");
            Document scheduleHtml = Jsoup.parse(html);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(DocumentUtil.getFilePath("Html")+"html.txt"));
            fileOutputStream.write(html.getBytes());
            fileOutputStream.close();

            Element schedule = scheduleHtml.select("#ylkbTable").get(0);//课表元素
            Element scheduleTable = schedule.getElementById("table2");//第二种课表
            Elements scheduleContents = scheduleTable.select(".timetable_con");//所有课程元素
            int countId = 0;
            for(Element scheduleContent: scheduleContents){//对于每一个课程元素
                //Element festival = scheduleContent.parent().previousElementSibling();
                //Element weekSibling = scheduleContent.parent().parent().parent().child(0);
                Element titleChild = scheduleContent.select(".title").get(0);//课程标题元素
                Element timeChild = titleChild.nextElementSibling().child(0);
                Element classroomChild = timeChild.nextElementSibling();
                Element teacherChild = classroomChild.nextElementSibling();
                Element courseIdChild = teacherChild.nextElementSibling();
                Element attachmentChild = courseIdChild.nextElementSibling();
                Element classHourChild = attachmentChild.nextElementSibling();
                //Element weekHourChild = classHourChild.nextElementSibling();
                //Element totalHourChild = weekHourChild.nextElementSibling();
                MySubject temp = new MySubject();
                temp.setName(titleChild.text());
                temp.setRoom(classroomChild.text());
                temp.setDay(text_day.get(scheduleContent.parent().parent().parent().child(0).text()));
                temp.setTeacher(teacherChild.text());
                temp.setTime(classHourChild.text());
                List<Integer> weekList = new ArrayList<>();
                // 处理周数字符串，格式："周数：14周" 或 "周数：1-4周,7-12周"
                List<Integer> tempList;

                // 根据 , 分割周数字符串
                List<String> weekPeriods = Arrays.asList(timeChild.text().split(","));
                for(String weekPeriod: weekPeriods) {
                    tempList = getNumber(weekPeriod);
                    if(tempList.size() != 0) {
                        for (int i = tempList.get(0); i <= tempList.get(1); i++)
                            weekList.add(i);
                    }
                }

                temp.setWeekList(weekList);
                temp.setId(countId);

                tempList = getNumber(scheduleContent.parent().previousElementSibling().text());
                temp.setStart(tempList.get(0));
                temp.setStep(tempList.get(1)-tempList.get(0)+1);
                countId++;
                mySubjects.add(temp);
            }
        } catch(Exception e) {
            e.printStackTrace();
            showAlert(getActivity(), "错误", "返回的课程表数据无法解析。");
            return;
        }

    }

    /**
     * 提取数字（格式\w*-\w*
     */
    private List<Integer> getNumber(String s){
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        String[] ss = s.split("-");
        List<Integer> temp = new ArrayList<>();
        if(ss.length == 2) {
            Matcher m_start = p.matcher(ss[0]);
            Matcher m_stop = p.matcher(ss[1]);
            String s_start = m_start.replaceAll("").trim();
            String s_stop = m_stop.replaceAll("").trim();
            int start = Integer.parseInt(s_start);
            int stop = Integer.parseInt(s_stop);
            temp.add(start);
            temp.add(stop);
        }
        else if(ss.length == 1){
            Matcher m_start = p.matcher(ss[0]);
            String s_start = m_start.replaceAll("").trim();
            int start = Integer.parseInt(s_start);
            temp.add(start);
            temp.add(start);
        }
        return temp;
    }
}
