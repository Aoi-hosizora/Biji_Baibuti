package com.baibuti.biji;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener {

    // private com.baibuti.biji.HorizontalListView mHorizontalList;
    private ListView mHomeAlarmList;
    private ListView mHomeNoteList;

    private Button mHomeNewAlarm;
    private Button mHomeNewNote;

    private Data mainData;
    private ArrayList<Alarm> AlarmAList;
    private ArrayList<Note> NoteAList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hometab, container, false);

        mHomeAlarmList = (ListView) view.findViewById(R.id.id_home_alarmlist);
        mHomeNoteList = (ListView) view.findViewById(R.id.id_home_notelist);
        mHomeNewAlarm = (Button) view.findViewById(R.id.id_home_newalarm);
        mHomeNewNote = (Button) view.findViewById(R.id.id_home_newnote);

        mHomeNewAlarm.setOnClickListener(this);
        mHomeNewNote.setOnClickListener(this);

        initDatas();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_home_newalarm:
                final EditText editText = new EditText(getActivity());
                AlertDialog.Builder inputdialog = new AlertDialog.Builder(getActivity());
                inputdialog.setTitle("New Alarm...").setView(editText);
                inputdialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(),editText.getText().toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                break;
            case R.id.id_home_newnote:
                Toast.makeText(getActivity(), "ABC", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void initDatas() {
        mainData = Data.getData();
        AlarmAList = mainData.getAlarm();
        NoteAList = mainData.getNote();

        AlarmAdapter alarmAdapter = new AlarmAdapter(getActivity(),R.layout.alarmlistview, AlarmAList);
        mHomeAlarmList.setAdapter(alarmAdapter);

        NoteAdapter noteAdapter = new NoteAdapter(getActivity(), R.layout.notelistview, NoteAList);
        mHomeNoteList.setAdapter(noteAdapter);

        mHomeAlarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Alarm alarm = AlarmAList.get(position);
                Toast.makeText(getActivity(), alarm.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        mHomeNoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = NoteAList.get(position);
                Toast.makeText(getActivity(), note.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
