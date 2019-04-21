package com.baibuti.biji.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.baibuti.biji.Data.Group;
import com.baibuti.biji.Data.GroupAdapter;
import com.baibuti.biji.Data.GroupRadioAdapter;
import com.baibuti.biji.Data.Note;
import com.baibuti.biji.Data.NoteAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.View.SpacesItemDecoration;
import com.baibuti.biji.db.FileClassDao;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;
import com.baibuti.biji.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileFragment extends Fragment {


    private List<FileClass> fileClassListItems  = new ArrayList<>();
    private ListView fileClassList;
    private FileClassDao fileClassDao;
    private FileClassAdapter fileClassAdapter;
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
            initFileClassList(view);

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

    private void initFileClassList(View view){
        //init fileclasslistitem
        initData();
        fileClassAdapter = new FileClassAdapter(getContext(), fileClassListItems);
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
                    addNewFileClass();
                }
            }
        });
        if(!fileClassListItems.get(fileClassListItems.size() - 1).getFileClassName().equals("+")) {
            Log.d("FFFFF", "调用前"+fileClassListItems.get(fileClassListItems.size() - 1).getFileClassName());
            FileClass temp = new FileClass("+", 0);
            fileClassListItems.add(fileClassAdapter.getCount(), temp);
            //Log.d("FILECLASSLIST", fileClassListItems.toString());
            fileClassAdapter.notifyDataSetChanged();
            Log.d("FFFFF", "调用后"+fileClassListItems.get(fileClassListItems.size() - 1).getFileClassName());
        }
    }

    /**
     * 初始化 Dao 和 List 数据
     */
    public void initData() {

        if (fileClassDao == null)
            fileClassDao = new FileClassDao(this.getContext());

        fileClassListItems = fileClassDao.queryFileClassAll();
    }

    private void addNewFileClass(){
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

                        updateFileClassList(edit.getText().toString().trim());

                        dialog.dismiss();
                    }
                });

        editDialog.create().show();
    }

    /**
     * 修改分组信息提交
     */
    private void updateFileClassList(final String newFileClassName) {

        int newFileClassOrder = 0;

        // 更改好的分组信息
        final FileClass newFileClass = new FileClass(newFileClassName, newFileClassOrder);

        // 先判断空标题
        if (newFileClassName.isEmpty())
            HandleNullTitle();

        else {
            // 标题非空
            if (fileClassDao.checkDuplicate(newFileClass, null) != 0)
                // 新建分组重复
                HandleDuplicateFileClass(newFileClass);
            else {
                // 新建分组不重复
                try {
                    fileClassDao.insertFileClass(newFileClass);
                    fileClassListItems.add(fileClassAdapter.getCount()-1, newFileClass);
                    //Log.d("FILECLASSLIST", fileClassListItems.toString());
                    fileClassAdapter.notifyDataSetChanged();

                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 空标题提醒对话框
     */
    private void HandleNullTitle() {
        android.support.v7.app.AlertDialog emptyDialog = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.FileclassDialog_NullTitleAlertTitle)
                .setMessage(R.string.FileclassDialog_NullTitleAlertMsg)
                .setPositiveButton(R.string.FileclassDialog_NullTitleAlertPositiveButtonForOK, null)
                .create();
        emptyDialog.show();
    }

    /**
     * 重复分组标题对话框
     * @param newFileClass 添加的分组
     */
    private void HandleDuplicateFileClass(FileClass newFileClass) {
        android.support.v7.app.AlertDialog dupalert = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.GroupDialog_DuplicateAlertTitle)
                .setMessage(String.format(getContext().getText(R.string.GroupDialog_DuplicateAlertMsg).toString(), newFileClass.getFileClassName()))
                .setNegativeButton(R.string.GroupDialog_DuplicateAlertOk, null)
                .create();
        dupalert.show();
    }

}