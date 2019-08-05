package com.baibuti.biji.UI.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import android.widget.LinearLayout;
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
import com.baibuti.biji.Data.Models.FileItem;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.Activity.FileCloudActivity;
import com.baibuti.biji.UI.Dialog.FileImportDialog;
import com.baibuti.biji.Utils.StringUtils.Define;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFragment extends Fragment {

    private Activity activity;

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

    private List<FileItem> importedDocuments = new ArrayList<>();

    //标记导入的文件是否被选择
    final private static int UNCHECKED = 0;
    final private static int CHECKED = 1;

    private LinearLayout searchResultLayout;
    private RecyclerView searchResultList;
    private List<Document> searchResults = new ArrayList<>();
    private DocumentAdapter searchResultAdapter;

    private boolean closeDSL = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            Log.e("test_filefragment", "有缓存: 执行");
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            Log.e("test_filefragment", "没有缓存: 执行");
            view = inflater.inflate(R.layout.fragment_filetab, container, false);

            initToolBar(view);
            initSearchResultLayout(view);
            initFileClassList(view);
            initDocumentLayout(view);

            closeDSL = true;
        }
        Log.e("db_path", "onCreateView: "+ fileClassDao.getDBPath());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        Log.e("test_filefragment", "onCreate: 执行");
    }

    /**
     * 初始化工具栏
     * @param view
     */
    private void initToolBar(View view){
        Toolbar mToolBar = view.findViewById(R.id.tab_file_toolbar);
        mToolBar.setTitle(R.string.FileFrag_Header);

        mToolBar.setNavigationIcon(R.drawable.ic_cloud_upload_black_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Enter cloud disk",Toast.LENGTH_LONG).show();
                //添加菜单逻辑
                try {
                    Intent intent = new Intent(getActivity(), FileCloudActivity.class);
                    getActivity().startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
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
                            fileClassAdapter.isDeleting = true;
                            fileClassDao.deleteFileClass(fileClassListItems.get(lastPositionClicked).getId());
                            documentDao.deleteDocumentByClass(fileClassListItems.get(lastPositionClicked).getFileClassName());
                            fileClassListItems.remove(lastPositionClicked);
                            documentListItems.clear();
                            documentListsByClass.remove(lastPositionClicked);
                            fileClassAdapter.notifyDataSetChanged();
                            documentAdapter.notifyDataSetChanged();
                            documentHeader.setText("");
                            unSelectedText.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_LONG).show();
                        }
                        //fileClassAdapter.isDeleting = false;
                        break;
                    case R.id.action_import_documents:
                        //导入资料
                        if(!documentHeader.getText().toString().equals("")) {
                            FileImportDialog cdd = new FileImportDialog(getActivity(), importedDocuments);
                            cdd.setOnFinishScanListener(new FileImportDialog.OnFinishScanListener() {
                                @Override
                                public void OnFinish() {
                                    Toast.makeText(getContext(), importedDocuments.size() + "", Toast.LENGTH_LONG).show();
                                    for (FileItem f : importedDocuments) {
                                        if (f.getTag() == CHECKED) {
                                            Document newDocument = new Document(f.getFileType(), f.getFileName(), f.getFilePath(), documentHeader.getText().toString());
                                            documentListItems.add(newDocument);
                                            documentDao.insertDocument(newDocument);
                                        }
                                    }

                                    documentAdapter.notifyDataSetChanged();

                                    documentListsByClass.remove(lastPositionClicked);
                                    documentListsByClass.add(lastPositionClicked, new ArrayList<>(documentListItems));

                                    importedDocuments.clear();
                                }
                            });
                            cdd.show();
                        }
                        break;
                }
                return true;
            }
        });

        //搜索框
        documentSearchView = (SearchView) view.findViewById(R.id.filefragment_filesearch);
        documentSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(getContext(), "onQueryTextSubmit", Toast.LENGTH_LONG).show();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(null != imm){
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
                }
                documentSearchView.clearFocus();
                documentSearchView.onActionViewCollapsed();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchResults.clear();
                        searchResultAdapter.notifyDataSetChanged();
                    }
                });
                searchResultLayout.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String s) {
                Toast.makeText(getContext(), "onQueryTextChange", Toast.LENGTH_LONG).show();

                searchResults.clear();
                searchResultLayout.setVisibility(View.VISIBLE);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchDocuments(s);
                    }
                });

                if(closeDSL) {
                    searchResultLayout.setVisibility(View.GONE);
                    closeDSL = false;
                }

                return true;
            }
        });

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

    /**
     * 搜索结果页
     * @param s
     */
    private void searchDocuments(String s){
        if(!s.equals("")) {
            for (List<Document> l : documentListsByClass) {
                for (Document d : l) {
                    if (d.getDocumentName().contains(s)) {
                        searchResults.add(d);
                    }
                }
            }
        }
        searchResultAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化搜索结果布局
     * @param view
     */
    private void initSearchResultLayout(View view){
        searchResultLayout = (LinearLayout) view.findViewById(R.id.filefragment_search_result);
        searchResultList = (RecyclerView) view.findViewById(R.id.filefragment_serachresultlist);
        searchResultList.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultAdapter = new DocumentAdapter(searchResults);
        searchResultAdapter.setOnDocumentClickListener(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void OnDocumentClick(String path) {
                openDocument(path);
            }
        });
        searchResultList.setAdapter(searchResultAdapter);
        documentSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchResultLayout.setVisibility(View.GONE);
                documentSearchView.clearFocus();
                return false;
            }
        });
    }

    /**
     * 初始化分类列表
     * @param view
     */
    private void initFileClassList(View view){
        //初始化 fileclasslistitem
        initData();
        fileClassAdapter = new FileClassAdapter(getContext(), fileClassListItems);
        fileClassList = (ListView) view.findViewById(R.id.filefragment_fileclasses);
        fileClassList.setAdapter(fileClassAdapter);
        fileClassList.setVerticalScrollBarEnabled(false);
        fileClassList.setDivider(null);
        fileClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                fileClassAdapter.isDeleting = false;
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

    /**
     * 初始化资料列表
     * @param view
     */
    private void initDocumentLayout(View view){
        dealWithDocuments();
        documentHeader = (TextView) view.findViewById(R.id.filefragment_document_list_header);
        unSelectedText = (TextView) view.findViewById(R.id.filefragment_document_unselected);
        documentHeader.setText("");
        unSelectedText.setVisibility(View.VISIBLE);
        documentRecyclerView = (RecyclerView) view.findViewById(R.id.filefragment_document_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        documentRecyclerView.setLayoutManager(layoutManager);
        documentListItems.clear();
        documentAdapter = new DocumentAdapter(documentListItems);
        documentAdapter.setOnDocumentClickListener(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void OnDocumentClick(String path) {
                openDocument(path);
            }
        });
        documentAdapter.setOnDocumentLongClickListener(new DocumentAdapter.OnDocumentLongClickListener() {
            @Override
            public void OnDocumentLongClick(int position) {
                showConfirmDialog(position);
            }
        });
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

        fileClassListItems = fileClassDao.queryFileClassAll();
        for(FileClass f: fileClassListItems){
            if(!f.getFileClassName().equals("+")) {
                List<Document> l = documentDao.queryDocumentAll(f.getFileClassName());
                documentListsByClass.add(l);
            }
        }
    }

    /**
     * 新建分类
     * @param position
     */
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
     * 确认对话框
     * @param position
     */
    private void showConfirmDialog(final int position){

        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getContext());
        normalDialog.setIcon(R.drawable.ic_error_black_24dp);
        normalDialog.setTitle("删除资料");
        normalDialog.setMessage("确认删除?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteDocumentFromList(position);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
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

    /**
     * 数据库只存储了资料的路径和分类名，要补充文件名和文件类型属性
     */
    private void dealWithDocuments(){

        File file;
        for(List<Document> l: documentListsByClass){
            for(Document d: l){
                file = new File(d.getDocumentPath());
                String name = file.getName();
                String type;
                if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    type="doc";
                }else if(name.endsWith(".ppt") || name.endsWith(".pptx")){
                    type="ppt";
                }else if(name.endsWith(".xls") || name.endsWith(".xlsx")){
                    type="xls";
                }else if(name.endsWith(".pdf")){
                    type="pdf";
                }else if(name.endsWith(".txt")){
                    type="txt";
                }else if(name.endsWith(".zip")){
                    type="zip";
                }else{
                    type="unknown";
                }
                d.setDocumentName(name);
                d.setDocumentType(type);
            }
        }
    }

    /**
     * 更新右侧资料列表
     * @param position
     */
    private void updateDocumentRecyclerview(int position){
        documentListItems.clear();
        documentListItems.addAll(documentListsByClass.get(position));
        documentAdapter.notifyDataSetChanged();
        Log.i("test", "updateDocumentRecyclerview: "+documentListsByClass.get(position).size());
    }

    /**
     * 判断输入的分类名是否合法（支持中文，字母，或下划线）
     * @param fileClassName
     * @return
     */
    private boolean isLegalName(String fileClassName){
        Pattern p = Pattern.compile("[\\w]+");
        Matcher m = p.matcher(fileClassName);
        return m.matches();
    }

    /**
     * 调用wps打开文档
     * @param path
     */
    private void openDocument(String path){

        try {
            File file = new File(path);
            Log.e("test", "openDocument: 打开文件");
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");
            Log.e("test", "openDocument: 获得Intent");
            Bundle bundle = new Bundle();
            //打开模式
            bundle.putString(Define.OPEN_MODE, Define.NORMAL);
            bundle.putBoolean(Define.ENTER_REVISE_MODE, true);//以修订模式打开
            //bundle.putString(Define.OPEN_MODE, Define.READ_ONLY);
            bundle.putBoolean(Define.SEND_SAVE_BROAD, true);
            bundle.putBoolean(Define.SEND_CLOSE_BROAD, true);
            bundle.putBoolean(Define.HOME_KEY_DOWN, true);
            bundle.putBoolean(Define.BACK_KEY_DOWN, true);
            bundle.putBoolean(Define.ENTER_REVISE_MODE, true);
            bundle.putBoolean(Define.IS_SHOW_VIEW, false);
            bundle.putBoolean(Define.AUTO_JUMP, true);
            //设置广播
            bundle.putString(Define.THIRD_PACKAGE, activity.getPackageName());
            intent.setAction(Intent.ACTION_VIEW);
            Log.e("test", "openDocument: 设置action");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(activity,
                        "com.baibuti.biji.FileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(contentUri, "*/*");
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
            }
            Log.e("test", "openDocument: 即将发送");
            intent.putExtras(bundle);
            activity.startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 从右侧列表中删除资料
     * @param position
     */
    private void deleteDocumentFromList(int position){
        try {
            String path = documentListItems.get(position).getDocumentPath();
            documentDao.deleteDocumentByPath(path);
            documentListItems.remove(position);
            documentListsByClass.get(lastPositionClicked).remove(position);
            documentAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}