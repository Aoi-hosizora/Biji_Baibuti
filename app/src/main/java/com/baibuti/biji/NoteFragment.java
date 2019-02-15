package com.baibuti.biji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;

public class NoteFragment extends Fragment implements View.OnClickListener {

    private Data mainData;

    private FloatingActionButton mFab;
    private SwipeRefreshLayout mSwipeRefresh;
    private ListView mNoteListView;
    private ArrayList<Note> NoteList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);

        mFab = (FloatingActionButton) view.findViewById(R.id.id_note_addfab);
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mNoteListView = (ListView) view.findViewById(R.id.id_note_notelistview);


        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshdata();
            }
        });

        mFab.setOnClickListener(this);

        initData();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_note_addfab:
                Toast.makeText(getActivity(), "A", Toast.LENGTH_SHORT).show();
            break;
        }
    }

    private void initData() {
        mainData = Data.getData();

        NoteList = mainData.getNote();


        NoteAdapter noteAdapter = new NoteAdapter(getActivity(), R.layout.notelistview, NoteList);
        mNoteListView.setAdapter(noteAdapter);

        mNoteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = NoteList.get(position);
                Toast.makeText(getActivity(), note.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshdata() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

}
