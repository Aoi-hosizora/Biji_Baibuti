package com.baibuti.biji.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.InteractStrategy;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.contract.IScheduleInteract;
import com.baibuti.biji.model.po.MySubject;
import com.baibuti.biji.R;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.service.scut.ScheduleService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.WebViewActivity;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx_activity_result2.RxActivityResult;

import static android.app.Activity.RESULT_OK;

public class ScheduleFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.schedulefragment_weekview)
    WeekView m_weekView;

    @BindView(R.id.schedulefragment_timetableView)
    TimetableView m_timetableView;

    @BindView(R.id.schedulefragment_week_textview)
    TextView m_titleTextView;

    List<MySubject> mySubjects = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_schedule, container, false);
        ButterKnife.bind(this, view);
        isInit = true;
        init();
        return view;
    }

    private boolean isInit;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isInit = false;
    }

    private void init() {
        if (!isInit || !getUserVisibleHint())
            return;

        initView(view);
        AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {
            @Override
            public void onLogin(String username) {
                ActionRefresh_Clicked(false);
            }

            @Override
            public void onLogout() {
                ActionRefresh_Clicked(false);
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void initView(View view) {
        setHasOptionsMenu(true);

        // Toolbar
        Toolbar m_toolbar = view.findViewById(R.id.ScheduleFrag_Toolbar);
        m_toolbar.setTitle("课程表");
        m_toolbar.inflateMenu(R.menu.schedule_frag_action);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View v) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });
        m_toolbar.setOnMenuItemClickListener(menuItemClickListener);
        m_toolbar.setPopupTheme(R.style.popup_theme);

        // WeekView
        m_weekView.curWeek(1)
            .callback((int week) -> {
                int cur = m_timetableView.curWeek();
                // 更新切换后的日期，从当前周cur->切换的周week
                m_timetableView.onDateBuildListener()
                    .onUpdateDate(cur, week);
                m_timetableView.changeWeekOnly(week);
            })
            .callback(this::ChangeCurrWeek_Clicked)
            .isShow(false)
            .showView();

        // TimeTable
        m_timetableView.curWeek(1)
            .callback((int curWeek) ->
                m_titleTextView.setText(String.format(Locale.CHINA, "第 %d 周", curWeek)))
            .callback((View v, List<Schedule> scheduleList) -> {
                if (scheduleList.size() == 0) return;
                Schedule schedule = scheduleList.get(0);

                StringBuilder sb = new StringBuilder();
                for (int s = schedule.getStart(); s < schedule.getStart() + schedule.getStep() - 1; s++)
                    sb.append(s).append(", "); // 9, 3 -> 9, 10
                sb.append(schedule.getStart() + schedule.getStep() - 1);
                String times = sb.toString();
                String weeks = schedule.getWeekList().toString().trim();
                weeks = weeks.substring(1, weeks.length() - 1);

                final String message = "老师：" + schedule.getTeacher() + "\n" +
                    "上课地点：" + schedule.getRoom() + "\n" +
                    "上课周次：第 " + weeks + " 周\n" +
                    "上课节数：第 " + times + " 节";

                showAlert(getContext(), schedule.getName(), message,
                    "复制", (d, w) -> {
                        if (CommonUtil.copyText(getContext(), message))
                            showToast(getActivity(), "内容已复制");
                    },
                    "返回", null
                );
            })
            .showView();

        // Load data
        // ActionRefresh_Clicked(true);
    }

    /**
     * 主界面点击 点现周次界面
     */
    @OnClick(R.id.schedulefragment_layout)
    void MainLayout_Clicked() {
        if (m_weekView.isShowing()) {
            m_weekView.isShow(false);
            // 返回当前周
            int cur = m_timetableView.curWeek();
            m_timetableView.onDateBuildListener().onUpdateDate(cur, cur);
            m_timetableView.changeWeekOnly(cur);
        }
        else
            m_weekView.isShow(true);
    }

    /**
     * WeekView 修改当前周次
     */
    private void ChangeCurrWeek_Clicked() {
        final String[] items = new String[m_weekView.itemCount()];
        for (int i = 0; i < m_weekView.itemCount(); i++)
            items[i] = String.format(Locale.CHINA, "第 %d 周", i + 1);

        showAlert(getActivity(), "设置当前周",
            items, (v, w) -> {
                m_weekView.curWeek(w + 1).updateView();
                m_timetableView.changeWeekForce(w + 1);
                IScheduleInteract scheduleInteract = InteractStrategy.getInstance().getScheduleInteract(getContext());
                ProgressHandler.process(getContext(), "修改当前周...", true,
                    scheduleInteract.updateSchedule(MySubject.toJsons(mySubjects), w + 1), new InteractInterface<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            showToast(getContext(), "成功修改当前周为" + items[w]);
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(getContext(), "错误", "当前周修改失败");
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                        }
                    }
                );
            },
            "取消", null
        );
    }

    /**
     * ActionBar 点击事件
     */
    private Toolbar.OnMenuItemClickListener menuItemClickListener = (menu) -> {
        switch (menu.getItemId()) {
            case R.id.action_import_schedule:
                ActionImportSchedule_Clicked();
                break;
            case R.id.action_refresh_schedule:
                ActionRefresh_Clicked(true);
                break;
            case R.id.action_delete_schedule:
                ActionDeleteSchedule_Clicked();
        }
        return true;
    };

    /**
     * ActionBar 导入课程表
     */
    private void ActionImportSchedule_Clicked() {
        showToast(getContext(), "当前版本暂时只支持获取华工的课程表，并需要在华工的校园网内打开。");
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        RxActivityResult.on(this).startIntent(intent)
            .subscribe((result) -> {
                if (result.resultCode() == RESULT_OK) {
                    String html = result.data().getStringExtra("html");
                    CbImportSchedule(html);
                }
            }).isDisposed();


//        AssetManager manager = getResources().getAssets();
//        try {
//            String html = StringUtil.readFromInputStream(manager.open("schedule.html"));
//            CbImportSchedule(html);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }

    /**
     * 活动返回，解析数据 (onActivityResult)
     */
    private void CbImportSchedule(String html) {
        mySubjects = ScheduleService.parseHtml(html);
        if (mySubjects.size() == 0) {
            showAlert(getActivity(), "错误", "返回的课程表数据无法解析。");
            return;
        }

        // show
        m_weekView.source(mySubjects).showView();
        m_timetableView.source(mySubjects).updateView();

        // save
        IScheduleInteract scheduleInteract = InteractStrategy.getInstance().getScheduleInteract(getActivity());
        ProgressHandler.process(getActivity(), "上传课程表中...", true,
            scheduleInteract.updateSchedule(MySubject.toJsons(mySubjects), m_timetableView.curWeek()), new InteractInterface<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    showToast(getActivity(), "课程表上传成功");
                }

                @Override
                public void onError(String message) {
                    showAlert(getActivity(), "错误", "课程表上传失败");
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getActivity(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * ActionBar 刷新课程表
     */
    private void ActionRefresh_Clicked(boolean isShowToast) {
        // Load Data
        IScheduleInteract scheduleInteract = InteractStrategy.getInstance().getScheduleInteract(getContext());
        ProgressHandler.process(getActivity(), "更新中...", true,
            scheduleInteract.querySchedule(), new InteractInterface<Pair<String, Integer>>() {
                @Override
                public void onSuccess(Pair<String, Integer> pair) {
                    String scheduleJson = pair.first;
                    int currWeek = pair.second;

                    if (scheduleJson.trim().isEmpty()) {
                        if (isShowToast)
                            showToast(getActivity(), "尚未设置课程表");
                    } else {
                        // Show Schedule
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity == null) return;

                        mySubjects = MySubject.fromJson(scheduleJson);
                        m_weekView.curWeek(currWeek).source(mySubjects).showView();
                        m_timetableView.curWeek(currWeek).source(mySubjects).updateView();

                        if (isShowToast)
                            showToast(getActivity(), "课程表更新完成");
                    }
                }

                @Override
                public void onError(String message) {
                    showAlert(getActivity(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getActivity(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * ActionBar 删除课程表
     */
    private void ActionDeleteSchedule_Clicked() {
        showAlert(getActivity(),
            "删除", "是否删除课程表？",
            "确定", (d, v) -> {
                IScheduleInteract scheduleInteract = InteractStrategy.getInstance().getScheduleInteract(getActivity());
                ProgressHandler.process(getContext(), "删除课程表中...", true,
                    scheduleInteract.deleteSchedule(), new InteractInterface<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            showToast(getActivity(), "删除课表成功");
                            m_weekView.source(new ArrayList<MySubject>()).updateView();
                            m_timetableView.source(new ArrayList<MySubject>()).updateView();
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(getActivity(), "错误", message);
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(getActivity(), "错误", "网络错误：" + throwable.getMessage());
                        }
                    }
                );
            },
            "取消", null
        );
    }
}
