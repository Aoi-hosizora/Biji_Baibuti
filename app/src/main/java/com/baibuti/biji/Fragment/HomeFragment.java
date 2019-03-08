package com.baibuti.biji.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Activity.ModifyNoteActivity;
import com.baibuti.biji.Data.Alarm;
import com.baibuti.biji.Data.AlarmAdapter;
import com.baibuti.biji.Data.Data;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements View.OnClickListener {

    // private com.baibuti.biji.HorizontalListView mHorizontalList;
    private ListView mHomeAlarmList;
    private ListView mHomeNoteList;

    private Button mHomeNewAlarm;
    private Button mHomeNewNote;

    private Data mainData;
    private ArrayList<Alarm> AlarmList;
    private ArrayList<Note> NoteList;

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

    private int NoteListClickPos;
    private NoteAdapter noteAdapter;
    private AlarmAdapter alarmAdapter;

    private void initDatas() {
        mainData = Data.getData();
        AlarmList = mainData.getAlarm();
        NoteList = mainData.getNote();

        alarmAdapter = new AlarmAdapter(getActivity(),R.layout.alarmlistview, AlarmList);
        mHomeAlarmList.setAdapter(alarmAdapter);

        noteAdapter = new NoteAdapter(getActivity(), R.layout.notelistview, NoteList);
        mHomeNoteList.setAdapter(noteAdapter);

        mHomeAlarmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Alarm alarm = AlarmList.get(position);
                Toast.makeText(getActivity(), alarm.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        mHomeNoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = NoteList.get(position);
                NoteListClickPos = position;

                Intent intent=new Intent(getActivity(),ModifyNoteActivity.class);
                intent.putExtra("notedata",note);
                startActivityForResult(intent,1);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("intent_result", true) == true) {
                        Note newnote = (Note) data.getSerializableExtra("modify_note");
                        NoteList.set(NoteListClickPos,newnote);

                        mainData.setNoteItem(NoteListClickPos, newnote);

                        noteAdapter.notifyDataSetChanged();
                    }
                }
        }
    }
}
