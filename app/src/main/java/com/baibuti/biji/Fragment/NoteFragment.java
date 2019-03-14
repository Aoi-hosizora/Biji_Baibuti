package com.baibuti.biji.Fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Data.Data;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class NoteFragment extends Fragment implements View.OnClickListener {

    private ListView mNoteList;

    private Data mainData;
    private ArrayList<Note> NoteList;
    private com.wyt.searchbox.SearchFragment searchFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notetab, container, false);
        setHasOptionsMenu(true);
        mNoteList = (ListView) view.findViewById(R.id.note_list);
        //添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                //这里处理逻辑
                Toast.makeText(getContext(),"HAHAHA", Toast.LENGTH_SHORT).show();
            }
        });
        //initActionbar();
        initDatas();
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    private int NoteListClickPos;
    private NoteAdapter noteAdapter;

    private void initActionbar(){
        /*android.support.v7.app.ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar == null) {
            Toast.makeText(getContext(),"No ActionBar",Toast.LENGTH_LONG).show();
            return;
        }
        actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new ImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.search);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT
                | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = 80;
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
            }
        });*/

    }

    private void initDatas() {
        mainData = Data.getData();
        NoteList = mainData.getNote();
        noteAdapter = new NoteAdapter(getActivity(), R.layout.notelistview, NoteList,this);
        mNoteList.setAdapter(noteAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.notefragment_actionbar,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                searchFragment.show(getActivity().getSupportFragmentManager(),com.wyt.searchbox.SearchFragment.TAG);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true)) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        NoteListClickPos = data.getIntExtra("modify_note_pos",0);

                        NoteList.set(NoteListClickPos,newnote);

                        mainData.setNoteItem(NoteListClickPos, newnote);

                        noteAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }
}
