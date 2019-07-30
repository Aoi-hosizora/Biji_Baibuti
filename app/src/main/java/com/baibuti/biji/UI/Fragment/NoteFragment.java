package com.baibuti.biji.UI.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Data.Adapters.GroupRadioAdapter;
import com.baibuti.biji.UI.Activity.MainActivity;
import com.baibuti.biji.UI.Activity.ModifyNoteActivity;
import com.baibuti.biji.UI.Activity.OCRActivity;
import com.baibuti.biji.UI.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Adapters.GroupAdapter;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Data.Adapters.NoteAdapter;
import com.baibuti.biji.UI.Dialog.GroupDialog;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Widget.ListView.SpacesItemDecoration;
import com.baibuti.biji.UI.Widget.ListView.RecyclerViewEmptySupport;
import com.baibuti.biji.Data.DB.GroupDao;
import com.baibuti.biji.Data.DB.NoteDao;
import com.baibuti.biji.Utils.FileDirUtils.FilePathUtil;
import com.baibuti.biji.Utils.FileDirUtils.SDCardUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wyt.searchbox.SearchFragment;
import com.baibuti.biji.Utils.StrSrchUtils.SearchUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * 待整理 !!!!!!
 */
public class NoteFragment extends Fragment implements View.OnClickListener, IShowLog {

    // region 声明: View UI ProgressDialog Toolbar DrawerLayout

    private View view;

    private RecyclerViewEmptySupport mNoteList;

    private FloatingActionsMenu m_fabmenu;
    private SearchFragment searchFragment;
    private SwipeRefreshLayout mSwipeRefresh;
    // private SlidingMenu slidingMenu;

    private ProgressDialog loadingDialog;
    private ProgressDialog loadingGroupDialog;

    private Toolbar m_toolbar;

    private DrawerLayout m_drawerLayout;
    private NavigationView m_navigationView;
    private ListView m_nav_groupList;

    // endregion 声明: View UI ProgressDialog Toolbar

    // region 声明: flag REQ

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    private static final int REQ_NOTE_NEW = 2; // 从 MNote 返回
    private static final int REQ_NOTE_UPDATE = 1; // 从 VMNote 返回

    // endregion 声明: flag REQ

    // region 声明: 一些等待的秒数

    private final static int SearchReturnSecond = 200;
    private final static int SwipeRefreshSecond = 100;
    private final static int ShowGroupDlgSecond = 10;
    private final static int HideGroupPrgSecond = 100;

    // endregion 声明: 一些等待的秒数

    // region 创建界面 菜单栏 浮动菜单 等待框 搜索框 右划菜单 onCreateView initToolbar initFloatingActionBar setupProgressAndSR initSearchFrag ShowLogE onClick initRightMenu closeDrawer

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        } else {
            view = inflater.inflate(R.layout.fragment_notetab, container, false);

            ///
           // slidingMenu = ((MainActivity) getActivity()).getSlidingMenu();
            m_fabmenu = (FloatingActionsMenu) view.findViewById(R.id.note_fabmenu);

            mNoteList = view.findViewById(R.id.note_list);
            View ListEmptyView = view.findViewById(R.id.note_emptylist);
            mNoteList.setEmptyView(ListEmptyView);

            setupProgressAndSR();

            initToolbar(view);
            initFloatingActionBar(view);
            initData(); // GetDao & List
            initAdapter();
            initListView(NoteList);
            initSearchFrag();
            initRightMenu();
        }

        return view;
    }

    /**
     * 初始化菜单栏以及标题
     *
     * @param view
     */
    private void initToolbar(View view) {

        m_toolbar = view.findViewById(R.id.NoteFrag_Toolbar);
        m_toolbar.inflateMenu(R.menu.notefragment_actionbar);
        m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        if (m_fabmenu.isExpanded())
                            m_fabmenu.collapse();
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
    private void initFloatingActionBar(View view) {
        FloatingActionButton mNotePhoto = view.findViewById(R.id.note_photo);
        FloatingActionButton mNoteEdit = view.findViewById(R.id.note_edit);

        mNotePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_fabmenu.collapse();
                NotePhoto_Click();
            }
        });
        mNoteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_fabmenu.collapse();
                HandleNewUpdateNote(true, null, false);
            }
        });
    }

    /**
     * 设置 SwipeRefresh ProgressDialog
     */
    private void setupProgressAndSR() {
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.note_listsrl);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        // mSwipeRefresh.setColorSchemeColors(Color.RED,Color.BLUE,Color.GREEN);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshdata(500);
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

                if (m_fabmenu.isExpanded())
                    m_fabmenu.collapse();

                try {
                    if (!keyword.isEmpty()) {

                        IsSearching = true;
                        SearchingStr = keyword;

                        initListView(search(keyword));

//                    ShowLogE("initSearchFrag", search(keyword).isEmpty()+"");

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
                        mSwipeRefresh.setEnabled(false);
                        m_fabmenu.setVisibility(View.GONE);
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
        m_navigationView = view.findViewById(R.id.id_noteFrag_Right_nav);

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
                ShowGroupDialog();
            }
        });

    }


    /**
     * 关闭侧边栏
     */
    private void closeDrawer() {
        m_drawerLayout.closeDrawer(Gravity.END);
    }

    /**
     * IShowLog 接口，全局设置 Log 格式
     *
     * @param FunctionName
     * @param Msg
     */
    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "NoteFragment";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg); // MainActivity: initDatas###data=xxx
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

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
        List<Note> notelist = new ArrayList<>();

