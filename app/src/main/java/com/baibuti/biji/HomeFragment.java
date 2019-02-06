package com.baibuti.biji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private ArrayList<String> AlarmAList;
    private ArrayList<String> NoteAList;

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

        initHomeDatas();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_home_newalarm:
                Toast.makeText(getActivity(), "ABC", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_home_newnote:
                Toast.makeText(getActivity(), "ABC", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void initHomeDatas() {
        mainData = Data.getData();
        AlarmAList = mainData.getAlarm();
        NoteAList = mainData.getNote();

        Log.d("TAG", "initHomeDatas: "+AlarmAList.get(0));
        Log.d("TAG", "initHomeDatas: "+AlarmAList.get(1));
        Log.d("TAG", "initHomeDatas: "+NoteAList.get(0));

        String[] data = {"a","b","c"};
        ArrayAdapter<String> AlarmAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,AlarmAList);
        mHomeAlarmList.setAdapter(AlarmAdapter);

        ArrayAdapter<String> NoteAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,NoteAList);
        mHomeNoteList.setAdapter(NoteAdapter);
    }
}
