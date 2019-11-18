package com.baibuti.biji.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.adapter.FileClassAdapter;
import com.baibuti.biji.model.dao.local.DocumentDao;
import com.baibuti.biji.model.dao.local.FileClassDao;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.po.FileItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.dialog.FileImportDialog;
import com.baibuti.biji.util.otherUtil.DefineString;
import com.jwsd.libzxing.OnQRCodeScanCallback;
import com.jwsd.libzxing.QRCodeManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileFragment extends BaseFragment implements IContextHelper {

    private Activity activity;

    private List<DocClass> docClassListItems = new ArrayList<>();
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
    final private static int CHECKED = 1;

    private LinearLayout searchResultLayout;
    private RecyclerView searchResultList;
    private List<Document> searchResults = new ArrayList<>();
    private DocumentAdapter searchResultAdapter;
    private ProgressDialog loadingDialog;

    //标记是否需要关闭搜索结果页面
    private boolean closeDSL = false;
    /**
     * 标记登录时是否刷新过
     */
    private boolean HasRefreshed = false;
    /**
     * 标记是否已经初始化界面
     */
    private boolean HasInitedView = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_filetab, container, false);

            loadingDialog = new ProgressDialog(getContext());
            loadingDialog.setMessage(getResources().getString(R.string.NoteFrag_LoadingData));
            loadingDialog.setCanceledOnTouchOutside(false);

            initToolBar(view);
            initSearchResultLayout(view);
            initData();

            AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {

                // TODO
                public void onLogin(String UserName) {
                    if(getUserVisibleHint()) {
                        Log.e("测试", "FileFragment.onLogin: 调用");
                        refresh();
                        HasRefreshed = true;
                    }
                    else
                        HasRefreshed = false;
                }

                @Override
                public void onLogout() {
                    if(getUserVisibleHint()) {
                        refresh();
                        HasRefreshed = true;
                    }
                    else
                        HasRefreshed = false;
                }
            });


        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        Log.e("test_filefragment", "onCreate: 执行");
    }

    @Override
    public boolean onBackPressed() {

    }

    /**
     * 初始化工具栏
     * @param view
     */
    private void initToolBar(View view){
        Toolbar mToolBar = view.findViewById(R.id.tab_file_toolbar);
        mToolBar.setTitle(R.string.FileFrag_Header);

        mToolBar.setNavigationIcon(R.drawable.tab_menu);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openNavMenu();
            }
        });

        mToolBar.setPopupTheme(R.style.popup_theme);

        mToolBar.inflateMenu(R.menu.filefragment_menu);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_rename_fileclass:
                        renameFileclass();
                        break;
                    case R.id.action_delete_fileclass:
                        deleteFileclass();
                        break;
                    case R.id.action_import_documents:
                        importFileclass();
                        break;
                    case R.id.action_share_documents:
                        shareDocuments();
                        break;
                    case R.id.action_scan_share_code:
                        scanShareCode();
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

    private void renameFileclass(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //重命名分组
                String currentFileClassName = documentHeader.getText().toString();
                final DocClass currentDocClass = fileClassDao.queryFileClassByName(currentFileClassName, false);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final EditText edit = new EditText(getContext());
                        AlertDialog.Builder editDialog = new AlertDialog.Builder(getContext());
                        editDialog.setTitle(getString(R.string.FileclassDialog_Renameclass));
                        editDialog.setIcon(R.drawable.ic_rename);
                        edit.setHint("资料分类名...");
                        //设置dialog布局
                        editDialog.setView(edit);

                        //设置按钮
                        editDialog.setPositiveButton(getString(R.string.FileClassDialog_ConfirmBtn) , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(!isLegalName(edit.getText().toString().trim())) {
                                           new android.support.v7.app.AlertDialog
                                                .Builder(getContext())
                                                .setTitle(R.string.FileClassDialog_NullTitleAlertTitle)
                                                .setMessage(R.string.FileClassDialog_IllegalTitleAlertMsg)
                                                .setPositiveButton(R.string.FileClassDialog_NullTitleAlertPositiveButtonForOK, null)
                                                .create().show();
                                        }
                                        else
                                            updateFileClassList(edit.getText().toString().trim(), TAG_RENAME, currentDocClass, lastPositionClicked);

                                        dialog.dismiss();
                                    }
                                });

                        editDialog.create().show();
                    }
                });
            }
        }).start();

    }

    private void deleteFileclass(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //删除分类
                try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileClassAdapter.isDeleting = true;
                        }
                    });

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoadingDialog("删除中...");
                        }
                    });

                    Log.e("测试", "run: " + lastPositionClicked);
                    fileClassDao.deleteFileClass(docClassListItems.get(lastPositionClicked).getId());
                    documentDao.deleteDocumentByClass(docClassListItems.get(lastPositionClicked).getName(), true);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            docClassListItems.remove(lastPositionClicked);
                            documentListItems.clear();
                            documentListsByClass.remove(lastPositionClicked);
                            fileClassAdapter.lastButton = null;
                            fileClassAdapter.notifyDataSetChanged();
                            documentAdapter.notifyDataSetChanged();
                            documentHeader.setText("");
                            unSelectedText.setVisibility(View.VISIBLE);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelLoadingDialog();
                    }
                });
            }
        }).start();

    }

    private void importFileclass(){
        //导入资料
        if(!documentHeader.getText().toString().equals("")) {
            FileImportDialog cdd = new FileImportDialog(getActivity(), importedDocuments);
            cdd.setOnFinishScanListener(new FileImportDialog.OnFinishScanListener() {
                @Override
                public void OnFinish() {
                    Toast.makeText(getContext(), importedDocuments.size() + "", Toast.LENGTH_LONG).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showLoadingDialog("上传中...");
                                }
                            });

                            for(int i = 0; i < importedDocuments.size(); i++){

                                FileItem f = importedDocuments.get(i);
                                if (f.getTag() == CHECKED) {
                                    Document newDocument = new Document(f.getFileType(), f.getFileName(), f.getFilePath(), documentHeader.getText().toString());
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            documentListItems.add(newDocument);
                                        }
                                    });
                                    documentDao.insertDocument(newDocument);
                                }
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    documentAdapter.notifyDataSetChanged();
                                    documentListsByClass.remove(lastPositionClicked);
                                    documentListsByClass.add(lastPositionClicked, new ArrayList<>(documentListItems));
                                    importedDocuments.clear();

                                    cancelLoadingDialog();
                                }
                            });
                        }
                    }).start();
                }
            });
            cdd.show();
        }
    }

    /**
     * TODO !!!
     */
    private void shareDocuments(){

        if(documentHeader.getText().toString().equals(""))
            Toast.makeText(getContext(), "未选择分类", Toast.LENGTH_SHORT).show();

        showLoadingDialog("生成共享码...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!AuthManager.getInstance().isLogin()){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "未登录，无法共享", Toast.LENGTH_SHORT).show();
                            cancelLoadingDialog();
                        }
                    });
                    return;
                }
                File file = FileClassUtil.getShareCode(documentHeader.getText().toString());
                if(null != file){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cancelLoadingDialog();

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            LayoutInflater inflater = getLayoutInflater();
                            View dialoglayout = inflater.inflate(R.layout.documents_share_code, null);
                            AlertDialog dia = builder.create();
                            dia.show();
                            Window window = dia.getWindow();
                            WindowManager.LayoutParams lp = window.getAttributes();
                            lp.width = 800;
                            lp.height = 800;
                            window.setGravity(Gravity.CENTER);
                            window.setAttributes(lp);
                            window.setContentView(dialoglayout);
                            ((ImageView) dialoglayout.findViewById(R.id.documents_share_code_iv)).setImageBitmap(file2drawable(file));
                        }
                    });
                }
                else{
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "获取共享码失败", Toast.LENGTH_SHORT).show();
                            cancelLoadingDialog();
                        }
                    });
                }
            }
        }).start();
    }

    private Bitmap file2drawable(File file){
        try {
            Uri uri = Uri.fromFile(file);
            return MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "未找到图片", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * TODO !!!
     */
    private void scanShareCode() {
        if(!AuthManager.getInstance().isLogin()){
            Toast.makeText(getContext(), "未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeManager.getInstance()
            .with(getActivity())
            .scanningQRCode(new OnQRCodeScanCallback() {

                @Override
                public void onCompleted(String shareCode) {
                    showAlert(getActivity(),
                        "扫描结果", "添加共享文件？",
                        "确认", (d, v) -> getSharedDocuments(shareCode),
                        "取消", null
                    );
                }

                @Override
                public void onError(Throwable throwable) {
                    showToast(getActivity(), throwable.getMessage());
                }

                @Override
                public void onCancel() {
                    showToast(getActivity(), "操作已取消");
                }
            });
    }

    /**
     * TODO !!!
     * @param shareCode
     */
    private void getSharedDocuments(final String shareCode){
        Log.e("测试", "shareCode: "+shareCode);
        try {
            JSONObject jsonObject = new JSONObject(shareCode);
            final String username = jsonObject.getString("usr");
            final String foldername = jsonObject.getString("folder");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DocumentUtil.getSharedFiles("?username=" + username + "&docClass="+foldername);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        });
                    }catch(ServerException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch(JSONException e){
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
                    if (d.getDocName().contains(s)) {
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
            public void OnDocumentClick(Document document) {
                openDocument(document);
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
     * 初始化分类列表和文件列表
     */
    public void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fileClassDao == null)
                    fileClassDao = new FileClassDao(activity);

                if(documentDao == null)
                    documentDao = new DocumentDao(activity);
                docClassListItems = fileClassDao.queryAllFileClasses();
                documentDao.pushpull();
                for(DocClass f: docClassListItems){
                    if(!f.getName().equals("+")) {
                        List<Document> l = documentDao.queryDocumentsByClassName(f.getName(), false);
                        documentListsByClass.add(l);
                    }
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileClassAdapter = new FileClassAdapter(getContext(), docClassListItems);
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
                                    DocClass currentDocClass = (DocClass) docClassListItems.get(position);
                                    //更改文件列表标题
                                    documentHeader.setText(currentDocClass.getName());
                                    //获取分类下的文件
                                    updateDocumentRecyclerview(position);
                                }
                                else
                                    addNewFileClass(position);//添加新类别
                            }
                        });

                        if(!docClassListItems.get(docClassListItems.size() - 1).getName().equals("+")) {
                            DocClass temp = new DocClass("+", 0);
                            docClassListItems.add(fileClassAdapter.getCount(), temp);
                            fileClassAdapter.notifyDataSetChanged();
                        }

                        Log.e("测试", "filefrg.run: 执行");

                        initDocumentLayout(view);
                    }
                });
            }
        }).start();

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
            public void OnDocumentClick(Document document) {
                openDocument(document);
            }
        });
        documentAdapter.setOnDocumentLongClickListener(new DocumentAdapter.OnDocumentLongClickListener() {
            @Override
            public void OnDocumentLongClick(int position) {
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
        });
        documentRecyclerView.setAdapter(documentAdapter);

        closeDSL = true;
        HasInitedView = true;
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

                        if(!isLegalName(edit.getText().toString().trim())) {
                            new android.support.v7.app.AlertDialog
                                .Builder(getContext())
                                .setTitle(R.string.FileClassDialog_NullTitleAlertTitle)
                                .setMessage(R.string.FileClassDialog_IllegalTitleAlertMsg)
                                .setPositiveButton(R.string.FileClassDialog_NullTitleAlertPositiveButtonForOK, null)
                                .create().show();
                        }
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
    private void updateFileClassList(final String newFileClassName, int tag, DocClass currentDocClass, int position) {

        int newFileClassOrder = 0;
        // 更改好的分组信息
        final DocClass newDocClass = new DocClass(newFileClassName, newFileClassOrder);

        // 先判断空标题
        if (newFileClassName.isEmpty()) {
            android.support.v7.app.AlertDialog emptyDialog = new android.support.v7.app.AlertDialog
                .Builder(getContext())
                .setTitle(R.string.FileClassDialog_NullTitleAlertTitle)
                .setMessage(R.string.FileClassDialog_NullTitleAlertMsg)
                .setPositiveButton(R.string.FileClassDialog_NullTitleAlertPositiveButtonForOK, null)
                .create();
            emptyDialog.show();
        }


        else {
            // 标题非空
            if (fileClassDao.checkDuplicate(newDocClass, null) != 0) {
                // 分组重复
                new android.support.v7.app.AlertDialog
                    .Builder(getContext())
                    .setTitle(R.string.GroupDialog_DuplicateAlertTitle)
                    .setMessage(String.format(getContext().getText(R.string.GroupDialog_DuplicateAlertMsg).toString(), newDocClass.getName()))
                    .setNegativeButton(R.string.GroupDialog_DuplicateAlertOk, null)
                    .create().show();
            }
            else {
                // 分组不重复
                try {
                    if(tag == TAG_NEW) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showLoadingDialog("添加新分类..");
                                    }
                                });

                                long ret = fileClassDao.insertFileClass(newDocClass);
                                newDocClass.setId((int)ret);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        docClassListItems.add(fileClassAdapter.getCount() - 1, newDocClass);
                                    }
                                });
                                List<Document> l = documentDao.queryDocumentsByClassName(newDocClass.getName());
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        documentListsByClass.add(position, l);
                                        fileClassAdapter.notifyDataSetChanged();
                                        Log.e("测试", "更新本地记录："
                                                + new UtLogDao(getContext()).getLog(LogModule.Mod_FileClass).getUpdateTime().toString());

                                        cancelLoadingDialog();
                                    }
                                });
                            }
                        }).start();
                    }
                    else if(tag == TAG_RENAME){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showLoadingDialog("重命名...");
                                    }
                                });

                                currentDocClass.setName(newFileClassName);
                                fileClassDao.updateFileClass(currentDocClass);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        docClassListItems.remove(position);
                                        docClassListItems.add(position, currentDocClass);
                                        documentHeader.setText(newFileClassName);
                                        fileClassAdapter.notifyDataSetChanged();

                                        cancelLoadingDialog();

                                    }
                                });
                            }
                        }).start();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 非法字符提醒对话框
     */
    private void HandleIllegalName(){

    }

    /**
     * 数据库只存储了资料的路径和分类名，要补充文件名和文件类型属性
     */
    private void dealWithDocuments(){

        File file;
        String name;
        for(List<Document> l: documentListsByClass){
            for(Document d: l){
                if(!d.getFilename().equals("")) {
                    file = new File(d.getFilename());
                    name = file.getName();
                    d.setDocName(name);
                }
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
    }

    /**
     * 判断输入的分类名是否合法（支持中文，字母，或下划线）
     * @param fileClassName
     * @return
     */
    private boolean isLegalName(String fileClassName){
        return Pattern.compile("[\\w]+").matcher(fileClassName).matches();
    }

    /**
     * 调用wps打开文档
     * @param document
     */
    private void openDocument(Document document){

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(document.getFilename());
                    if(!file.exists()){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoadingDialog("下载中...");
                            }
                        });
                        try {
                            if(!DocumentUtil.downloadFile(document)){
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelLoadingDialog();
                                        Toast.makeText(getContext(), "下载失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            documentDao.updateDocument(document);
                            cancelLoadingDialog();
                        }catch (ServerException e){
                            e.printStackTrace();
                        }
                    }
                    //下载完成并更新路径，打开
                    Intent intent = activity.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");
                    Bundle bundle = new Bundle();
                    //打开模式
                    bundle.putString(DefineString.OPEN_MODE, DefineString.NORMAL);
                    bundle.putBoolean(DefineString.ENTER_REVISE_MODE, true);//以修订模式打开
                    //bundle.putString(DefineString.OPEN_MODE, DefineString.READ_ONLY);
                    bundle.putBoolean(DefineString.SEND_SAVE_BROAD, true);
                    bundle.putBoolean(DefineString.SEND_CLOSE_BROAD, true);
                    bundle.putBoolean(DefineString.HOME_KEY_DOWN, true);
                    bundle.putBoolean(DefineString.BACK_KEY_DOWN, true);
                    bundle.putBoolean(DefineString.ENTER_REVISE_MODE, true);
                    bundle.putBoolean(DefineString.IS_SHOW_VIEW, false);
                    bundle.putBoolean(DefineString.AUTO_JUMP, true);
                    //设置广播
                    bundle.putString(DefineString.THIRD_PACKAGE, activity.getPackageName());
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
                }
            }).start();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 从右侧列表中删除资料
     * @param position
     */
    private void deleteDocumentFromList(int position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoadingDialog("删除中...");
                        }
                    });
                    String name = documentListItems.get(position).getDocName();
                    String path = documentListItems.get(position).getFilename();
                    documentDao.deleteDocument(name, path, true);
                    documentListItems.remove(position);
                    documentListsByClass.get(lastPositionClicked).remove(position);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            documentAdapter.notifyDataSetChanged();
                            cancelLoadingDialog();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 登录时刷新
     */
    private void refresh(){

        showLoadingDialog("加载中...");

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(!HasInitedView);
                //清除旧数据
                docClassListItems.clear();
                documentListItems.clear();
                documentListsByClass.clear();

                fileClassDao = new FileClassDao(activity);
                documentDao = new DocumentDao(activity);

                docClassListItems.addAll(fileClassDao.queryAllFileClasses());
                documentDao.pushpull();
                for(DocClass f: docClassListItems){
                    if(!f.getName().equals("+")) {
                        List<Document> l = documentDao.queryDocumentsByClassName(f.getName(), false);
                        if(null != l && l.size() != 0) {
                            for (Document document : l)
                                Log.e("测试", "refresh: " + document.getId() + ' ' +
                                        document.getDocName() + ' ' +
                                        document.getClassName() + '\n');
                        }
                        documentListsByClass.add(l);
                    }
                }

                if(!docClassListItems.get(docClassListItems.size() - 1).getName().equals("+")) {
                    DocClass temp = new DocClass("+", 0);
                    docClassListItems.add(fileClassAdapter.getCount(), temp);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileClassAdapter.notifyDataSetChanged();
                        dealWithDocuments();
                        documentHeader.setText("");
                        unSelectedText.setVisibility(View.VISIBLE);
                        documentListItems.clear();

                        cancelLoadingDialog();
                    }
                });
            }
        }).start();
    }

    /**
     * 对用户可见时，判断是否需要刷新
     */
    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint() && !HasRefreshed){
            refresh();
            HasRefreshed = true;
        }
        if(getUserVisibleHint())
            searchResultLayout.setVisibility(View.GONE);
    }

    private void showLoadingDialog(String message){
        if(!loadingDialog.isShowing()) {
            loadingDialog.setMessage(message);
            loadingDialog.show();
        }
    }

    private void cancelLoadingDialog(){
        if(loadingDialog.isShowing())
            loadingDialog.cancel();
    }
}