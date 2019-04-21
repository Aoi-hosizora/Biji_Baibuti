package com.baibuti.biji.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
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
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (null != view){
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        }else {
            view = inflater.inflate(R.layout.fragment_filetab, container, false);
            /**
             * 控件的初始化
             */
            initToolBar(view);
            initFileCLassList(view);
        }

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
        fileClassList.setVerticalScrollBarEnabled(false);
        fileClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //获取分类下的文件


                //点击添加新类别
                if(fileClassList.getCount() == position + 1){
                    addNewFileClass(adapterView, position);
                }
            }
        });
    }

    private void initFileCLass(){
        FileClass fileClass_pdf = new FileClass("pdf");
        fileClassListItems.add(fileClass_pdf);
        FileClass fileClass_ppt = new FileClass("ppt");
        fileClassListItems.add(fileClass_ppt);
        FileClass fileClass_doc = new FileClass("doc");
        fileClassListItems.add(fileClass_doc);
        FileClass fileclass_add = new FileClass("+");
        fileClassListItems.add(fileclass_add);
    }

    private void addNewFileClass(final AdapterView<?> adapterView, final int position){
        Toast.makeText(getContext(),"Add new fileclass", Toast.LENGTH_SHORT).show();
        final EditText edit = new EditText(getContext());

        AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
        editDialog.setTitle(getString(R.string.FileclassDialog_AddNewclass));
        editDialog.setIcon(R.mipmap.ic_launcher_round);

        //设置dialog布局
        editDialog.setView(edit);

        //设置按钮
        editDialog.setPositiveButton(getString(R.string.FileclassDialog_Confirmbtn)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(),
                                edit.getText().toString().trim(),Toast.LENGTH_SHORT).show();

                        //在分类列表中添加新的类别
                        FileClassAdapter adapter = (FileClassAdapter) adapterView.getAdapter();
                        FileClass item=(FileClass) adapter.getItem(position);
                        item.setFileClassName(edit.getText().toString().trim());
                        FileClass fileClass_new = new FileClass("+");
                        fileClassListItems.add(fileClass_new);
                        adapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });

        editDialog.create().show();
    }
}