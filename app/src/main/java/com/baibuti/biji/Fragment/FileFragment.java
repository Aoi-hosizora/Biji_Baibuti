package com.baibuti.biji.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baibuti.biji.Data.FileClass;
import com.baibuti.biji.Data.FileClassAdapter;
import com.baibuti.biji.R;
import com.baibuti.biji.db.FileClassDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileFragment extends Fragment {

    private List<FileClass> fileClassListItems  = new ArrayList<>();
    private ListView fileClassList;
    private FileClassDao fileClassDao;
    private FileClassAdapter fileClassAdapter;
    private View view;
    private int TAG_NEW = 0;
    private int TAG_RENAME = 1;

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
        fileClassList.setDivider(null);
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

        fileClassList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(Menu.NONE, 0, 0, "重命名");
                contextMenu.add(Menu.NONE, 1, 0, "删除");
            }
        });

        if(!fileClassListItems.get(fileClassListItems.size() - 1).getFileClassName().equals("+")) {
            FileClass temp = new FileClass("+", 0);
            fileClassListItems.add(fileClassAdapter.getCount(), temp);
            fileClassAdapter.notifyDataSetChanged();
        }
    }

    //选中菜单Item后触发
    public boolean onContextItemSelected(MenuItem item){

        //关键代码在这里
        final AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        if(menuInfo.position == fileClassList.getCount()-1)
            return false;

        switch (item.getItemId()){
            case 0:
                //重命名分类
                //Toast.makeText(getContext(), String.valueOf(menuInfo.position), Toast.LENGTH_LONG).show();

                String currentFileClassName = fileClassListItems.get(menuInfo.position).getFileClassName();
                final FileClass currentFileClass = fileClassDao.queryFileClassByName(currentFileClassName);
                //Log.d("KKKKK", currentFileClass.getFileClassName());

                final EditText edit = new EditText(getContext());
                AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
                editDialog.setTitle(getString(R.string.FileclassDialog_Renameclass));
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

                                updateFileClassList(edit.getText().toString().trim(), TAG_RENAME, currentFileClass, menuInfo.position);

                                dialog.dismiss();
                            }
                        });

                editDialog.create().show();
                break;
            case 1:
                //点击第二个菜单项要做的事，如获取点击的数据
                //Toast.makeText(getContext(), ""+fileClassListItems.get(menuInfo.position).getFileClassName(), Toast.LENGTH_LONG).show();
                try {
                    fileClassDao.deleteFileClass(fileClassListItems.get(menuInfo.position).getId());
                    fileClassListItems.remove(menuInfo.position);
                    fileClassAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "删除失败", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onContextItemSelected(item);
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
        //Toast.makeText(getContext(),"Add new fileclass", Toast.LENGTH_SHORT).show();
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
                        //Toast.makeText(getContext(),
                                //edit.getText().toString().trim(),Toast.LENGTH_SHORT).show();

                        updateFileClassList(edit.getText().toString().trim(), TAG_NEW, null, 0);

                        // Check if no view has focus:
                        View view = getView();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        dialog.dismiss();
                    }
                });

        editDialog.create().show();
    }

    /**
     * 修改分组信息提交
     */
    private void updateFileClassList(final String newFileClassName, int tag, FileClass currentFileClass, int position) {

        int newFileClassOrder = 0;
        // 更改好的分组信息
        final FileClass newFileClass = new FileClass(newFileClassName, newFileClassOrder);

        // 先判断空标题
        if (newFileClassName.isEmpty())
            HandleNullTitle();

        else {
            // 标题非空
            if (fileClassDao.checkDuplicate(newFileClass, null) != 0)
                // 分组重复
                HandleDuplicateFileClass(newFileClass);
            else {
                // 分组不重复
                try {
                    if(tag == TAG_NEW) {
                        fileClassDao.insertFileClass(newFileClass);
                        fileClassListItems.add(fileClassAdapter.getCount() - 1, newFileClass);
                    }
                    else if(tag == TAG_RENAME){
                        currentFileClass.setFileClassName(newFileClassName);
                        fileClassDao.updateFileClass(currentFileClass);
                        fileClassListItems.remove(position);
                        fileClassListItems.add(position, currentFileClass);
                    }
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