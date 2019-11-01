package com.baibuti.biji.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.ui.adapter.GroupRadioAdapter;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.ModifyNoteActivity;
import com.baibuti.biji.ui.activity.OCRActivity;
import com.baibuti.biji.ui.activity.ViewModifyNoteActivity;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.ui.adapter.GroupAdapter;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.ui.adapter.NoteAdapter;
import com.baibuti.biji.ui.dialog.GroupDialog;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.listView.RecyclerListScrollHelper;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.model.dao.local.GroupDao;
import com.baibuti.biji.model.dao.local.NoteDao;
import com.baibuti.biji.util.fileUtil.AppPathUtil;
import com.baibuti.biji.util.fileUtil.SaveNameUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wyt.searchbox.SearchFragment;
import com.baibuti.biji.util.stringUtil.SearchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import me.kareluo.imaging.IMGEditActivity;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener {

    // region 声明: View UI ProgressDialog Toolbar DrawerLayout

    private View view;

    private RecyclerViewEmptySupport mNoteList;

    private FloatingActionsMenu m_fabMenu;
    private SearchFragment searchFragment;
    private SwipeRefreshLayout mSwipeRefresh;

    private ProgressDialog loadingDialog;
    private ProgressDialog loadingGroupDialog;

    private Toolbar m_toolbar;
    private AppBarLayout m_appBarLayout;

    private DrawerLayout m_drawerLayout;
    private ListView m_nav_groupList;

    private Dialog m_LongClickNotePopupMenu;
    private Note LongClickNoteItem;

    // endregion 声明: View UI ProgressDialog Toolbar DrawerLayout

    // region 声明: flag REQ

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    private static final int REQ_NOTE_NEW = 2; // 从 MNote 返回
    private static final int REQ_NOTE_UPDATE = 1; // 从 VMNote 返回

    private static final int REQUEST_CROP = 3; // 剪辑
    private static final int REQUEST_TAKE_PHOTO = 4; // 拍照

    // endregion 声明: flag REQ

    // region 声明: 一些等待的秒数

    private final static int SearchReturnSecond = 200;
    private final static int SwipeRefreshSecond = 100;
    private final static int ShowGroupDlgSecond = 10;
    private final static int HideGroupPrgSecond = 100;

    /**
     * 滑动列表 隐藏速度 (越小越慢)
     */
    private final int ScrollShowHideInterpolator = 1;

    /**
     * 标记登录时是否刷新过
     */
    private boolean HasRefreshed = false;

    // endregion 声明: 一些等待的秒数

    // region 创建界面 菜单栏 浮动菜单 等待框 搜索框 右划菜单 onCreateView initToolbar initFabMenu setupProgressAndSR initSearchFrag ShowLogE onClick initRightMenu

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_notetab, container, false);

            m_fabMenu = view.findViewById(R.id.note_fabmenu);

            mNoteList = view.findViewById(R.id.note_list);
            View ListEmptyView = view.findViewById(R.id.note_emptylist);
            mNoteList.setEmptyView(ListEmptyView);

            setupProgressAndSR();

            initToolbar(view);
            initFabMenu(view);
            initData(); // GetDao & List
            initAdapter();
            initListView(NoteList);
            initSearchFrag();
            initRightMenu();

            initListScroll();

            registerAuthActions();
        }

        return view;
    }

    /**
     * 初始化菜单栏以及标题
     *
     * @param view
     */
    private void initToolbar(View view) {

        m_appBarLayout = view.findViewById(R.id.NoteFrag_AppbarLayout);
        m_toolbar = view.findViewById(R.id.NoteFrag_Toolbar);
        m_toolbar.inflateMenu(R.menu.notefragment_actionbar);
        m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchFragment.show(getActivity().getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
                        break;
                    case R.id.action_modifygroup:
                        ModifyGroupMenuClick();
                        break;
                    case R.id.action_search_back:
                        SearchGroupBack();
                        break;
                }
                return true;
            }
        });
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openNavMenu();
            }
        });
        m_toolbar.setTitle(R.string.NoteFrag_Header);
    }

    /**
     * 初始化浮动按钮菜单
     *
     * @param view
     */
    private void initFabMenu(View view) {
        FloatingActionButton mNotePhoto = view.findViewById(R.id.note_photo);
        FloatingActionButton mNoteEdit = view.findViewById(R.id.note_edit);

        View back = view.findViewById(R.id.note_fabmenu_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_fabMenu.collapse();
            }
        });

        m_fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        back.setVisibility(View.VISIBLE);
                    }
                }, 50);
            }

            @Override
            public void onMenuCollapsed() {
                back.setVisibility(View.GONE);
            }
        });

        mNotePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_fabMenu.collapse();
                takePhoto();
            }
        });
        mNoteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_fabMenu.collapse();
                HandleNewUpdateNote(true, null, false);
            }
        });
    }

    /**
     * 判断 fab 是否展开
     * @return
     */
    public boolean isFabExpanded() {
        return m_fabMenu.isExpanded();
    }

    /**
     * 隐藏 fab
     */
    public void collapseFab() {
        m_fabMenu.collapse();
    }

    /**
     * 设置 SwipeRefresh ProgressDialog
     */
    private void setupProgressAndSR() {
        mSwipeRefresh = view.findViewById(R.id.note_listsrl);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(500);
            }
        });

        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setMessage(getResources().getString(R.string.NoteFrag_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);

        loadingGroupDialog = new ProgressDialog(getContext());
        loadingGroupDialog.setMessage(getContext().getString(R.string.NoteFrag_LoadingGroupData));
        loadingGroupDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 初始化搜索框
     */
    private void initSearchFrag() {
        // 添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setAllowReturnTransitionOverlap(true);
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {

                try {
                    if (!keyword.isEmpty()) {

                        IsSearching = true;
                        SearchingStr = keyword;

                        initListView(search(keyword));

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
                        mSwipeRefresh.setEnabled(false);
                        m_fabMenu.setVisibility(View.GONE);
                        m_toolbar.setTitle(String.format(getContext().getString(R.string.NoteFrag_menu_search_content), keyword));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    /**
     * 初始化右划菜单
     */
    private void initRightMenu() {

        // 布局
        m_drawerLayout = view.findViewById(R.id.id_noteFrag_drawer_layout);
        NavigationView m_navigationView = view.findViewById(R.id.id_noteFrag_Right_nav);

        m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        m_navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // 宽度
        ViewGroup.LayoutParams params = m_navigationView.getLayoutParams();
        params.width = metrics.widthPixels / 3 * 2;
        m_navigationView.setLayoutParams(params);

        // 列表
        m_nav_groupList = view.findViewById(R.id.id_NoteFrag_nav_GroupList);

        // 按钮
        Button m_groupMgrButton = view.findViewById(R.id.id_NoteFrag_nav_GroupMgrButton);
        m_groupMgrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsGrouping)
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.NoteFrag_GroupingMgrAlertTitle)
                            .setMessage(R.string.NoteFrag_GroupingMgrAlertMsg)
                            .setPositiveButton(R.string.NoteFrag_GroupingMgrAlertPosDoButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SearchGroupBack();
                                    ShowGroupDialog();
                                }
                            })
                            .setNegativeButton(R.string.NoteFrag_GroupingMgrAlertNegBackButton, null)
                            .create().show();
                else
                    ShowGroupDialog();
            }
        });

        ImageButton m_groupMgrBackButton = view.findViewById(R.id.id_NoteFrag_nav_BackButton);
        m_groupMgrBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_drawerLayout.closeDrawer(Gravity.END);
            }
        });
    }

    /**
     * List 滑动隐藏
     */
    private void initListScroll() {
        mNoteList.addOnScrollListener(new RecyclerListScrollHelper(new RecyclerListScrollHelper.OnShowHideScrollListener() {

            @Override
            public void onHide() {
                // m_appBarLayout.animate().translationY(-m_appBarLayout.getHeight()).setInterpolator(new AccelerateInterpolator(ScrollShowHideInterpolator));

                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) m_fabMenu.getLayoutParams();
                m_fabMenu.animate().translationY(m_fabMenu.getHeight()+layoutParams.bottomMargin).setInterpolator(new AccelerateInterpolator(ScrollShowHideInterpolator));
            }

            @Override
            public void onShow() {
                // m_appBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(ScrollShowHideInterpolator));
                m_fabMenu.animate().translationY(0).setInterpolator(new DecelerateInterpolator(ScrollShowHideInterpolator));
            }
        }));
    }

    /**
     * 全局设置 Log 格式
     *
     * @param FunctionName
     * @param Msg
     */
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "NoteFragment";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }

    /**
     * 弹出菜单点击
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_NoteFrag_PopupMenu_ViewNote:
                HandleNewUpdateNote(false, LongClickNoteItem, false);
                LongClickNoteItem = null;
                m_LongClickNotePopupMenu.cancel();
            break;
             case R.id.id_NoteFrag_PopupMenu_ChangeGroup:
                 modifyNoteGroup(LongClickNoteItem);
                 LongClickNoteItem = null;
                 m_LongClickNotePopupMenu.cancel();
             break;
            case R.id.id_NoteFrag_PopupMenu_DeleteNote:
                DeleteNote(getView(), LongClickNoteItem);
                LongClickNoteItem = null;
                m_LongClickNotePopupMenu.cancel();
            break;
            case R.id.id_NoteFrag_PopupMenu_Cancel:
                LongClickNoteItem = null;
                m_LongClickNotePopupMenu.cancel();
            break;
        }
    }

    // endregion 创建界面 菜单栏 浮动菜单 等待框 搜索框 右划菜单

    // region 搜索处理 IsSearching IsSearchingNull getIsSearching SearchingStr search SearchGroupBack

    /**
     * 用于返回数据时判断当前是否处在搜索页面
     * 而进行下一步处理刷新 ListView
     */
    private boolean IsSearching = false;

    private boolean IsSearchingNull = false;

    /**
     * MainAct用
     * @return
     */
    public boolean getIsSearching() {
        // TODO ???
        return !SearchingStr.equals("");
    }

    /**
     * 当 IsSearching 时表示当前所有页面的 keyWord
     */
    private String SearchingStr = "";

    /**
     * 查找笔记功能
     *
     * @param str
     * @return
     */
    private List<Note> search(String str) {
        List<Note> notelist = SearchUtil.getSearchItems(NoteList.toArray(new Note[0]), str);
        noteAdapter.notifyDataSetChanged();

        IsSearchingNull = notelist.isEmpty();

        if (IsSearchingNull) {
            Toast.makeText(getContext(), R.string.NoteFrag_SearchNullToast, Toast.LENGTH_SHORT).show();
        }

        return notelist;
    }

    /**
     * 返回原界面，退出搜索或分类，以及重新登陆
     */
    public void SearchGroupBack() {
        if (!loadingDialog.isShowing())
            loadingDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SearchReturnSecond); // 200
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            NoteDao noteDao = new NoteDao(getContext());

                            // TODO !!!
                            //     java.lang.NullPointerException: Attempt to invoke virtual method '
                            //          android.database.sqlite.SQLiteDatabase android.content.Context.openOrCreateDatabase
                            //          (java.lang.String, int, android.database.sqlite.SQLiteDatabase$CursorFactory, android.database.DatabaseErrorHandler)
                            //     ' on a null object reference
                            try {
                                NoteList = noteDao.queryAllNotes();
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }


                            // TODO !!!
                            //     java.lang.NullPointerException: Attempt to invoke virtual method '
                            //          void android.support.v4.app.FragmentActivity.runOnUiThread(java.lang.Runnable)
                            //     ' on a null object reference

                            Activity a = getActivity();

                            if (a != null) {
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initListView(NoteList);

                                        mSwipeRefresh.setEnabled(true);
                                        m_fabMenu.setVisibility(View.VISIBLE);

                                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(false);
                                        m_toolbar.getMenu().findItem(R.id.action_search).setEnabled(true);
                                        m_toolbar.getMenu().findItem(R.id.action_search).setIcon(R.drawable.search);
                                        m_toolbar.setTitle(R.string.NoteFrag_Header);

                                        IsSearching = false;
                                        SearchingStr = "";
                                        IsSearchingNull = false;

                                        IsGrouping = false;
                                        currGroup = null;
                                        currGroupIdx = -1;
                                        IsGroupingNull = false;

                                        loadingDialog.cancel();

                                    }
                                });
                            }
                        }
                    }).start();
                }
                catch (NullPointerException ex) {
                    ex.printStackTrace();
                    Log.e("", "run: NullPointerException" );
                    loadingDialog.cancel();
                }

            }
        }).start();

    }

    // endregion 搜索处理

    // region 列表数据 初始化各种数据和适配器 下拉刷新 initData initAdapter refreshNoteList refreshGroupList refreshData refreshAll

    private List<Note> NoteList;
    private List<Group> GroupList;

    private NoteAdapter noteAdapter;
    private GroupAdapter groupAdapter;

    /**
     * 初始化 Dao 和 List 数据
     */
    @WorkerThread
    public void initData() {
        NoteDao noteDao = new NoteDao(this.getContext());
        GroupDao groupDao = new GroupDao(this.getContext());

        NoteList = noteDao.queryAllNotes();
        GroupList = groupDao.queryAllGroups();
    }

    /**
     * 初始化 note/group Adapter
     */
    public void initAdapter() {
        noteAdapter = new NoteAdapter();
        groupAdapter = new GroupAdapter(getContext(), GroupList);
    }

    /**
     * 刷新 笔记列表
     */
    @WorkerThread
    public void refreshNoteList() {
        NoteDao noteDao = new NoteDao(getContext());
        noteDao = new NoteDao(getContext());
        NoteList = noteDao.queryAllNotes();
        Collections.sort(NoteList);
        noteAdapter = new NoteAdapter();
        noteAdapter.setmNotes(NoteList);
        noteAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新 分组列表
     */
    @WorkerThread
    public void refreshGroupList() {
        GroupDao groupDao = new GroupDao(getContext());
        groupDao = new GroupDao(getContext());
        GroupList = groupDao.queryAllGroups();
        Collections.sort(GroupList);
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新数据，用于 下拉 和 返回刷新
     *
     * @param ms 毫秒
     */
    private void refreshData(int ms) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        refreshAll();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initListView(NoteList);

                                mSwipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }

        }, ms);
    }

    /**
     * 下拉刷新用
     */
    public void refreshAll() {
        initData();
        initAdapter();
        refreshNoteList();
        refreshGroupList();
    }

    // endregion 初始化各种数据和适配器 下拉刷新

    // region 显示分组 分组显示 ShowGroupDialog ModifyGroupMenuClick currGroup showAsGroup getGroupOfNote showAsDefault

    /**
     * 显示 分类 对话框
     */
    public void ShowGroupDialog() {
        m_drawerLayout.closeDrawer(Gravity.END);
        loadingGroupDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GroupDialog dialog = new GroupDialog(getActivity(), new GroupDialog.OnUpdateGroupListener() {

                    @Override
                    public void OnUICreateFinished() {
                        // 双重等待隐藏等待对话框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingGroupDialog.dismiss();
                            }
                        }, HideGroupPrgSecond); // 100
                    }

                    @Override
                    public void UpdateGroupFinished() {

                        // 更新完分组信息后同时在列表中刷新数据
                        refreshData(SwipeRefreshSecond); // 100

                        if (IsSearching)
                            if (!IsSearchingNull)
                                initListView(search(SearchingStr));
                            else
                                initListView(NoteList);
                        else
                            // 防止冲突，直接回复
                            if (IsGrouping)
                                SearchGroupBack();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        }, ShowGroupDlgSecond); // 10
    }

    /**
     * 菜单点击，显示左滑菜单
     */
    public void ModifyGroupMenuClick() {

        // 列表
        GroupDao groupDao = new GroupDao(getContext());

        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Group> groups = groupDao.queryAllGroups();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(groups);

                        groups.add(0, Group.AllGroups);

                        GroupRadioAdapter groupRadioAdapter = new GroupRadioAdapter(getContext(), groups, new GroupRadioAdapter.OnRadioButtonSelect() {

                            @Override
                            public void onSelect(int position) {
                                Group curr = groups.get(position);
                                if (curr == Group.AllGroups)
                                    showAsDefault();
                                else
                                    showAsGroup(curr, position);
                            }
                        });

                        m_nav_groupList.setAdapter(groupRadioAdapter);

                        if (currGroup != null)
                            groupRadioAdapter.setChecked(currGroupIdx);
                        else
                            groupRadioAdapter.setChecked(Group.AllGroups);

                        groupAdapter.notifyDataSetChanged();

                        m_drawerLayout.openDrawer(Gravity.END);
                    }
                });
            }
        }).start();
    }

    private Group currGroup = null;
    private int currGroupIdx = -1;

    /**
     * MainAct用
     * @return
     */
    public boolean getIsGrouping() {
        // TODO ???
        return currGroup != null;
    }

    private boolean IsGrouping = false;
    private boolean IsGroupingNull = false;

    /**
     * 判断是否开着,MainAct用
     * @return
     */
    public boolean getDrawerIsOpen() {
        return m_drawerLayout.isDrawerOpen(Gravity.END);
    }

    /**
     * 关闭侧边栏
     */
    public void closeDrawer() {
        m_drawerLayout.closeDrawer(Gravity.END);
    }

    /**
     * 按分组显示
     * @param group
     */
    private void showAsGroup(Group group, int idx) {
        currGroup = group;
        currGroupIdx = idx;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {

                List<Note> groupNotes = getGroupOfNote(group);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IsGrouping = true;
                        IsGroupingNull = groupNotes.isEmpty();

                        if (IsGroupingNull) {
                            Toast.makeText(getContext(), R.string.NoteFrag_GroupNullToast, Toast.LENGTH_SHORT).show();
                        }

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
                        m_toolbar.getMenu().findItem(R.id.action_search).setEnabled(false);
                        m_toolbar.getMenu().findItem(R.id.action_search).setIcon(R.drawable.ic_search_grey_24dp);
                        mSwipeRefresh.setEnabled(false);

                        initListView(groupNotes);
                        closeDrawer();

                        m_toolbar.setTitle(String.format(Locale.CHINA,
                                getString(R.string.NoteFrag_GroupingTitle), group.getName()));
                    }
                });