//        for (Note note : NoteList) {
//            if (note.getTitle().contains(str) || note.getContent().contains(str))
//                notelist.add(note);
//        }
//        noteAdapter.notifyDataSetChanged();

        notelist = SearchUtil.getSearchItems(NoteList.toArray(new Note[0]), str);
        noteAdapter.notifyDataSetChanged();

        IsSearchingNull = notelist.isEmpty();

        if (IsSearchingNull) {
            Toast.makeText(getContext(), R.string.NoteFrag_SearchNullToast, Toast.LENGTH_SHORT).show();
        }

        return notelist;
    }

    /**
     * 返回原界面，退出搜索或分类
     */
    public void SearchGroupBack() {
        loadingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(SearchReturnSecond); // 200
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NoteList = noteDao.queryNotesAll();
                        initListView(NoteList);

                        mSwipeRefresh.setEnabled(true);
                        m_fabmenu.setVisibility(View.VISIBLE);

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(false);
                        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(true);
                        m_toolbar.setTitle(R.string.NoteFrag_Header);

                        IsSearching = false;
                        SearchingStr = "";
                        IsSearchingNull = false;

                        IsGrouping = false;
                        currGroup = null;
                        currGroupIdx = -1;
                        IsGroupingNull = false;

                        loadingDialog.dismiss();

                    }
                });
            }
        }).start();

    }

    // endregion 搜索处理

    // region 列表数据 初始化各种数据和适配器 下拉刷新 initData initAdapter refreshNoteList refreshGroupList refreshdata refreshAll

    private List<Note> NoteList;
    private List<Group> GroupList;

    private NoteAdapter noteAdapter;
    private GroupAdapter groupAdapter;

    private NoteDao noteDao;
    private GroupDao groupDao;

    /**
     * 初始化 Dao 和 List 数据
     */
    public void initData() {
        if (noteDao == null)
            noteDao = new NoteDao(this.getContext());

        if (groupDao == null)
            groupDao = new GroupDao(this.getContext());

        NoteList = noteDao.queryNotesAll();
        GroupList = groupDao.queryGroupAll();
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
    public void refreshNoteList() {
        noteDao = new NoteDao(getContext());
        NoteList = noteDao.queryNotesAll();
        Collections.sort(NoteList);
        noteAdapter = new NoteAdapter();
        noteAdapter.setmNotes(NoteList);
        noteAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新 分组列表
     */
    public void refreshGroupList() {
        groupDao = new GroupDao(getContext());
        GroupList = groupDao.queryGroupAll();
        Collections.sort(GroupList);
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新数据，用于 下拉 和 返回刷新
     *
     * @param ms 毫秒
     */
    private void refreshdata(int ms) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshAll();
                initListView(NoteList);

                mSwipeRefresh.setRefreshing(false);
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
                GroupDialog dialog = new GroupDialog(getContext(), new GroupDialog.OnUpdateGroupListener() {

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
                        refreshdata(SwipeRefreshSecond); // 100

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
                dialog.show();
            }
        }, ShowGroupDlgSecond); // 10
    }

    /**
     * 菜单点击，显示左滑菜单
     */
    public void ModifyGroupMenuClick() {

        if (m_fabmenu.isExpanded())
            m_fabmenu.collapse();

        // 列表

        List<Group> groups = groupDao.queryGroupAll();
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
     * 按分组显示
     * @param group
     */
    private void showAsGroup(Group group, int idx) {
        currGroup = group;
        currGroupIdx = idx;
        List<Note> groupNotes = getGroupOfNote(group);

        IsGrouping = true;
        IsGroupingNull = groupNotes.isEmpty();

        if (IsGroupingNull) {
            Toast.makeText(getContext(), R.string.NoteFrag_GroupNullToast, Toast.LENGTH_SHORT).show();
        }

        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
        m_toolbar.getMenu().findItem(R.id.action_search).setVisible(false);
        mSwipeRefresh.setEnabled(false);

        initListView(groupNotes);
        closeDrawer();

        m_toolbar.setTitle(String.format(Locale.CHINA,
            getString(R.string.NoteFrag_GroupingTitle), group.getName()));
    }

    /**
     * 显示全部分组
     */
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
    private List<Note> getGroupOfNote(Group group) {
        List<Note> ret = new ArrayList<>();
        List<Note> allNotes = noteDao.queryNotesAll();
        for (Note note : allNotes)
            if (note.getGroupLabel().getName().equals(group.getName()))
                ret.add(note);
        return ret;
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

//        mNoteList.setEmptyView(ListEmptyView);

        noteAdapter.notifyDataSetChanged();

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (m_fabmenu.isExpanded()) // 先关闭弹出菜单
                    m_fabmenu.collapse();
                else {
                    SelectedNoteItem = position;
                    HandleNewUpdateNote(false, nlist.get(position), false);
                }
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {
                if (m_fabmenu.isExpanded())
                    m_fabmenu.collapse();

                DeleteNote(view, note);
            }
        });
    }

    // endregion 进入笔记

    // region 笔记增删改 文字识别 活动返回 NotePhoto_Click OpenOCRAct DeleteNote HandleNewUpdateNote onActivityResult

    private Uri imgUri; // 拍照时返回的uri
    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照

    /**
     * 浮动菜单 OCR
     */
    private void NotePhoto_Click() {
        // Photo 类型
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date());
        String fileName = time + "_Photo";

        // /Biji/NoteImage/
        String path = SDCardUtil.getPictureDir(); // 保存路径
        File file = new File(path);

        // 要保存的图片文件
        File imgFile = new File(file + File.separator + fileName + ".jpg");

        // 将file转换成uri，返回 provider 路径
        imgUri = FilePathUtil.getUriForFile(getContext(), imgFile);

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

        // TODO nox 模拟器没有相机可以测试
    }

    /**
     * 拍完照后文字识别
     */
    private void OpenOCRAct(Uri imgPath) {
        Intent intent = new Intent(getContext(), OCRActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(OCRActivity.INT_IMGPATH, imgPath.getPath());

        intent.putExtra(OCRActivity.INT_BUNDLE, bundle);
        startActivity(intent);
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

                        int ret = noteDao.deleteNote(note.getId());

                        if (ret > 0) {
                            NoteList.remove(note);
                            noteAdapter.notifyDataSetChanged();

                            // TODO 一堆垃圾代码待改

                            if (IsSearching)
                                noteAdapter.setmNotes(search(SearchingStr));
                            else if (IsGrouping)
                                noteAdapter.setmNotes(getGroupOfNote(currGroup));
                            else
                                noteAdapter.setmNotes(NoteList);
                            noteAdapter.notifyDataSetChanged();

                            Snackbar.make(view, R.string.NoteFrag_DeleteAlertDeleteSuccess, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.NoteFrag_DeleteAlertUndo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {

                                                long noteId = noteDao.insertNote(note);
                                                NoteList.add(noteDao.queryNoteById((int) noteId));
                                                Collections.sort(NoteList);

                                                if (IsSearching)
                                                    noteAdapter.setmNotes(search(SearchingStr));
                                                else if (IsGrouping)
                                                    noteAdapter.setmNotes(getGroupOfNote(currGroup));
                                                else
                                                    noteAdapter.setmNotes(NoteList);
                                                noteAdapter.notifyDataSetChanged();
                                            }
                                            catch (Exception ex) {
                                                ex.printStackTrace();
                                            }

                                            Snackbar.make(v, R.string.NoteFrag_DeleteAlertUndoSuccess, Snackbar.LENGTH_SHORT).show();
                                        }
                                    }).show();

                        }
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

        switch (requestCode) {
            case REQ_NOTE_NEW:
            case REQ_NOTE_UPDATE:

                ShowLogE("onActivityResult", "HasReturn");
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
                    OpenOCRAct(imgUri);
                }
                break;
        }
    }

    // endregion 笔记增删改

}


