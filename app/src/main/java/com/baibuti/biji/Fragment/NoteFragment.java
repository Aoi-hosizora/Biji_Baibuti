package com.baibuti.biji.Fragment;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ActionMenuView;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Data.Data;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.View.SimplerSearcherView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener {

    private ListView mNoteList;

    private Data mainData;
    private ArrayList<Note> NoteList;
    private com.wyt.searchbox.SearchFragment searchFragment;
//    private SwipeRefreshLayout mSwipeRefresh;
    private SlidingMenu slidingMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);
        setHasOptionsMenu(true);
        slidingMenu = ((MainActivity)getActivity()).getSlidingMenu();
        mNoteList = view.findViewById(R.id.note_list);
        //添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setAllowReturnTransitionOverlap(true);
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                Toast.makeText(getContext(),"This is note_search", Toast.LENGTH_SHORT).show();
                //添加逻辑处理
            }
        });

//        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                refreshdata();
//            }
//        });

        initToolbar(view);
        initFloatingActionBar(view);
        initDatas();
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    private NoteAdapter noteAdapter;

    private void initToolbar(View view){
        Toolbar toolbar = view.findViewById(R.id.note_toolbar);
        toolbar.inflateMenu(R.menu.notefragment_actionbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
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

    private void initFloatingActionBar(View view){
        FloatingActionButton mNotePhoto = view.findViewById(R.id.note_photo);
        FloatingActionButton mNoteEdit = view.findViewById(R.id.note_edit);
        mNotePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "This is note_photo",Toast.LENGTH_LONG).show();
                //添加逻辑处理
            }
        });
        mNoteEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addDoc_intent=new Intent(getActivity(),ModifyNoteActivity.class);
                addDoc_intent.putExtra("notedata",new Note("",""));
                addDoc_intent.putExtra("flag",0); // NEW
                startActivityForResult(addDoc_intent,2); // 2 from FloatingButton

            }
        });
    }

    private void initDatas() {
        mainData = Data.getData();
        NoteList = mainData.getNote();
        noteAdapter = new NoteAdapter(getActivity(), R.layout.notelistview, NoteList,this);
        mNoteList.setAdapter(noteAdapter);
    }


//    private void refreshdata() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        initDatas();
//                        noteAdapter.notifyDataSetChanged();
//                        mSwipeRefresh.setRefreshing(false);
//                    }
//                });
//            }
//        }).start();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: // MODIFY
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        int NoteListClickPos = data.getIntExtra("modify_note_pos",0);
                        NoteList.set(NoteListClickPos,newnote);
                        mainData.setNoteItem(NoteListClickPos, newnote);
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
                        noteAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }
}