//            }
//        });

    }

    /**
     * 显示全部分组
     */
    @WorkerThread
    private void showAsDefault() {
        currGroup = null;
        currGroupIdx = -1;
        IsGrouping = false;

        SearchGroupBack();
        closeDrawer();
    }
    
    /**
     * 获取分组笔记
     * @param group
     * @return
     */
    @WorkerThread
    private List<Note> getGroupOfNote(Group group) {
        List<Note> ret = new ArrayList<>();
        NoteDao noteDao = new NoteDao(getContext());
        List<Note> allNotes = noteDao.queryAllNotes();
        for (Note note : allNotes)
            if (note.getGroup().getName().equals(group.getName()))
                ret.add(note);
        return ret;
    }

     /**
      * 修改分组
      */
     private void modifyNoteGroup(final Note note) {

         ProgressDialog loading = new ProgressDialog(getContext());
         loading.setMessage("分组数据加载中...");
         loading.setCancelable(false);
         if (!loading.isShowing())
             loading.show();

         new Thread(new Runnable() {
             @Override
             public void run() {

                 refreshGroupList();

                 getActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {

                         AlertDialog GroupSettingDialog = new AlertDialog
                                 .Builder(getContext())
                                 .setTitle(R.string.MNoteActivity_GroupSetAlertTitle)
                                 .setNegativeButton(R.string.MNoteActivity_GroupSetAlertNegativeButtonForCancel, null)
                                 .setSingleChoiceItems(groupAdapter, 0, new DialogInterface.OnClickListener() {

                                     @Override
                                     public void onClick(final DialogInterface dialog, int which) {

                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {

                                                 // 修改保存数据库
                                                 NoteDao noteDao = new NoteDao(getContext());
                                                 Group group = GroupList.get(which);
                                                 note.setGroup(group, true);
                                                 noteDao.updateNote(note);

                                                 getActivity().runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {

                                                         // 更新数据
                                                         if (IsSearching && !IsSearchingNull)
                                                             initListView(search(SearchingStr));
                                                         else if (IsGrouping && !IsGroupingNull)
                                                             initListView(getGroupOfNote(currGroup));
                                                         else
                                                             initListView(NoteList);


                                                         // 排序显示
                                                         Collections.sort(NoteList);
                                                         noteAdapter.notifyDataSetChanged();

                                                         // 提示
                                                         Toast.makeText(getActivity(), String.format(Locale.CHINA,
                                                                 getString(R.string.NoteFrag_ModifyGroupToast), note.getTitle(), group.getName()), Toast.LENGTH_SHORT).show();

                                                         dialog.dismiss();
                                                     }
                                                 });
                                             }
                                         }).start();
                                     }
                                 }).create();


                         GroupSettingDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                             @Override
                             public void onShow(DialogInterface dialog) {
                                 loading.dismiss();
                             }
                         });

                         GroupSettingDialog.show();
                     }
                 });
             }
         }).start();
     }

    // endregion 显示分组 分组显示

    // region 初始化笔记列表 进入笔记 SelectedNoteItem initListView

    /**
     * 当前选中的笔记，用于返回时修改列表
     */
    private int SelectedNoteItem;

    /**
     * 初始化 笔记列表 View，并处理点击笔记事件
     *
     * @param nlist
     */
    private void initListView(final List<Note> nlist) {

        mNoteList.addItemDecoration(new SpacesItemDecoration(0));//设置item间距

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        mNoteList.setLayoutManager(layoutManager);

        Collections.sort(nlist);
        noteAdapter.setmNotes(nlist);

        mNoteList.setAdapter(noteAdapter);

        noteAdapter.notifyDataSetChanged();

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SelectedNoteItem = position;
                HandleNewUpdateNote(false, nlist.get(position), false);
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {
                showPopupMenuOfNote(note);
            }
        });
    }

    /**
     * 长按笔记显示弹出菜单
     * @param note
     */
    private void showPopupMenuOfNote(Note note) {
        // 记录长按项
        LongClickNoteItem = note;

        m_LongClickNotePopupMenu = new Dialog(getContext(), R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(getContext(), m_LongClickNotePopupMenu, R.layout.popupmenu_note_longclicknote);

        root.findViewById(R.id.id_NoteFrag_PopupMenu_ViewNote).setOnClickListener(NoteFragment.this);
        root.findViewById(R.id.id_NoteFrag_PopupMenu_ChangeGroup).setOnClickListener(NoteFragment.this);
        root.findViewById(R.id.id_NoteFrag_PopupMenu_DeleteNote).setOnClickListener(NoteFragment.this);
        root.findViewById(R.id.id_NoteFrag_PopupMenu_Cancel).setOnClickListener(NoteFragment.this);

        m_LongClickNotePopupMenu.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                LongClickNoteItem = null;
            }
        });

        TextView label = root.findViewById(R.id.id_NoteFrag_PopupMenu_Label);
        label.setText(String.format(Locale.CHINA, getString(R.string.NoteFrag_PopupMenuLabel), LongClickNoteItem.getTitle()));

        m_LongClickNotePopupMenu.show();
    }

    // endregion 进入笔记

    // region 笔记增删改 文字识别 活动返回 takePhoto OpenOCRAct DeleteNote HandleNewUpdateNote onActivityResult

    private Uri imgUri; // 拍照时返回的uri

    /**
     * 浮动菜单 OCR
     */
    private void takePhoto() {

        // 要保存的图片文件 _PHOTO 格式
        String filename = SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.PHOTO);

        // 7.0 调用系统相机拍照不再允许使用 Uri 方式，应该替换为 FileProvider
        // provider 路径
        imgUri = AppPathUtil.getUriByPath(getContext(), filename);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 权限
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // 传入新图片名
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);

        try {
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.NoteFrag_CameraNoFoundAlertTitle)
                .setMessage(R.string.NoteFrag_CameraNoFoundAlertMsg)
                .setPositiveButton(R.string.NoteFrag_CameraNoFoundAlertPosButton, null)
                .create().show();
        }

        // TODO nox_adb 没有相机可以测试
    }

    /**
     * 拍完照后文字识别
     * @param imgUri 原图片
     */
    private void OpenOCRAct(Uri imgUri) {
        Intent intent = new Intent(getContext(), OCRActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(OCRActivity.INT_IMGPATH, imgUri.toString());

        intent.putExtra(OCRActivity.INT_BUNDLE, bundle);
        startActivity(intent);
    }

    /**
     * 图片编辑
     */
    private void StartEditImg(Uri uri) {
        try {
            // 获得源路径
            String imgPath = AppPathUtil.getFilePathByUri(getContext(), uri);

            if (imgPath == null || imgPath.isEmpty()) {
                new AlertDialog.Builder(getContext())
                    .setTitle("插入图片")
                    .setMessage("从相册获取的图片或拍照得到的图片不存在，请重试。")
                    .setNegativeButton("确定", null)
                    .create()
                    .show();
                return;
            }

            // Uri uri2 = Uri.fromFile(new File(imgPath));

            Intent intent = new Intent(getActivity(), IMGEditActivity.class);

            // intent.putExtra(IMGEditActivity.INT_IMAGE_URI, uri2);
            intent.putExtra(IMGEditActivity.INT_IMAGE_URI, uri);
            intent.putExtra(IMGEditActivity.INT_IMAGE_SAVE_URI, SaveNameUtil.getImageFileName(SaveNameUtil.SaveType.EDITED));

            startActivityForResult(intent, REQUEST_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除笔记
     *
     * @param view
     * @param note
     */
    private void DeleteNote(final View view, final Note note) {
        AlertDialog deleteAlert = new AlertDialog
                .Builder(getContext())
                .setTitle(R.string.NoteFrag_DeleteAlertTitle)
                .setMessage(String.format(getResources().getString(R.string.NoteFrag_DeleteAlertMsg), note.getTitle()))
                .setPositiveButton(R.string.NoteFrag_DeleteAlertPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        NoteDao noteDao = new NoteDao(getContext());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                int ret = noteDao.deleteNote(note.getId());

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ret > 0) {
                                            NoteList.remove(note);
                                            noteAdapter.notifyDataSetChanged();

                                            // TODO 一堆垃圾代码待改

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (IsSearching)
                                                        noteAdapter.setmNotes(search(SearchingStr));
                                                    else if (IsGrouping)
                                                        noteAdapter.setmNotes(getGroupOfNote(currGroup));
                                                    else
                                                        noteAdapter.setmNotes(NoteList);

                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            noteAdapter.notifyDataSetChanged();

                                                            Snackbar.make(view, R.string.NoteFrag_DeleteAlertDeleteSuccess, Snackbar.LENGTH_LONG)

                                                                    // TODO !!!!!!!!!!!!

                                                                    // .setAction(R.string.NoteFrag_DeleteAlertUndo, new View.OnClickListener() {
                                                                    //     @Override
                                                                    //     public void onClick(View v) {
                                                                    //         try {
//
                                                                    //             long noteId = noteDao.insertNote(note);
                                                                    //             new Thread(new Runnable() {
                                                                    //                 @Override
                                                                    //                 public void run() {
                                                                    //                     Note note1 = noteDao.queryNoteById((int) noteId);
//
                                                                    //                     getActivity().runOnUiThread(new Runnable() {
                                                                    //                         @Override
                                                                    //                         public void run() {
//
                                                                    //                             NoteList.add(note1);
                                                                    //                             Collections.sort(NoteList);
//
                                                                    //                             new Thread(new Runnable() {
                                                                    //                                 @Override
                                                                    //                                 public void run() {
//
                                                                    //                                     if (IsSearching)
                                                                    //                                         noteAdapter.setmNotes(search(SearchingStr));
                                                                    //                                     else if (IsGrouping)
                                                                    //                                         noteAdapter.setmNotes(getGroupOfNote(currGroup));
                                                                    //                                     else
                                                                    //                                         noteAdapter.setmNotes(NoteList);
//
                                                                    //                                     getActivity().runOnUiThread(new Runnable() {
                                                                    //                                         @Override
                                                                    //                                         public void run() {
//
                                                                    //                                             noteAdapter.notifyDataSetChanged();
                                                                    //                                         }
                                                                    //                                     });
                                                                    //                                 }
                                                                    //                             }).start();
                                                                    //                         }
                                                                    //                     });
                                                                    //                 }
                                                                    //             }).start();
                                                                    //         } catch (Exception ex) {
                                                                    //             ex.printStackTrace();
                                                                    //         }
//
                                                                    //         Snackbar.make(v, R.string.NoteFrag_DeleteAlertUndoSuccess, Snackbar.LENGTH_SHORT).show();
                                                                    //     }
                                                                    // })
                                                            .show();
                                                        }
                                                    });
                                                }
                                            }).start();


                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.NoteFrag_DeleteAlertNegativeButton, null)
                .create();

        deleteAlert.show();
    }

    /**
     * 处理从 fab 新建或者从 list 修改，活动转换
     *
     * @param isNew true 表示 新建
     * @param note  false 时有用，传入原先数据
     */
    private void HandleNewUpdateNote(boolean isNew, Note note, boolean noteisnew) {
        if (isNew) {
            hasNoteReturned = false;
            Intent addNote_intent = new Intent(getActivity(), ModifyNoteActivity.class);
            addNote_intent.putExtra("notedata", new Note("", ""));
            addNote_intent.putExtra("flag", NOTE_NEW); // NEW
            startActivityForResult(addNote_intent, REQ_NOTE_NEW); // 2 from FloatingButton
        } else {

            if (!noteisnew)
                hasNoteReturned = false;

            Intent modifyNote_intent = new Intent(getContext(), ViewModifyNoteActivity.class);
            modifyNote_intent.putExtra("notedata", note);
            modifyNote_intent.putExtra("isModify", noteisnew);
            modifyNote_intent.putExtra("flag", NOTE_UPDATE); // UPDATE
            startActivityForResult(modifyNote_intent, REQ_NOTE_UPDATE); // 1 from List
        }

    }

    boolean hasNoteReturned = false; // 标识是否回退过

    /**
     * 处理从 View Modify Note Activity 返回的数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        ShowLogE("onActivityResult", "HasReturn");

        switch (requestCode) {
            case REQ_NOTE_NEW:
            case REQ_NOTE_UPDATE:

                if (resultCode == RESULT_OK) {

                    int flag = data.getIntExtra("flag", NOTE_NEW);
                    Note note = (Note) data.getSerializableExtra("notedata");

                    if (flag == NOTE_NEW) {
                        hasNoteReturned = true;
                        NoteList.add(NoteList.size(), note);
                        SelectedNoteItem = NoteList.indexOf(note);
                        HandleNewUpdateNote(false, NoteList.get(SelectedNoteItem), true);
                    }
                    else {
                        NoteList.set(SelectedNoteItem, note);

                        //////
                        Collections.sort(NoteList);
                        noteAdapter.notifyDataSetChanged();

                        if (IsSearching)
                            initListView(search(SearchingStr));
                        else if (IsGrouping)
                            initListView(getGroupOfNote(currGroup));
                        else
                            initListView(NoteList);

                        if (hasNoteReturned)
                            Toast.makeText(getContext(), String.format(getResources().getString(R.string.NoteFrag_ActivityReturnNewNote), note.getTitle()), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), String.format(getResources().getString(R.string.NoteFrag_ActivityReturnUpdateNote), note.getTitle()), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            // 拍照获得图片，编辑
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    StartEditImg(imgUri);
                }
                break;

            // 裁剪后文字识别
            case REQUEST_CROP: // 裁剪
                if (resultCode == RESULT_OK) {
                    // _PHOTO
                    AppPathUtil.deleteFile(AppPathUtil.getFilePathByUri(getContext(), imgUri));
                    OpenOCRAct(data.getData()); // _small uri
                }
                break;
        }
    }

    // endregion 笔记增删改

    /**
     * 订阅登录注销事件
     */
    private void registerAuthActions() {
        AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {

            // TODO
            public void onLogin(String UserName) {
                if(getUserVisibleHint()) {
                    SearchGroupBack();
                    HasRefreshed = true;
                }
                else
                    HasRefreshed = false;
            }

            @Override
            public void onLogout() {
                if(getUserVisibleHint()) {
                    SearchGroupBack();
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
        if(!HasRefreshed) {
            SearchGroupBack();
            HasRefreshed = true;
        }
    }
}
