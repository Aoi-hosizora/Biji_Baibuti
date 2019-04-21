package com.baibuti.biji.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Data.FileClass;
import com.baibuti.biji.Data.FileClassAdapter;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.View.SpacesItemDecoration;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileFragment extends Fragment {


    private List<FileClass> fileClassListItems  = new ArrayList<>();
    private ListView fileClassList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filetab, container, false);

        initToolBar(view);
        initFileCLassList(view);

        return view;
    }

    private void initToolBar(View view){
        Toolbar mToolBar = view.findViewById(R.id.tab_file_toolbar);
        mToolBar.setTitle(R.string.file_header);
        mToolBar.setNavigationIcon(R.drawable.tab_file_menu);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"This is a menu",Toast.LENGTH_LONG).show();
                //添加菜单逻辑
            }
        });
    }

    private void initFileCLassList(View view){
        //init fileclasslistitem
        initFileCLass();
        FileClassAdapter fileClassAdapter = new FileClassAdapter(getContext(), fileClassListItems);
        fileClassList = (ListView) view.findViewById(R.id.filefragment_fileclasses);
        fileClassList.setAdapter(fileClassAdapter);
        fileClassList.setSelector(R.drawable.filefrag_fileclass_selector);
        fileClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getContext(), "Clicked on item " + position, Toast.LENGTH_SHORT ).show();
            }
        });
    }

    private void initFileCLass(){
        for(int i=0;i<1;i++){
            FileClass fileClass_pdf = new FileClass("pdf");
            fileClassListItems.add(fileClass_pdf);
            FileClass fileClass_ppt = new FileClass("ppt");
            fileClassListItems.add(fileClass_ppt);
            FileClass fileClass_doc = new FileClass("doc");
            fileClassListItems.add(fileClass_doc);
        }
    }
}