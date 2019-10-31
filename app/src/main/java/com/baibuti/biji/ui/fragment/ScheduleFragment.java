package com.baibuti.biji.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baibuti.biji.model.dao.local.ScheduleDao;
import com.baibuti.biji.model.po.MySubject;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.module.file.DocumentUtil;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.WebviewActivity;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleFragment extends Fragment {

    private Activity activity;
    private View view;
    private Toolbar m_toolbar;

    private WeekView mWeekView;
    private TimetableView mTimetableView;
    private TextView titleTextView;
    private ConstraintLayout mLayout;
    private List<MySubject> mySubjects = new ArrayList<>();
    private ProgressDialog loadingDialog;

    private ScheduleDao scheduleDao;

    //记录切换的周次，不一定是当前周
    int target = -1;

    private HashMap<String, Integer> text_day = new HashMap<String, Integer>(){};

    /**
     * 标记登录时是否刷新过
     */
    private boolean HasRefreshed = false;

    private static final int REQUEST_CODE_FOR_WEBAC = 100;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_scheduletab, container, false);

            scheduleDao = new ScheduleDao(getContext());
            loadingDialog = new ProgressDialog(getContext());
            loadingDialog.setMessage(getResources().getString(R.string.NoteFrag_LoadingData));
            loadingDialog.setCanceledOnTouchOutside(false);

            text_day.put("星期一", 1);
            text_day.put("星期二", 2);
            text_day.put("星期三", 3);
            text_day.put("星期四", 4);
            text_day.put("星期五", 5);
            text_day.put("星期六", 6);
            text_day.put("星期日", 7);

            initView(view);
            initTimetableView(view);

            registerAuthActions();
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
    }

    private void initView(View view) {
        initToolbar(view);
    }

    /**
     * 从WebviewActivity返回html数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_FOR_WEBAC && resultCode == 101){
            String html = data.getStringExtra("html");
            getSchedule(html);
        }
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);

        m_toolbar = view.findViewById(R.id.ScheduleFrag_Toolbar);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openNavMenu();
            }
        });
        m_toolbar.setTitle(R.string.ScheduleFrag_Header);

        m_toolbar.setPopupTheme(R.style.popup_theme);

        m_toolbar.inflateMenu(R.menu.schedulefragment_menu);
        m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_import_schedule:
                        Intent intent = new Intent(getContext(), WebviewActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_FOR_WEBAC);
                        break;
                    case R.id.action_refresh_schedule:
                        getScheduleFromBackEnd();
                        break;
                }
                return true;
            }
        });
    }

    private void getScheduleFromBackEnd(){
        if(!AuthManager.getInstance().isLogin()){
            Toast.makeText(getContext(), "未登录", Toast.LENGTH_SHORT).show();
            return;
        }
        mySubjects.clear();
        showLoadingDialog("加载中...");
        //Toast.makeText(getContext(), "待完成", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String scheduleJson = scheduleDao.querySchedule();
                if(scheduleJson.equals(""))
                    return;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mySubjects.addAll(JSON.parseArray(scheduleJson, MySubject.class));

                        //展示课表
                        mWeekView.source(mySubjects).showView();
                        mTimetableView.source(mySubjects).updateView();
                        cancelLoadingDialog();
                    }
                });
            }
        }).start();
    }

    private void getSchedule(String html){

        mySubjects.clear();
        //解析课表并装载数据
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
                //处理周数字符串，格式："周数：14周" 或 "周数：1-4周,7-12周"
                List<Integer> tempList;
                List<String> weekPeriods = getWeekPeriod(timeChild.text());
                for(String weekPeriod: weekPeriods){
                    tempList = getNumber(weekPeriod);
                    if(tempList.size() != 0){
                        for(int i = tempList.get(0); i <= tempList.get(1); i++)
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
        }catch(Exception e) {
            e.printStackTrace();
            Log.e("test", "getSchedule: 解析课表出错");
        }

        final String subjectsJsonStr = JSON.toJSONString(mySubjects);
        Log.e("测试", "getSchedule: "  + subjectsJsonStr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(scheduleDao.querySchedule(false).equals(""))
                    scheduleDao.newSchedule(subjectsJsonStr);
                else {
                    scheduleDao.deleteSchedule(false);
                    scheduleDao.newSchedule(subjectsJsonStr);
                }
            }
        }).start();

        //展示课表
        mWeekView.source(mySubjects).showView();
        mTimetableView.source(mySubjects).updateView();

    }

    /**
     * 根据','分割周数字符串
     * @param weekString
     * @return
     */
    private List<String> getWeekPeriod(String weekString){
        String[] ss = weekString.split(",");
        return new ArrayList<>(Arrays.asList(ss));
    }

    /**
     * 提取数字（格式\w*-\w*）
     * @param s
     * @return
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

    /**
     * 将assets中的html资源转为字符串
     * @param context
     * @param fileName
     * @return
     */
    public String getHtmlString(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 初始化课程控件
     * @param v
     */
    private void initTimetableView(View v) {

        mLayout = v.findViewById(R.id.schedulefragment_layout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果周次选择已经显示了，那么将它隐藏，更新课程、日期
                //否则，显示
                if (mWeekView.isShowing()) hideWeekView();
                else showWeekView();
            }
        });

        //获取控件
        mWeekView = v.findViewById(R.id.schedulefragment_weekview);
        mTimetableView = v.findViewById(R.id.schedulefragment_timetableView);
        titleTextView = v.findViewById(R.id.schedulefragment_week_textview);

        //设置周次选择属性
        mWeekView.curWeek(1)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        mTimetableView.curWeek(1)
                .curTerm("大三下学期")
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        display(scheduleList);
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        Toast.makeText(getContext(),
                                "长按:周" + day  + ",第" + start + "节",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
                .showView();
        //展示课表
        mWeekView.source(mySubjects).showView();
        mTimetableView.source(mySubjects).showView();
    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     */
    @Override
    public void onStart() {
        super.onStart();
        mTimetableView.onDateBuildListener()
                .onHighLight();
    }

    /**
     * 周次选择布局的左侧被点击时回调
     * 对话框修改当前周次
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[20];
        int itemCount = mWeekView.itemCount();
        for (int i = 0; i < itemCount; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mTimetableView.changeWeekForce(target + 1);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 显示内容
     *
     * @param beans
     */
    protected void display(List<Schedule> beans) {
        String str = "";
        for (Schedule bean : beans) {
            str += bean.getName() + ","+bean.getWeekList().toString()+","+bean.getStart()+","+bean.getStep()+"\n";
        }
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 隐藏周次选择，此时需要将课表的日期恢复到本周并将课表切换到当前周
     */
    public void hideWeekView(){
        mWeekView.isShow(false);
        titleTextView.setTextColor(getResources().getColor(R.color.app_course_textcolor_blue));
        int cur = mTimetableView.curWeek();
        mTimetableView.onDateBuildListener()
                .onUpdateDate(cur, cur);
        mTimetableView.changeWeekOnly(cur);
    }

    public void showWeekView(){
        mWeekView.isShow(true);
        titleTextView.setTextColor(getResources().getColor(R.color.app_red));
    }

    private void refresh(){
        mySubjects.clear();
        showLoadingDialog("加载中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String scheduleJson = scheduleDao.querySchedule();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!scheduleJson.equals("")){
                            mySubjects.addAll(JSON.parseArray(scheduleJson, MySubject.class));
                        }
                        //展示课表
                        mWeekView.source(mySubjects).showView();
                        mTimetableView.source(mySubjects).showView();
                        cancelLoadingDialog();
                    }
                });
            }
        }).start();

    }

    /**
     * 订阅登录注销事件
     */
    private void registerAuthActions() {
        AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {

            // TODO
            public void onLogin(String UserName) {
                if(getUserVisibleHint()) {
                    Log.e("测试", "ScheduleFragment.onLogin: 调用");
                    scheduleDao = new ScheduleDao(getContext());
                    refresh();
                    HasRefreshed = true;
                }
                else
                    HasRefreshed = false;
            }

            @Override
            public void onLogout() {
                if(getUserVisibleHint()) {
                    scheduleDao = new ScheduleDao(getContext());
                    refresh();
                    HasRefreshed = true;
                }
                else
                    HasRefreshed = false;
            }
        });
    }

    /**
     * 对用户可见时，判断是否需要刷新
     */
    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint() && !HasRefreshed){
            scheduleDao = new ScheduleDao(getContext());
            refresh();
            HasRefreshed = true;
        }
    }

    private void showLoadingDialog(String message){
        if(!loadingDialog.isShowing()) {
            loadingDialog.setMessage(message);
            loadingDialog.show();
        }
    }

    private void cancelLoadingDialog(){
        if(loadingDialog.isShowing())
            loadingDialog.cancel();
    }

}
