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

    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefresh;
    private ListView NoteListView;
    private ArrayList<Note> NoteList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.id_note_addfab);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        NoteListView = (ListView) view.findViewById(R.id.id_note_notelistview);


        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshdata();
            }
        });

        fab.setOnClickListener(this);

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


        ArrayAdapter<Note> NoteAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, NoteList);
        NoteListView.setAdapter(NoteAdapter);

        NoteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

}
