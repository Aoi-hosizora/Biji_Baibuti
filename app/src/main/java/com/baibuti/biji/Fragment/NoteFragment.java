package com.baibuti.biji.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.Dialog.GroupDialog;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.View.SpacesItemDecoration;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener, IShowLog {

    private RecyclerView mNoteList;

    private FloatingActionsMenu m_fabmenu;
    private com.wyt.searchbox.SearchFragment searchFragment;
    private SwipeRefreshLayout mSwipeRefresh;
    private SlidingMenu slidingMenu;

    private ProgressDialog loadingDialog;
    private Toolbar m_toolbar;

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);
        setHasOptionsMenu(true);

        m_fabmenu = (FloatingActionsMenu) view.findViewById(R.id.note_fabmenu);

        slidingMenu = ((MainActivity)getActivity()).getSlidingMenu();
        mNoteList = view.findViewById(R.id.note_list);

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.note_listsrl);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshdata();
            }
        });

        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setMessage(getResources().getString(R.string.NoteFragment_LoadingData));
        loadingDialog.setCanceledOnTouchOutside(false);

        initToolbar(view);
        initFloatingActionBar(view);
        initData(); // GetDao & List
        initAdapter();
        initListView(NoteList);
        initSearchFrag();
        return view;
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

    /**
     * 初始化菜单栏
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
                        GroupDialog.setupGroupDialog(getContext(), groupAdapter, GroupList, groupDao, noteDao, getLayoutInflater())
                                .showModifyGroup();
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
                Intent addDoc_intent=new Intent(getActivity(),ModifyNoteActivity.class);
                addDoc_intent.putExtra("notedata",new Note("",""));
                addDoc_intent.putExtra("flag",NOTE_NEW); // NEW
                startActivityForResult(addDoc_intent,2); // 2 from FloatingButton
            }
        });

//        m_fabmenu.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Log.e("0", "onFocusChange: "+hasFocus );
//                if (!hasFocus)
//                    m_fabmenu.collapse();
//            }
//        });

    }

    /**
     * 用于返回数据时判断当前是否处在搜索页面
     * 而进行下一步处理刷新 ListView
     */
    private boolean IsSearching = false;

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
        if (notelist.isEmpty())
            Toast.makeText(getContext(), "搜索不到任何数据。", Toast.LENGTH_SHORT).show();
        return notelist;
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
                if (!keyword.isEmpty()) {
                    IsSearching = true;
                    SearchingStr = keyword;
                    initListView(search(keyword));
                    m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(true);
                    mSwipeRefresh.setEnabled(false);
                    m_toolbar.setTitle(String.format(getContext().getString(R.string.notefragment_menu_search_content), keyword));
                }
            }
        });
    }

    /**
     * 返回原界面，退出搜索
     */
    private void SearchFracBack() {

        loadingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
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
                        m_toolbar.getMenu().findItem(R.id.action_search_back).setVisible(false);
                        m_toolbar.setTitle(R.string.note_header);

                        IsSearching = false;
                        SearchingStr = "";

                        loadingDialog.dismiss();

                    }
                });
            }
        }).start();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

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
     * 刷新数据，用于下拉
     */
    private void refreshdata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                        initAdapter();
                        refreshNoteList();
                        refreshGroupList();
                        initListView(NoteList);

                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    /**
     * 刷新 笔记列表
     */
    public void refreshNoteList() {
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
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }


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
        noteAdapter.notifyDataSetChanged();

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                SelectedNoteItem = position;
                ClickNoteItem(nlist, position);
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {
                DeleteNote(view, note);
            }
        });
    }

    /**
     * 点击 NoteList 的一项，打开 ViewModifyNote 活动
     * @param nlist List<Note>
     * @param position
     */
    public void ClickNoteItem(final List<Note> nlist, int position) {
        Intent intent=new Intent(getContext(), ViewModifyNoteActivity.class);
        intent.putExtra("notedata",nlist.get(position));
        intent.putExtra("flag",NOTE_UPDATE); // UPDATE
        startActivityForResult(intent,1); // 1 from List
    }

    /**
     * 删除笔记
     * @param view
     * @param note
     */
    private void DeleteNote(final View view, final Note note) {
        AlertDialog deleteAlert = new AlertDialog
                .Builder(getContext())
                .setTitle(R.string.DeleteAlert_Title)
                .setMessage(String.format(getResources().getString(R.string.DeleteAlert_Msg), note.getTitle()))
                .setPositiveButton(R.string.DeleteAlert_PositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int ret = noteDao.deleteNote(note.getId());

                        if (ret > 0) {
                            NoteList.remove(note);
                            noteAdapter.notifyDataSetChanged();

                            Snackbar.make(view , R.string.DeleteAlert_DeleteSuccess, Snackbar.LENGTH_LONG)
                                .setAction(R.string.DeleteAlert_Undo, new View.OnClickListener() {
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
                                        Snackbar.make(view, R.string.DeleteAlert_UndoSuccess, Snackbar.LENGTH_SHORT).show();
                                    }
                                }).show();
                        }
                    }
                })
                .setNegativeButton(R.string.DeleteAlert_NegativeButton, null)
                .create();

        deleteAlert.show();
    }

    //////////////////////////////////////////////////

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
            case 1: // FROM LIST
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        NoteList.set(SelectedNoteItem, newnote);
                        Collections.sort(NoteList);
                        noteAdapter.notifyDataSetChanged();

                        if (IsSearching)
                            initListView(search(SearchingStr));
                    }
                    break;
                }
            case 2: // ADD
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        Toast.makeText(getActivity(), newnote.getTitle(), Toast.LENGTH_SHORT).show();
                        NoteList.add(NoteList.size(), newnote);
                        Collections.sort(NoteList);
                        noteAdapter.notifyDataSetChanged();

                        if (IsSearching)
                            initListView(search(SearchingStr));
                    }
                }
                break;
        }
    }

}


