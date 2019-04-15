package com.baibuti.biji.Fragment;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Activity.ViewModifyNoteActivity;
import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.Dialog.GroupDialog;
import com.baibuti.biji.R;
import com.baibuti.biji.RainbowPalette;
import com.baibuti.biji.View.SpacesItemDecoration;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mNoteList;

    private List<Note> NoteList;
    private List<Group> GroupList;

    private FloatingActionsMenu m_fabmenu;
    private FloatingActionButton m_fabback;
    private com.wyt.searchbox.SearchFragment searchFragment;
    private SwipeRefreshLayout mSwipeRefresh;
    private SlidingMenu slidingMenu;

    private static final int NOTE_NEW = 0; // new
    private static final int NOTE_UPDATE = 1; // modify

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);
        setHasOptionsMenu(true);

        m_fabmenu = (FloatingActionsMenu) view.findViewById(R.id.note_fabmenu);
        m_fabback = (FloatingActionButton) view.findViewById(R.id.note_searchback);

        slidingMenu = ((MainActivity)getActivity()).getSlidingMenu();
        mNoteList = view.findViewById(R.id.note_list);

        m_fabback.setOnClickListener(this);

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.note_listsrl);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshdata();
            }
        });

        initToolbar(view);
        initFloatingActionBar(view);
        initData(); // GetDao & List
        initAdapter();
        initListView(NoteList);
        initSearchFrag();
        return view;
    }

    private List<Note> search(String str) {
        List<Note> notelist = new ArrayList<>();

        for (Note note : NoteList) {
            if (note.getTitle().contains(str) || note.getContent().contains(str))
                notelist.add(note);
        }

        return notelist;
    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.note_toolbar);
        toolbar.inflateMenu(R.menu.notefragment_actionbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
                        break;
                    case R.id.action_modifygroup:
                        GroupDialog.setupGroupDialog(getContext(), groupAdapter, GroupList, groupDao, noteDao, getLayoutInflater())
                                .showModifyGroup();
                        refreshdata();
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(slidingMenu != null)
                    slidingMenu.showMenu();
            }
        });
        toolbar.setTitle(R.string.note_header);

        //SimplerSearcherView seacherView = view.findViewById(R.id.note_searcher);
        //seacherView.setOnSearcherClickListener(((MainActivity)getActivity()));
    }

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

    private NoteAdapter noteAdapter;
    private GroupAdapter groupAdapter;

    private NoteDao noteDao;
    private GroupDao groupDao;

    public void initData() {
        if (noteDao == null) {
            noteDao = new NoteDao(this.getContext());
            groupDao = new GroupDao(this.getContext());
        }

        NoteList = noteDao.queryNotesAll();
        GroupList = groupDao.queryGroupAll();
    }

    public void initAdapter() {
        noteAdapter = new NoteAdapter();
        groupAdapter = new GroupAdapter(getContext(), GroupList);
    }

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

    public void refreshNoteList() {
        NoteList = noteDao.queryNotesAll();
        Collections.sort(NoteList);
        noteAdapter = new NoteAdapter();
        noteAdapter.setmNotes(NoteList);
        noteAdapter.notifyDataSetChanged();
    }

    public void refreshGroupList() {
        GroupList = groupDao.queryGroupAll();
        groupAdapter = new GroupAdapter(getContext(), GroupList); // 必要
        groupAdapter.notifyDataSetChanged();
    }


    private int SelectedNoteItem;

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

                Intent intent=new Intent(getContext(), ViewModifyNoteActivity.class);
                intent.putExtra("notedata",nlist.get(position));
                intent.putExtra("flag",NOTE_UPDATE); // UPDATE
                startActivityForResult(intent,1); // 1 from List
            }
        });

        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(final View view, final Note note) {
                DeleteNote(view, note);
            }
        });
    }

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

    private void initSearchFrag() {
        // 添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setAllowReturnTransitionOverlap(true);
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                if (keyword.isEmpty()) {

                }
                else {
                    initListView(search(keyword));
                    m_fabmenu.setVisibility(View.GONE);
                    m_fabback.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_searchback:
                NoteList = noteDao.queryNotesAll();
                initListView(NoteList);
                m_fabback.setVisibility(View.GONE);
                m_fabmenu.setVisibility(View.VISIBLE);
        }
    }


    //////////////////////////////////////////////////



    //////////////////////////////////////////////////

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
                    }
                }
                break;
        }
    }

}
