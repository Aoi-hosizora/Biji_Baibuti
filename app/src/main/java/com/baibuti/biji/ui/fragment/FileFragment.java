package com.baibuti.biji.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocClassDao;
import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.dao.net.ShareCodeNetDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.wps.WpsService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.adapter.DocClassAdapter;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.vo.FileItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.dialog.FileImportDialog;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.jwsd.libzxing.OnQRCodeScanCallback;
import com.jwsd.libzxing.QRCodeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.id_view_file_search)
    SearchView m_searchView;

    @BindView(R.id.id_document_srl)
    SwipeRefreshLayout m_srl;

    @BindView(R.id.id_document_list_view)
    RecyclerViewEmptySupport m_documentListView;

    @BindView(R.id.id_docclass_list_view)
    ListView m_docClassListView;

    private Dialog m_itemPopupMenu;

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
        documentAdapter.setOnDocumentClickListener(this::OpenDocument);
        documentAdapter.setOnDocumentLongClickListener(this::DocListItem_LongClicked);
        m_documentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        m_documentListView.setAdapter(documentAdapter);

        // DocClass
        DocClassAdapter docClassAdapter = new DocClassAdapter(getContext());
        docClassAdapter.setList(pageData.docClassListItems);
        m_docClassListView.setVerticalScrollBarEnabled(false);
        m_docClassListView.setDivider(null);
        m_docClassListView.setOnItemClickListener((adapterView, view, position, id) ->
            onDocClassItemClicked(pageData.docClassListItems.get(position)));
        m_docClassListView.setAdapter(docClassAdapter);

        // Search
        LayoutUtil.AdjustSearchViewLayout(m_searchView);
        m_searchView.setIconified(false);
        m_searchView.setSubmitButtonEnabled(true);
        m_searchView.setQueryRefinementEnabled(true);
        m_searchView.setQueryHint("搜索文档");
        m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // TODO back
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
     * 点击分组，更新显示
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

    // region popup

    /**
     * 长按 文档列表弹出菜单
     */
    private void DocListItem_LongClicked(Document document) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        DocClass docClass = pageData.docClassListItems.get(m_docClassListView.getSelectedItemPosition());

        m_itemPopupMenu = new Dialog(activity, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(activity, m_itemPopupMenu, R.layout.popup_doc_long_click_item);

        TextView label = root.findViewById(R.id.id_doc_popup_label);
        label.setText(String.format("当前选中项: %s (%s)", document.getBaseFilename(), docClass.getName()));

        root.findViewById(R.id.id_doc_popup_delete).setOnClickListener((v) -> PopupDeleteDoc_Clicked(document));
        root.findViewById(R.id.id_doc_popup_open).setOnClickListener((v) -> OpenDocument(document));
        root.findViewById(R.id.id_doc_popup_cancel).setOnClickListener((v) -> m_itemPopupMenu.dismiss());

        m_itemPopupMenu.show();
    }

    /**
     * Popup 删除文档
     */
    private void PopupDeleteDoc_Clicked(Document document) {

        showAlert(getActivity(),
            "删除", "确定删除文档资料 \"" + document.getBaseFilename() + "\" ？",
            "删除", (d, w) -> {
                IDocumentDao documentDao = DaoStrategyHelper.getInstance().getDocumentDao(getContext());
                try {
                    if (documentDao.deleteDocument(document.getId()) == DbStatusType.FAILED) {
                        showAlert(getActivity(), "错误", "删除文档错误");
                    } else {
                        pageData.documentListItems.remove(document);
                        pageData.showDocumentList.remove(document);
                        m_documentListView.getAdapter().notifyDataSetChanged();
                    }
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", "删除文档错误：" + ex.getMessage());
                }
            },
            "取消", null
        );
    }

    // endregion

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
                importDocumentIntoDocClass();
                break;
            case R.id.action_share_documents:
                shareTheWholeClassDocuments();
                break;
            case R.id.action_scan_share_code:
                scanShareCodeQrCode();
                break;
        }
        return true;
    };

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
                if (CommonUtil.isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名不合法，仅允许由中文、字母、数字和下划线组成");
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
        showInputDialog(getActivity(),
            "重命名分组", docClass.getName(), "", 1,
            "重命名", (v, d, text) -> {
                text = text.trim();
                if (text.isEmpty()) {
                    showAlert(getActivity(), "错误", "没有输入分组名。");
                    return;
                }
                if (CommonUtil.isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名不合法，仅允许由中文、字母、数字和下划线组成。");
                    return;
                }

                IDocClassDao docClassDao = DaoStrategyHelper.getInstance().getDocClassDao(getActivity());
                try {
                    // SUCCESS | FAILED | DUPLICATED | DEFAULT
                    DbStatusType status = docClassDao.updateDocClass(new DocClass(docClass.getId(), text));
                    if (status == DbStatusType.SUCCESS) {
                        docClass.setName(text);
                        showToast(getActivity(), "分组 \"" + text + "\" 修改成功");
                        m_docClassListView.getAdapter().notify();
                        return;
                    }
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
            if (documents.isEmpty()) { // 无关联
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
            } else { // 有关联
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

    // endregion

    // region Share

    /**
     * 导入分组
     */
    private void importDocumentIntoDocClass() {
        if (m_docClassListView.getSelectedItemPosition() == -1) {
            showAlert(getActivity(), "错误", "未选择文档分组，无法导入。");
            return;
        }
        DocClass docClass = pageData.docClassListItems.get(m_docClassListView.getSelectedItemPosition());

        List<FileItem> importedDocuments = new ArrayList<>();
        FileImportDialog importDialog = new FileImportDialog(getActivity(), importedDocuments);
        importDialog.setOnFinishScanListener(() -> {

            ProgressDialog progressDialog = showProgress(getActivity(), "上传中...", false, null);

            IDocumentDao documentDao = DaoStrategyHelper.getInstance().getDocumentDao(getContext());
            for (int i = 0; i < importedDocuments.size(); i++) {

                FileItem f = importedDocuments.get(i);
                if (f.getTag() == FileItem.CHECKED) {
                    Document newDocument = new Document(-1, f.getFilePath(), docClass);
                    pageData.documentListItems.add(newDocument);
                    pageData.showDocumentList.add(newDocument);
                    try {
                        // 顺便上传
                        DbStatusType status = documentDao.insertDocument(newDocument);
                        if (status == DbStatusType.UPLOAD_FAILED) {
                            showAlert(getActivity(), "错误", "文件上传失败，服务器错误。");
                            return;
                        } else if (status == DbStatusType.FAILED) {
                            showAlert(getActivity(), "错误", "文档记录更新失败。");
                            return;
                        }
                    } catch (ServerException ex) {
                        ex.printStackTrace();
                        showAlert(getActivity(), "错误", "文件上传失败：" + ex.getMessage());
                        return;
                    }
                }
            }
            progressDialog.dismiss();
            m_documentListView.getAdapter().notifyDataSetChanged();
        });
        importDialog.show();
    }

    /**
     * 共享时长选择
     */
    private Map<String, Integer> exTextNumbers = new LinkedHashMap<String, Integer>() {{
        put("1个小时", 1);
        put("2个小时", 2);
        put("5个小时", 5);
        put("8个小时", 8);
        put("12个小时", 12);
        put("1天", 24);
        put("2天", 2 * 24);
        put("7天", 7 * 24);
        put("15天", 15 * 24);
    }};

    private String[] exTexts = exTextNumbers.keySet().toArray(new String[0]);

    /**
     * 共享整个分组的文档
     */
    private void shareTheWholeClassDocuments() {

        if (m_docClassListView.getSelectedItemPosition() == -1) {
            showToast(getContext(), "未选择文档分组");
            return;
        }
        DocClass docClass = pageData.docClassListItems.get(m_docClassListView.getSelectedItemPosition());

        if (!AuthManager.getInstance().isLogin()) {
            showToast(getContext(), "未登录，无法共享");
            return;
        }

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        // Ex
        Spinner exChooser = new Spinner(getActivity());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_common_spinner, exTexts);
        exChooser.setAdapter(spinnerAdapter);
        exChooser.setSelection(Arrays.asList(exTexts).indexOf("1个小时"), true);
        showAlert(getActivity(),
            "文件共享时间", exChooser,
            "确定", (d, w) -> {
                int exp = exTextNumbers.get(exTexts[exChooser.getSelectedItemPosition()]);
                exp *= 3600;

                ProgressDialog progressDialog = showProgress(getActivity(), "生成共享码...", false, null);

                String shareCode;
                ShareCodeNetDao shareCodeNetDao = new ShareCodeNetDao();
                try {
                    shareCode = shareCodeNetDao.newShareCode(docClass, exp);
                } catch (ServerException ex) {
                    progressDialog.dismiss();
                    ex.printStackTrace();
                    showAlert(getActivity(), "错误", "获取共享码错误：" + ex.getMessage());
                    return;
                }

                Bitmap qrCode = CommonUtil.generateQrCode(shareCode, 800, Color.BLACK);
                progressDialog.dismiss();
                if (qrCode == null) {
                    showAlert(getContext(), "错误", "二维码生成错误。");
                    return;
                }

                ImageView qrCodeImageView = new ImageView(getActivity());
                qrCodeImageView.setImageBitmap(qrCode);
                qrCodeImageView.setMinimumWidth(qrCode.getWidth());
                qrCodeImageView.setMinimumHeight(qrCode.getHeight());
                showAlert(getActivity(), "共享二维码", qrCodeImageView, "返回", null);

            },
            "取消", null
        );
    }

    /*
        // TODO 检查 uuid 是否同时被多个用户占用

        本地文件 -> 上传服务器 (文件路径名不变，增加 uuid)
        本地文件查看 -> 检查本地是否存在 (不存在 -> 下载, 存在 -> 打开)
        共享下载文件 -> 根据共享码获取 url，直接下载，选择分组 -> 本地保存后更新路径名，uuid 不变 -> 上传为自己的数据 (同一个 uuid)
        共享文件分享 -> 直接上传服务器处理 Redis 返回 共享码
     */

    /**
     * 扫描共享码
     */
    private void scanShareCodeQrCode() {
        if (!AuthManager.getInstance().isLogin()) {
            Toast.makeText(getContext(), "未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        QRCodeManager.getInstance()
            .with(getActivity())
            .scanningQRCode(new OnQRCodeScanCallback() {

                @Override
                public void onCompleted(String shareCode) {

                    if (shareCode.isEmpty())
                        showAlert(getContext(), "错误", "共享码错误。");
                    else {
                        showAlert(getContext(),
                            "文件共享", "是否下载共享码中的文件？",
                            "下载", (d, w) -> {
                                // TODO 下载
                            },
                            "取消", null
                        );
                    }
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

    /**
     * Popup / List 打开文档
     */
    private void OpenDocument(Document document) {
        showAlert(getContext(),
            "打开", "是否打开文档 \"" + document.getBaseFilename() + "\"？",
            "打开", (d, w) -> {
                File file = new File(document.getFilename());
                if (!file.exists()) {
                    showAlert(getContext(),
                        "错误", "文档 \"" + document.getBaseFilename() + "\" 不存在，是否下载？",
                        "下载", (d1, w1) -> {

                            // TODO api 下载

                        }, "取消", null
                    );
                } else {
                    // boolean status = WpsService.OpenDocumentThroughWPS(getActivity(), new File(document.getFilename()));
                    // if (!status) showAlert(getContext(), "错误", "打开文档错误。");

                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), WpsService.getMIMEType(file));
                    startActivity(intent);
                }
            },
            "取消", null
        );
    }
}
