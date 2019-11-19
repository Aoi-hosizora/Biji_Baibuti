package com.baibuti.biji.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocClassDao;
import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.adapter.DocClassAdapter;
import com.baibuti.biji.model.dao.local.DocumentDao;
import com.baibuti.biji.model.dao.local.DocClassDao;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.vo.FileItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.dialog.FileImportDialog;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.baibuti.biji.util.otherUtil.DefineString;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.jwsd.libzxing.OnQRCodeScanCallback;
import com.jwsd.libzxing.QRCodeManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.id_view_file_search)
    private SearchView m_searchView;

    @BindView(R.id.id_document_srl)
    private SwipeRefreshLayout m_srl;

    // ListView
    @BindView(R.id.id_document_list_view)
    private RecyclerViewEmptySupport m_documentListView;

    @BindView(R.id.id_docclass_list_view)
    private ListView m_docClassListView;

    // 标记导入的文件是否被选择
    final private static int CHECKED = 1;

    private static class PageData {

        /**
         * 用于展示的 Document
         */
        List<Document> showDocumentList = new ArrayList<>();

        /**
         * 所有 DocClass (用于展示)
         */
        List<DocClass> docClassListItems = new ArrayList<>();

        /**
         * 所有的 Document (用于分组显示)
         */
        List<Document> documentListItems = new ArrayList<>();
    }

    private PageData pageData = new PageData();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_file, container, false);
            ButterKnife.bind(this, view);

            initView();
            initData();
        }
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private void initView() {

        // Toolbar
        Toolbar m_toolbar = view.findViewById(R.id.tab_file_toolbar);
        m_toolbar.setTitle("文档资料");
        m_toolbar.inflateMenu(R.menu.file_frag_action);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View view) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });
        m_toolbar.setPopupTheme(R.style.popup_theme);
        m_toolbar.setOnMenuItemClickListener(menuItemClickListener);

        // Empty View
        m_documentListView.setEmptyView(view.findViewById(R.id.id_document_empty));

        // Swl
        m_srl.setColorSchemeResources(R.color.colorPrimary);
        m_srl.setOnRefreshListener(this::initData);

        // Document
        DocumentAdapter documentAdapter = new DocumentAdapter(getContext());
        documentAdapter.setDocumentList(pageData.showDocumentList);
        documentAdapter.setOnDocumentClickListener(this::openDocument);
        documentAdapter.setOnDocumentLongClickListener(this::deleteDocumentFromList);
        m_documentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        m_documentListView.setAdapter(documentAdapter);

        // DocClass
        DocClassAdapter docClassAdapter = new DocClassAdapter(getContext());
        docClassAdapter.setList(pageData.docClassListItems);
        m_docClassListView.setVerticalScrollBarEnabled(false);
        m_docClassListView.setDivider(null);
        m_docClassListView.setOnItemClickListener((adapterView, view, position, id) -> onDocClassItemClicked(pageData.docClassListItems.get(position)));
        m_docClassListView.setAdapter(docClassAdapter);

        // Search
        LayoutUtil.AdjustSearchViewLayout(m_searchView);
        m_searchView.setIconified(false);
        m_searchView.setSubmitButtonEnabled(true);
        m_searchView.setQueryRefinementEnabled(true);
        m_searchView.setQueryHint("搜索文档");
        m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) { return false; }

            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Document> searchResult = SearchUtil.getSearchItems(pageData.documentListItems.toArray(new Document[0]), query);
                pageData.showDocumentList.clear();
                pageData.showDocumentList.addAll(searchResult);
                m_documentListView.getAdapter().notifyDataSetChanged();
                return true;
            }
        });
    }

    /**
     * 初始化分类列表和文件列表
     */
    public void initData() {

        IDocClassDao docClassDao = DaoStrategyHelper.getInstance().getDocClassDao(getActivity());
        IDocumentDao documentDao = DaoStrategyHelper.getInstance().getDocumentDao(getActivity());

        try {
            pageData.docClassListItems.clear();
            pageData.documentListItems.clear();
            pageData.showDocumentList.clear();
            pageData.docClassListItems.addAll(docClassDao.queryAllDocClasses());
            pageData.documentListItems.addAll(documentDao.queryAllDocuments());

            if (pageData.docClassListItems.size() != 0)
                pageData.showDocumentList.addAll(filterDocumentByDocClass(pageData.docClassListItems.get(0), pageData.documentListItems));

            m_docClassListView.getAdapter().notify();
            m_documentListView.getAdapter().notifyDataSetChanged();

        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(getActivity(), "错误", "加载数据错误：" + ex.getMessage());
        }

        if (m_srl.isRefreshing())
            m_srl.setRefreshing(false);
    }

    /**
     * 点击分组，更新显示或者新建
     */
    private void onDocClassItemClicked(DocClass docClass) {
        pageData.showDocumentList.clear();
        pageData.showDocumentList.addAll(filterDocumentByDocClass(docClass, pageData.documentListItems));
        m_documentListView.getAdapter().notifyDataSetChanged();
    }

    /**
     * 过滤显示
     */
    private List<Document> filterDocumentByDocClass(DocClass docClass, List<Document> documents) {
        List<Document> ret = new ArrayList<>();
        for (Document document : documents)
            if (document.getDocClass().getId() == docClass.getId())
                ret.add(document);
        return ret;
    }

    // region Toolbar

    /**
     * Toolbar 菜单点击事件
     */
    private Toolbar.OnMenuItemClickListener menuItemClickListener = (MenuItem item) -> {
        switch (item.getItemId()) {
            case R.id.action_new_fileclass:
                addDocClass();
                break;
            case R.id.action_rename_fileclass:
                renameDocClass();
                break;
            case R.id.action_delete_fileclass:
                deleteDocClass();
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
    };

    /**
     * 中文 字母 数字
     */
    private boolean isIllegalName(String fileClassName){
        return !Pattern.compile("[A-Za-z0-9\\u4e00-\\u9fa5_]+").matcher(fileClassName).matches();
    }

    /**
     * 新建分组
     */
    private void addDocClass() {
        showInputDialog(getActivity(),
            "新建文档分组", "", "新文档分组名...", 1,
            "确定", (d, w, text) -> {
                text = text.trim();
                if (text.isEmpty()) {
                    showAlert(getActivity(), "错误", "没有输入分组名。");
                    return;
                }
                if (isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名格式不支持，只允许中文、字母、数字或下划线。");
                    return;
                }
                DocClass newDocClass = new DocClass(text);

                IDocClassDao docClassDao = DaoStrategyHelper.getInstance().getDocClassDao(getActivity());
                try {
                    // SUCCESS | FAILED | DUPLICATED
                    DbStatusType status = docClassDao.insertDocClass(newDocClass);
                    if (status == DbStatusType.SUCCESS) {
                        showToast(getActivity(), "分组 \"" + newDocClass.getName() + "\" 新建成功");
                        pageData.docClassListItems.add(newDocClass);
                        m_docClassListView.getAdapter().notify();
                        return;
                    }
                    if (status == DbStatusType.DUPLICATED)
                        showAlert(getActivity(), "错误", "分组名 \"" + newDocClass.getName() + "\" 重复，请重新输入。");
                    else if (status == DbStatusType.FAILED)
                        showAlert(getActivity(), "错误", "新建分组错误，请重试。");
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", "新建分组错误：" + ex.getMessage());
                }
            },
            "取消", null
        );
    }

    /**
     * 重命名分组
     */
    private void renameDocClass() {

        DocClass docClass = pageData.docClassListItems.get(m_docClassListView.getSelectedItemPosition());
        String motoName = docClass.getName();

        showInputDialog(getActivity(),
            "重命名分组", docClass.getName(), "", 1,
            "重命名", (v, d, text) -> {
                text = text.trim();
                if (text.isEmpty()) {
                    showAlert(getActivity(), "错误", "没有输入分组名。");
                    return;
                }
                if (isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名格式不支持，只允许中文、字母、数字或下划线。");
                    return;
                }
                docClass.setName(text);

                IDocClassDao docClassDao = DaoStrategyHelper.getInstance().getDocClassDao(getActivity());
                try {
                    // SUCCESS | FAILED | DUPLICATED | DEFAULT
                    DbStatusType status = docClassDao.updateDocClass(docClass);
                    if (status == DbStatusType.SUCCESS) {
                        showToast(getActivity(), "分组 \"" + docClass.getName() + "\" 修改成功");
                        m_docClassListView.getAdapter().notify();
                        return;
                    }
                    docClass.setName(motoName);
                    if (status == DbStatusType.DUPLICATED)
                        showAlert(getActivity(), "错误", "分组名 \"" + docClass.getName() + "\" 重复，请重新输入。");
                    else if (status == DbStatusType.DEFAULT)
                        showAlert(getActivity(), "错误", "无法修改默认分组名，请重新输入。");
                    else
                        showAlert(getActivity(), "错误", "分组名修改错误，请重试。");
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", "修改分组名错误：" + ex.getMessage());
                }
            },
            "返回", null
        );
    }

    /**
     * 删除分组
     */
    private void deleteDocClass() {

        DocClass docClass = pageData.docClassListItems.get(m_docClassListView.getSelectedItemPosition());
        IDocClassDao docClassDao = DaoStrategyHelper.getInstance().getDocClassDao(getActivity());
        IDocumentDao documentDao = DaoStrategyHelper.getInstance().getDocumentDao(getActivity());
        try {
            List<Document> documents = documentDao.queryDocumentByClassId(docClass.getId());
            if (documents.isEmpty()) {
                showAlert(getActivity(),
                    "删除", "是否删除分组 \"" + docClass.getName() + "\"？",
                    "删除", (d, w) -> {
                        try {
                            // SUCCESS | FAILED | DEFAULT
                            DbStatusType status = docClassDao.deleteDocClass(docClass.getId(), false);
                            if (status == DbStatusType.SUCCESS)
                                showToast(getActivity(), "分组 \"" + docClass.getName() +"\" 删除成功");
                            else if (status == DbStatusType.DEFAULT)
                                showAlert(getActivity(), "错误", "无法删除默认分组。");
                            else
                                showAlert(getActivity(), "错误", "分组名删除错误，请重试。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(getActivity(), "错误", "删除分组错误：" + ex.getMessage());
                        }
                    },
                    "取消", null
                );
            } else {
                showAlert(getActivity(),
                    "删除", "分组 \"" + docClass.getName() + "\" 有相关联的 " + documents.size() + " 条文档，是否同时删除？",
                    "删除分组及文档记录", (d, w) -> {
                        try {
                            // SUCCESS | FAILED | DEFAULT
                            DbStatusType status = docClassDao.deleteDocClass(docClass.getId(), false);
                            if (status == DbStatusType.SUCCESS)
                                showToast(getActivity(), "分组 \"" + docClass.getName() + "\" 删除成功");
                            else if (status == DbStatusType.DEFAULT)
                                showAlert(getActivity(), "错误", "无法删除默认分组。");
                            else
                                showAlert(getActivity(), "错误", "分组名删除错误，请重试。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(getActivity(), "错误", "删除分组错误：" + ex.getMessage());
                        }
                    },
                    "删除分组并修改为默认分组", (d, w) -> {
                        try {
                            // SUCCESS | FAILED | DEFAULT
                            DbStatusType status = docClassDao.deleteDocClass(docClass.getId(), true);
                            if (status == DbStatusType.SUCCESS)
                                showToast(getActivity(), "分组 \"" + docClass.getName() + "\" 删除成功");
                            else if (status == DbStatusType.DEFAULT)
                                showAlert(getActivity(), "错误", "无法删除默认分组。");
                            else
                                showAlert(getActivity(), "错误", "分组名删除错误，请重试。");
                        } catch (ServerException ex) {
                            ex.printStackTrace();
                            showAlert(getActivity(), "错误", "删除分组错误：" + ex.getMessage());
                        }
                    },
                    "取消", null
                );
            }
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(getActivity(), "错误", "删除文档错误：" + ex.getMessage());
        }
    }

    /**
     * 导入分组
     */
    private void importFileclass(){
        //导入资料
        if(!m_txt_documentHeader.getText().toString().equals("")) {

            List<FileItem> importedDocuments = new ArrayList<>();

            FileImportDialog cdd = new FileImportDialog(getActivity(), importedDocuments);
            cdd.setOnFinishScanListener(() -> {
//                Toast.makeText(getContext(), importedDocuments.size() + "", Toast.LENGTH_LONG).show();
                ProgressDialog progressDialog = showProgress(getActivity(), "上传中...", false, null);
                for (int i = 0; i < importedDocuments.size(); i++) {

                    FileItem f = importedDocuments.get(i);
                    if (f.getTag() == CHECKED) {
                        Document newDocument = new Document(f.getFileType(), f.getFileName(), f.getFilePath(), m_txt_documentHeader.getText().toString());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                documentListItems.add(newDocument);
                            }
                        });
                        documentDao.insertDocument(newDocument);
                    }
                }

                progressDialog.dismiss();
                m_documentListView.getAdapter().notifyDataSetChanged();
                documentListsByClass.remove(lastPositionClicked);
                documentListsByClass.add(lastPositionClicked, new ArrayList<>(documentListItems));
                importedDocuments.clear();
            });
            cdd.show();
        }
    }

    /**
     * 共享文档
     */
    private void shareDocuments() {

        if (m_txt_documentHeader.getText().toString().equals("")) {
            Toast.makeText(getContext(), "未选择分类", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = showProgress(getActivity(), "生成共享码...", false, null);
        if (!AuthManager.getInstance().isLogin()) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "未登录，无法共享", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareCode = FileClassUtil.getShareCode(m_txt_documentHeader.getText().toString());
        progressDialog.dismiss();

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        ImageView qrCodeImageView = new ImageView(getActivity());
        qrCodeImageView.setMinimumWidth(800);
        qrCodeImageView.setMinimumHeight(800);
        qrCodeImageView.setImageDrawable(null); // TODO
        showAlert(getActivity(), "共享二维码", qrCodeImageView, "返回", null);

    }

    /**
     * 扫描共享码
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

    // endregion

    //////////////////////////////////////////////////////////////////////////////

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
     * 调用wps打开文档
     */
    private void openDocument(Document document) {

        File file = new File(document.getFilename());
        if (!file.exists()) {

            ProgressDialog progressDialog = showProgress(getActivity(), "下载中...", false, null);

            try {
                if (!DocClass.downloadFile(document)) {
                    progressDialog.dismiss();
                    showAlert(getActivity(), "错误", "文件 \"" + document.getFilename() + "\" 下载失败。");
                    return;
                }
                progressDialog.dismiss();
                documentDao.updateDocument(document);
            } catch (ServerException e) {
                e.printStackTrace();
                showAlert(getActivity(), "错误", "未知错误..."); // TODO
            }
        }

        try {
            //下载完成并更新路径，打开
            Intent intent = activity.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");
            if (intent == null)
                throw new Exception();
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
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(getActivity(), "错误", "打开文档错误。");
        }

    }

    /**
     * 从右侧列表中删除资料
     */
    private void deleteDocumentFromList(Document document) {

        showAlert(getActivity(),
            "删除", "确定删除文档资料 \"" + document.getBaseFilename() + "\" ？",
            "删除", (d, w) -> {
                try {
                    IDocumentDao documentDao = DaoStrategyHelper.getInstance().getDocumentDao(getContext());
                    if (documentDao.deleteDocument(document.getId()) == DbStatusType.FAILED)
                        showAlert(getActivity(), "错误", "删除文档错误");
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", "删除文档错误：" + ex.getMessage());
                }
                documentListItems.remove(document);
                m_documentListView.getAdapter().notifyDataSetChanged();
            },
            "取消", null
        );
    }

    /**
     * 登录时刷新
     */
    private void refresh(){

        ProgressDialog progressDialog = showProgress(getActivity(), "加载中...", false, null);

        docClassListItems.clear();
        documentListItems.clear();
        documentListsByClass.clear();

        docClassDao = new DocClassDao(activity);
        documentDao = new DocumentDao(activity);

        docClassListItems.addAll(docClassDao.queryAllDocClasses());
        documentDao.pushpull();
        for(DocClass f: docClassListItems){
            if(!f.getName().equals("+")) {
                List<Document> l = documentDao.queryDocumentByClassId(f.getName(), false);
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
            docClassListItems.add(docClassAdapter.getCount(), temp);
        }

        progressDialog.dismiss();

        docClassAdapter.notifyDataSetChanged();
        dealWithDocuments();
        m_txt_documentHeader.setText("");
        m_txt_unSelected.setVisibility(View.VISIBLE);
        documentListItems.clear();

    }
}