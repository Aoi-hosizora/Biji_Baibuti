package com.baibuti.biji.UI.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.Data.Adapters.DocumentAdapter;
import com.baibuti.biji.Data.Adapters.FileClassAdapter;
import com.baibuti.biji.Data.DB.DocumentDao;
import com.baibuti.biji.Data.DB.FileClassDao;
import com.baibuti.biji.Data.Models.Document;
import com.baibuti.biji.Data.Models.FileClass;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Dialog.FileImportDialog;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFragment extends Fragment {

    private List<FileClass> fileClassListItems  = new ArrayList<>();
    private ListView fileClassList;
    private FileClassDao fileClassDao;
    private FileClassAdapter fileClassAdapter;
    private View view;
    private int TAG_NEW = 0;
    private int TAG_RENAME = 1;
    private int lastPositionClicked = 0;

    private SearchView documentSearchView;

    private TextView documentHeader;
    private RecyclerView documentRecyclerView;
    private TextView unSelectedText;
    private List<Document> documentListItems = new ArrayList<>();
    private DocumentAdapter documentAdapter;

    private DocumentDao documentDao;
    private List<List<Document> > documentListsByClass = new ArrayList<>();//保存各个文件分类下的文件列表


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_filetab, container, false);

            initToolBar(view);
            initFileClassList(view);
            initDocumentLayout(view);

        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initToolBar(View view){
        Toolbar mToolBar = view.findViewById(R.id.tab_file_toolbar);
        mToolBar.setTitle(R.string.FileFrag_Header);

        mToolBar.setNavigationIcon(R.drawable.tab_menu);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"This is a menu",Toast.LENGTH_LONG).show();
                //添加菜单逻辑
            }
        });

        mToolBar.setPopupTheme(R.style.popup_theme);

        mToolBar.inflateMenu(R.menu.filefragment_menu);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_rename_fileclass:
                        //重命名分组
                        String currentFileClassName = documentHeader.getText().toString();
                        final FileClass currentFileClass = fileClassDao.queryFileClassByName(currentFileClassName);

                        final EditText edit = new EditText(getContext());
                        AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
                        editDialog.setTitle(getString(R.string.FileclassDialog_Renameclass));
                        editDialog.setIcon(R.drawable.ic_rename);
                        edit.setHint("资料分类名...");
                        //设置dialog布局
                        editDialog.setView(edit);

                        //设置按钮
                        editDialog.setPositiveButton(getString(R.string.FileClassDialog_ConfirmBtn)
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(!isLegalName(edit.getText().toString().trim()))
                                            HandleIllegalName();
                                        else
                                            updateFileClassList(edit.getText().toString().trim(), TAG_RENAME, currentFileClass, lastPositionClicked);

                                        dialog.dismiss();
                                    }
                                });

                        editDialog.create().show();
                        break;
                    case R.id.action_delete_fileclass:
                        //删除分类
                        try {
                            fileClassDao.deleteFileClass(fileClassListItems.get(lastPositionClicked).getId());
                            documentDao.deleteDocumentByClass(fileClassListItems.get(lastPositionClicked).getFileClassName());
                            fileClassListItems.remove(lastPositionClicked);
                            documentListsByClass.remove(lastPositionClicked);
                            fileClassAdapter.notifyDataSetChanged();
                            documentAdapter.notifyDataSetChanged();
                            documentHeader.setText("");
                            unSelectedText.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.action_import_documents:
                        FileImportDialog cdd=new FileImportDialog(getActivity());
                        cdd.show();
                        break;
                }
                return true;
            }
        });

        documentSearchView = (SearchView) view.findViewById(R.id.filefragment_filesearch);
        //去除searchview下划线
        try{
            Class<?> argClass = documentSearchView.getClass();
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(documentSearchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initFileClassList(View view){
        //初始化 fileclasslistitem
        initData();
        fileClassAdapter = new FileClassAdapter(getContext(), fileClassListItems);
        fileClassList = (ListView) view.findViewById(R.id.filefragment_fileclasses);
        fileClassList.setAdapter(fileClassAdapter);
        fileClassList.setVerticalScrollBarEnabled(false);
        fileClassList.setDivider(null);
        Log.d("text_for_listview", "initFileClassList: "+fileClassList.toString());
        fileClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(unSelectedText.getVisibility() == View.VISIBLE)
                    unSelectedText.setVisibility(View.GONE);
                if(position != fileClassList.getCount() - 1) {
                    lastPositionClicked = position;
                    FileClass currentFileClass = (FileClass) fileClassListItems.get(position);
                    //更改文件列表标题
                    documentHeader.setText(currentFileClass.getFileClassName());
                    //获取分类下的文件
                    updateDocumentRecyclerview(position);
                }
                else
                    addNewFileClass(position);//添加新类别
            }
        });

        if(!fileClassListItems.get(fileClassListItems.size() - 1).getFileClassName().equals("+")) {
            FileClass temp = new FileClass("+", 0);
            fileClassListItems.add(fileClassAdapter.getCount(), temp);
            fileClassAdapter.notifyDataSetChanged();
        }
    }

    private void initDocumentLayout(View view){
        initDocuments();
        documentHeader = (TextView) view.findViewById(R.id.filefragment_document_list_header);
        unSelectedText = (TextView) view.findViewById(R.id.filefragment_document_unselected);
        documentHeader.setText(R.string.app_name);
        unSelectedText.setVisibility(View.GONE);
        documentRecyclerView = (RecyclerView) view.findViewById(R.id.filefragment_document_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        documentRecyclerView.setLayoutManager(layoutManager);
        documentListItems.clear();
        documentListItems.addAll(documentListsByClass.get(0));
        documentAdapter = new DocumentAdapter(documentListItems);
        documentRecyclerView.setAdapter(documentAdapter);
    }

    /**
     * 初始化 Dao 和 List 数据
     */
    public void initData() {

        if (fileClassDao == null)
            fileClassDao = new FileClassDao(this.getContext());

        if(documentDao == null)
            documentDao = new DocumentDao(this.getContext());

        //Test for saving files
        documentDao.insertDocument(new Document("卧佛", "/storage/emulated/0/Biji/默认笔记.pdf"));
        documentDao.insertDocument(new Document("哦唔去", "/storage/emulated/0/tencent/QQfile_recv/毕业设计任务书.doc"));

        fileClassListItems = fileClassDao.queryFileClassAll();
        for(FileClass f: fileClassListItems){
            if(!f.getFileClassName().equals("+")) {
                List<Document> l = documentDao.queryDocumentAll(f.getFileClassName());
                documentListsByClass.add(l);
            }
        }
    }

    private void addNewFileClass(final int position){

        final EditText edit = new EditText(getContext());

        AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
        editDialog.setTitle(getString(R.string.FileClassDialog_AddNewClass));
        editDialog.setIcon(R.drawable.ic_rename);
        edit.setHint("资料分类名...");

        //设置dialog布局
        editDialog.setView(edit);

        //设置按钮
        editDialog.setPositiveButton(getString(R.string.FileClassDialog_ConfirmBtn)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getContext(),
                        //edit.getText().toString().trim(),Toast.LENGTH_SHORT).show();

                        if(!isLegalName(edit.getText().toString().trim()))
                            HandleIllegalName();
                        else
                            updateFileClassList(edit.getText().toString().trim(), TAG_NEW, null, position);

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
                        List<Document> l = documentDao.queryDocumentAll(newFileClass.getFileClassName());
                        documentListsByClass.add(position, l);
                    }
                    else if(tag == TAG_RENAME){
                        currentFileClass.setFileClassName(newFileClassName);
                        fileClassDao.updateFileClass(currentFileClass);
                        fileClassListItems.remove(position);
                        fileClassListItems.add(position, currentFileClass);
                        documentHeader.setText(newFileClassName);
                    }

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
                .setTitle(R.string.FileClassDialog_NullTitleAlertTitle)
                .setMessage(R.string.FileClassDialog_NullTitleAlertMsg)
                .setPositiveButton(R.string.FileClassDialog_NullTitleAlertPositiveButtonForOK, null)
                .create();
        emptyDialog.show();
    }

    /**
     * 非法字符提醒对话框
     */
    private void HandleIllegalName(){
        android.support.v7.app.AlertDialog emptyDialog = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.FileClassDialog_NullTitleAlertTitle)
                .setMessage(R.string.FileClassDialog_IllegalTitleAlertMsg)
                .setPositiveButton(R.string.FileClassDialog_NullTitleAlertPositiveButtonForOK, null)
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

    //Test for documentlist
    private void initDocuments(){

        File file;
        for(List<Document> l: documentListsByClass){
            if(l.size() == 0){
                //所在分类文件列表为空
                Toast.makeText(getContext(), "No files", Toast.LENGTH_LONG).show();
            }
            for(Document d: l){
                file = new File(d.getDocumentPath());
                String name = file.getName();
                String type = name.substring(name.length() - 3);
                d.setDocumentName(name);
                d.setDocumentType(type);
                Toast.makeText(getContext(), type, Toast.LENGTH_LONG).show();
                Log.d("类型保存", "initDocuments: " + d.getDocumentType());
            }
        }
    }

    private void updateDocumentRecyclerview(int position){
        documentListItems.clear();
        documentListItems.addAll(documentListsByClass.get(position));
        documentAdapter.notifyDataSetChanged();
    }

    private boolean isLegalName(String fileClassName){
        Pattern p = Pattern.compile("[\\w]*");
        Matcher m = p.matcher(fileClassName);
        return m.matches();
    }

}