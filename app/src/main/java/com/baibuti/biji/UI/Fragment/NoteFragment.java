package com.baibuti.biji.UI.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baibuti.biji.UI.Activity.MainActivity;
import com.baibuti.biji.UI.Activity.ModifyNoteActivity;
import com.baibuti.biji.UI.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Adapters.GroupAdapter;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Data.Adapters.NoteAdapter;
import com.baibuti.biji.UI.Dialog.GroupDialog;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.View.SpacesItemDecoration;
import com.baibuti.biji.UI.Widget.RecyclerViewEmptySupport;
import com.baibuti.biji.Data.Db.GroupDao;
import com.baibuti.biji.Data.Db.NoteDao;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.wyt.searchbox.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener, IShowLog {

    // region 声明: View UI ProgressDialog Toolbar

    private View view;

    private RecyclerViewEmptySupport mNoteList;

    private FloatingActionsMenu m_fabmenu;
    private SearchFragment searchFragment;
    private SwipeRefreshLayout mSwipeRefresh;
    private SlidingMenu slidingMenu;

    private ProgressDialog loadingDialog;
    private ProgressDialog loadingGroupDialog;

    private Toolbar m_toolbar;

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

    // region 创建界面 菜单栏 浮动菜单 等待框 搜索框 onCreateView initToolbar initFloatingActionBar setupProgressAndSR initSearchFrag ShowLogE onClick

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_notetab, container, false);

            ///

            setHasOptionsMenu(true);

            m_fabmenu = (FloatingActionsMenu) view.findViewById(R.id.note_fabmenu);
            slidingMenu = ((MainActivity)getActivity()).getSlidingMenu();

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
        }

        return view;
    }

    /**
     * 初始化菜单栏以及标题
     * @param view
     */
    private void initToolbar(View view) {
        m_toolbar = view.findViewById(R.id.note_toolbar);
        m_toolbar.inflateMenu(R.menu.notefragment_actionbar);
        m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
                        break;
                    case R.id.action_modifygroup:
                        ShowGroupDialog();
                        break;
                    case R.id.action_search_back:
                        SearchFracBack();
                        break;
                }
                return true;
            }
        });
        m_toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(slidingMenu != null)
                    slidingMenu.showMenu();
            }
        });
        m_toolbar.setTitle(R.string.note_header);
    }

    /**
     * 初始化浮动按钮菜单
     * @param view
     */
    private void initFloatingActionBar(View view) {
        FloatingActionButton mNotePhoto = view.findViewById(R.id.note_photo);
        FloatingActionButton mNoteEdit = view.findViewById(R.id.note_edit);

        mNotePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_fabmenu.collapse();
                Toast.makeText(getContext(), "This is note_photo",Toast.LENGTH_LONG).show();
                //添加逻辑处理
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
        loadingDialog.setMessage(getResources().getString(R.string.NoteFragment_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);

        loadingGroupDialog = new ProgressDialog(getContext());
        loadingGroupDialog.setMessage(getContext().getString(R.string.NoteFragment_LoadingGroupData));
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

//                    ShowLogE("initSearchFrag", search(keyword).isEmpty()+"");

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
                        mSwipeRefresh.setEnabled(false);
                        m_fabmenu.setVisibility(View.GONE);
                        m_toolbar.setTitle(String.format(getContext().getString(R.string.notefragment_menu_search_content), keyword));
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    /**
     * IShowLog 接口，全局设置 Log 格式
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

    // endregion 创建界面 菜单栏 浮动菜单 等待框 搜索框

    // region 搜索处理 IsSearching IsSearchingNull getIsSearching SearchingStr search SearchFracBack

    /**
     * 用于返回数据时判断当前是否处在搜索页面
     * 而进行下一步处理刷新 ListView
     */
    private boolean IsSearching = false;
    private boolean IsSearchingNull = false;

    /**
     * 用于判断返回键时的事件
     * @return
     */
    public boolean getIsSearching() {
        return this.IsSearching;
    }

    /**
     * 当 IsSearching 时表示当前所有页面的 keyWord
     */
    private String SearchingStr;

    /**
     * 查找笔记功能
     * @param str
     * @return
     */
    private List<Note> search(String str) {
        List<Note> notelist = new ArrayList<>();

        for (Note note : NoteList) {
            if (note.getTitle().contains(str) || note.getContent().contains(str))
                notelist.add(note);
        }
        noteAdapter.notifyDataSetChanged();

        IsSearchingNull = notelist.isEmpty();
        if (IsSearchingNull) {
            Toast.makeText(getContext(), R.string.NoteFragment_SearchNullToast, Toast.LENGTH_SHORT).show();
        }

        return notelist;
    }

    /**
     * 返回原界面，退出搜索
     */
    public void SearchFracBack() {

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NoteList = noteDao.queryNotesAll();
                        initListView(NoteList);

                        mSwipeRefresh.setEnabled(true);
                        m_fabmenu.setVisibility(View.VISIBLE);

                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(false);
                        m_toolbar.setTitle(R.string.note_header);

                        IsSearching = false;
                        SearchingStr = "";
                        IsSearchingNull = false;
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

    // region 显示分组 ShowGroupDialog

    /**
     * 显示 分类 对话框
     */
    public void ShowGroupDialog() {

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
                    }
                });
                dialog.show();
            }
        }, ShowGroupDlgSecond); // 10
    }

    // endregion 显示分组

    // region 初始化笔记列表 进入笔记 SelectedNoteItem initListView

    /**
     * 当前选中的笔记，用于返回时修改列表
     */
    private int SelectedNoteItem;

    /**
     * 初始化 笔记列表 View，并处理点击笔记事件
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

                SelectedNoteItem = position;
                HandleNewUpdateNote(false, nlist.get(position), false);
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {
                DeleteNote(view, note);
            }
        });
    }

    // endregion 进入笔记

    // region 笔记增删改 活动返回 DeleteNote HandleNewUpdateNote onActivityResult

    /**
     * 删除笔记
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

                            Snackbar.make(view , R.string.NoteFrag_DeleteAlertDeleteSuccess, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.NoteFrag_DeleteAlertUndo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                noteDao.insertNote(note);
                                                NoteList.add(note);
                                                Collections.sort(NoteList);
                                                noteAdapter.notifyDataSetChanged();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            Snackbar.make(view, R.string.NoteFrag_DeleteAlertUndoSuccess, Snackbar.LENGTH_SHORT).show();
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
     * @param isNew true 表示 新建
     * @param note false 时有用，传入原先数据
     */
    private void HandleNewUpdateNote(boolean isNew, Note note, boolean noteisnew) {
        if (isNew) {
            hasNoteReturned = false;
            Intent addNote_intent=new Intent(getActivity(), ModifyNoteActivity.class);
            addNote_intent.putExtra("notedata",new Note("",""));
            addNote_intent.putExtra("flag",NOTE_NEW); // NEW
            startActivityForResult(addNote_intent,REQ_NOTE_NEW); // 2 from FloatingButton
        }
        else {

            if (!noteisnew)
                hasNoteReturned = false;

            Intent modifyNote_intent=new Intent(getContext(), ViewModifyNoteActivity.class);
            modifyNote_intent.putExtra("notedata", note);
            modifyNote_intent.putExtra("isModify", noteisnew);
            modifyNote_intent.putExtra("flag",NOTE_UPDATE); // UPDATE
            startActivityForResult(modifyNote_intent,REQ_NOTE_UPDATE); // 1 from List
        }

    }

    boolean hasNoteReturned = false; // 标识是否回退过

    /**
     * 处理从 View Modify Note Activity 返回的数据
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
                        else
                            initListView(NoteList);

                        if (hasNoteReturned)
                            Toast.makeText(getContext(), String.format(getResources().getString(R.string.NoteFrag_ActivityReturnNewNote), note.getTitle()), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), String.format(getResources().getString(R.string.NoteFrag_ActivityReturnUpdateNote), note.getTitle()), Toast.LENGTH_SHORT).show();

                    }


                }
                break;
        }
    }

    // endregion 笔记增删改

}


