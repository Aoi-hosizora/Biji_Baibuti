package com.baibuti.biji.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.InteractStrategy;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.contract.IDocClassInteract;
import com.baibuti.biji.common.interact.contract.IDocumentInteract;
import com.baibuti.biji.common.interact.server.ShareCodeNetInteract;
import com.baibuti.biji.common.retrofit.ServerUrl;
import com.baibuti.biji.model.dao.local.DownloadedDao;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.model.vo.MessageVO;
import com.baibuti.biji.service.doc.DocService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.activity.FileDownloadActivity;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.adapter.DocClassAdapter;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.vo.FileItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.dialog.FileImportDialog;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.google.gson.Gson;
import com.jwsd.libzxing.OnQRCodeScanCallback;
import com.jwsd.libzxing.QRCodeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx_activity_result2.RxActivityResult;

public class FileFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.id_view_file_search)
    SearchView m_searchView;

    private ImageView m_searchIcon;

    @BindView(R.id.id_document_srl)
    SwipeRefreshLayout m_srl;

    @BindView(R.id.id_document_header)
    TextView m_txt_document_header;

    @BindView(R.id.tab_file_toolbar)
    Toolbar m_toolBar;

    DocumentAdapter m_documentAdapter;
    DocClassAdapter m_docClassAdapter;

    private Dialog m_popup_document;
    private Dialog m_popup_docclass;

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
        } else {
            view = inflater.inflate(R.layout.fragment_file, container, false);
            ButterKnife.bind(this, view);

            initView();
            // initData();

            AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {

                @Override
                public void onLogin(String username) {
                    initData();
                }

                @Override
                public void onLogout() {
                    initData();
                }
            });
        }
        return view;
    }

    private void initView() {

        // Toolbar
        m_toolBar.setTitle("文档资料");
        m_toolBar.inflateMenu(R.menu.file_frag_action);
        m_toolBar.setNavigationIcon(R.drawable.tab_menu);
        m_toolBar.setNavigationOnClickListener((View view) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });
        m_toolBar.setOnMenuItemClickListener(menuItemClickListener);

        // Empty View
        RecyclerViewEmptySupport m_documentListView = view.findViewById(R.id.id_document_list_view);
        m_documentListView.setEmptyView(view.findViewById(R.id.id_document_empty));

        // Srl
        m_srl.setColorSchemeResources(R.color.colorPrimary);
        m_srl.setOnRefreshListener(this::initData);

        // Document
        m_documentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        m_documentAdapter = new DocumentAdapter(getContext());
        m_documentAdapter.setDocumentList(pageData.showDocumentList);
        m_documentAdapter.setOnDocumentClickListener(this::OpenDocument);
        m_documentAdapter.setOnDocumentLongClickListener(this::DocListItem_LongClicked);
        m_documentListView.setAdapter(m_documentAdapter);

        // DocClass
        ListView m_docClassListView = view.findViewById(R.id.id_docclass_list_view);
        m_docClassListView.setVerticalScrollBarEnabled(false);
        m_docClassListView.setDivider(null);
        m_docClassAdapter = new DocClassAdapter(getContext());
        m_docClassAdapter.setOnButtonClickListener((pos) ->
            onDocClassItemClicked(pageData.docClassListItems.get(pos)));
        m_docClassAdapter.setOnButtonLongClickListener((pos) -> {
            onDocClassItemClicked(pageData.docClassListItems.get(pos));
            DocClass_LongClicked(pageData.docClassListItems.get(pos));
            return true;
        });
        m_docClassAdapter.setDocClassList(pageData.docClassListItems);
        m_docClassListView.setAdapter(m_docClassAdapter);

        // Search
        m_searchIcon = m_searchView.findViewById(m_searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null));
        LinearLayout m_left = view.findViewById(R.id.id_document_left);
        LinearLayout m_right = view.findViewById(R.id.id_document_right);
        LayoutUtil.AdjustSearchViewLayout(m_searchView);
        m_searchIcon.setClickable(true);

        // m_searchView.setIconified(false);
        m_searchView.setIconifiedByDefault(false);
        m_searchView.setSubmitButtonEnabled(true);
        m_searchView.setQueryHint("搜索文档");
        m_searchView.clearFocus();
        m_searchView.onActionViewCollapsed();
        m_searchView.setOnClickListener((v) -> {
            m_searchView.onActionViewExpanded();
            Context context = getContext();
            m_searchIcon.setTag(true);
            if (context != null)
                m_searchIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sv_back_back_24dp));
        });

        m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                m_searchView.clearFocus();
                m_left.setVisibility(View.GONE);
                m_txt_document_header.setVisibility(View.GONE);
                m_right.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                m_srl.setEnabled(false); // 不允许下拉刷新
                if (getActivity() != null)
                    CommonUtil.closeSoftKeyInput(getActivity());
                onSearchSubmitClicked(query, false);
                return true;
            }
        });
        m_searchIcon.setOnClickListener((v) -> {
            m_searchView.clearFocus();
            m_searchView.onActionViewCollapsed();
            m_searchIcon.setTag(false);
            Context context = getContext();
            if (context != null)
                m_searchIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sv_back_back_24dp));

            m_left.setVisibility(View.VISIBLE);
            m_txt_document_header.setVisibility(View.VISIBLE);
            m_right.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 4));

            m_srl.setEnabled(true);
            onSearchSubmitClicked("", true);
        });
    }

    @Override
    public boolean onBackPressed() {
        if (m_searchIcon != null && (boolean) m_searchIcon.getTag()) {
            m_searchIcon.callOnClick();
            return true;
        }

        return false;
    }

    /**
     * 初始化分类列表和文件列表
     */
    public void initData() {

        IDocClassInteract docClassInteract = InteractStrategy.getInstance().getDocClassInteract(getActivity());
        IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getActivity());

        ProgressHandler.process(docClassInteract.queryAllDocClasses(), new InteractInterface<List<DocClass>>() {
            @Override
            public void onSuccess(List<DocClass> data) {
                pageData.docClassListItems.clear();
                pageData.docClassListItems.addAll(data);
                m_docClassAdapter.notifyDataSetChanged();

                ProgressHandler.process(documentInteract.queryAllDocuments(), new InteractInterface<List<Document>>() {
                    @Override
                    public void onSuccess(List<Document> data) {

                        DocClass curr = m_docClassAdapter.getCurrentItem(); // <<<
                        int currIdx = curr == null ? -1 : pageData.docClassListItems.indexOf(curr);

                        pageData.documentListItems.clear();
                        pageData.showDocumentList.clear();
                        pageData.documentListItems.addAll(data);
                        m_toolBar.setTitle("文档资料");

                        if (currIdx == -1 && pageData.docClassListItems.size() != 0) {
                            m_docClassAdapter.setCurrentItem(pageData.docClassListItems.get(0));
                            onDocClassItemClicked(pageData.docClassListItems.get(0));
                        } else if (currIdx != -1 && pageData.docClassListItems.size() > currIdx)
                            onDocClassItemClicked(pageData.docClassListItems.get(currIdx));

                        if (m_srl.isRefreshing())
                            m_srl.setRefreshing(false);
                    }

                    @Override
                    public void onError(String message) {
                        showAlert(getContext(), "错误", message);
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        showAlert(getContext(), "错误", "网路错误：" + throwable.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                showAlert(getContext(), "错误", message);
            }

            @Override
            public void onFailed(Throwable throwable) {
                showAlert(getContext(), "错误", "网路错误：" + throwable.getMessage());
            }
        });
    }

    /**
     * Search View 搜索
     *
     * @param isBack 是否返回还是搜索
     */
    private void onSearchSubmitClicked(String query, boolean isBack) {
        if (!isBack) {
            ProgressDialog progressDialog = showProgress(getContext(), "搜索 \"" + query + " \" 中...", false, null);
            if (query.trim().isEmpty())
                showToast(getActivity(), "没有输入搜索内容");
            else {
                List<Document> searchResult = SearchUtil.getSearchItems(pageData.documentListItems.toArray(new Document[0]), query);
                showToast(getActivity(), "共找到 " + searchResult.size() + " 条结果");
                pageData.showDocumentList.clear();
                pageData.showDocumentList.addAll(searchResult);
                m_documentAdapter.notifyDataSetChanged();
                m_toolBar.setTitle("\"" + query + "\" 的搜索结果");
            }
            new Handler().postDelayed(progressDialog::dismiss, 50);
        } else {
            ProgressDialog progressDialog = showProgress(getContext(), "返回中...", false, null);
            initData();
            new Handler().postDelayed(progressDialog::dismiss, 50);
        }
    }

    /**
     * 点击分组，更新显示
     */
    private void onDocClassItemClicked(DocClass docClass) {
        // if (docClass.getId() == m_docClassAdapter.getCurrentItem().getId())
        //     return;

        // ProgressDialog progressDialog = showProgress(getContext(), "加载数据中...", false, null);

        m_docClassAdapter.setCurrentItem(docClass);
        pageData.showDocumentList.clear();
        pageData.showDocumentList.addAll(filterDocumentByDocClass(docClass, pageData.documentListItems));
        m_txt_document_header.setText(String.format(Locale.CHINA, "%s (共 %d 项)", docClass.getName(), pageData.showDocumentList.size()));
        m_documentAdapter.notifyDataSetChanged();

        // new Handler().postDelayed(progressDialog::dismiss, 50);
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
     * 长按 分组弹出菜单
     */
    private void DocClass_LongClicked(DocClass docClass) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        m_popup_docclass = new Dialog(activity, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(activity, m_popup_docclass, R.layout.popup_doc_long_click_class);

        ((TextView) root.findViewById(R.id.id_doc_class_popup_label)).setText(String.format("当前选中分组: %s", docClass.getName()));
        root.findViewById(R.id.id_doc_class_popup_rename).setOnClickListener((v) -> ToolbarRenameDocClass_Clicked(docClass));
        root.findViewById(R.id.id_doc_class_popup_delete).setOnClickListener((v) -> ToolDeleteDocClass_Clicked(docClass));
        root.findViewById(R.id.id_doc_class_popup_share).setOnClickListener((v) -> ToolbarShareWholeDocClassDoc_Clicked(docClass));
        root.findViewById(R.id.id_doc_class_popup_cancel).setOnClickListener((v) -> m_popup_docclass.dismiss());

        m_popup_docclass.show();
    }

    /**
     * 长按 文档列表弹出菜单
     */
    private void DocListItem_LongClicked(Document document) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        m_popup_document = new Dialog(activity, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(activity, m_popup_document, R.layout.popup_doc_long_click_item);

        ((TextView) root.findViewById(R.id.id_doc_popup_label)).setText(String.format("当前选中文档: %s (%s)", document.getBaseFilename(), document.getDocClass().getName()));
        root.findViewById(R.id.id_doc_popup_open).setOnClickListener((v) -> OpenDocument(document));
        root.findViewById(R.id.id_doc_popup_share).setOnClickListener((v) -> PopupShareDocument_Clicked(document));
        root.findViewById(R.id.id_doc_popup_move).setOnClickListener((v) -> PopupMoveDocument_Clicked(document));
        root.findViewById(R.id.id_doc_popup_delete).setOnClickListener((v) -> PopupDeleteDoc_Clicked(document, false));
        root.findViewById(R.id.id_doc_popup_delete_file).setOnClickListener((v) -> PopupDeleteDoc_Clicked(document, true));
        root.findViewById(R.id.id_doc_popup_cancel).setOnClickListener((v) -> m_popup_document.dismiss());

        m_popup_document.show();
    }

    /**
     * Popup 删除文档
     */
    private void PopupDeleteDoc_Clicked(Document document, boolean isDeleteFile) {
        m_popup_document.dismiss();
        String message = (isDeleteFile)
            ? "确定删除列表中的文档记录 \"" + document.getBaseFilename() + "\"，若为本地文件，则该操作不会删除源文件？"
            : "确定删除列表中的文档记录以及文件 \"" + document.getBaseFilename() + "\"？";
        showAlert(getActivity(), "删除", message,
            "删除", (d, w) -> {
                IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getContext());

                ProgressHandler.process(getContext(), "删除记录中...", true,
                    documentInteract.deleteDocument(document.getId()), new InteractInterface<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            if (isDeleteFile)
                                AppPathUtil.deleteFile(document.getFilename());

                            pageData.documentListItems.remove(document);
                            pageData.showDocumentList.remove(document);
                            m_documentAdapter.notifyDataSetChanged();

                            m_txt_document_header.setText(String.format(Locale.CHINA,
                                "%s (共 %d 项)", m_docClassAdapter.getCurrentItem().getName(), pageData.showDocumentList.size()));
                            showToast(getContext(), "文档 \"" + document.getBaseFilename() + "\" 删除成功");
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(getActivity(), "错误", message);
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(getActivity(), "错误", "网络错误：" + throwable);
                        }
                    }
                );
            },
            "取消", null
        );
    }

    /**
     * Popup 移动至分组
     */
    private void PopupMoveDocument_Clicked(Document document) {
        m_popup_document.dismiss();
        IDocClassInteract docClassInteract = InteractStrategy.getInstance().getDocClassInteract(getContext());
        IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getContext());

        ProgressHandler.process(getContext(), "加载分组信息...", true,
            docClassInteract.queryAllDocClasses(), new InteractInterface<List<DocClass>>() {
                @Override
                public void onSuccess(List<DocClass> docClasses) {
                    String[] titles = new String[docClasses.size()];
                    for (int i = 0; i < docClasses.size(); i++)
                        titles[i] = docClasses.get(i).getName();

                    showAlert(getContext(), "修改分组",
                        titles, (d, w) -> {
                            if (document.getDocClass().getId() == docClasses.get(w).getId())
                                return;

                            DocClass motoClass = document.getDocClass();
                            document.setDocClass(docClasses.get(w));
                            ProgressHandler.process(getContext(), "修改文档分组中...", true,
                                documentInteract.updateDocument(document), new InteractInterface<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean data) {
                                        pageData.showDocumentList.remove(document);
                                        m_docClassAdapter.notifyDataSetChanged();
                                        showToast(getActivity(), "文档 \"" + document.getBaseFilename() + "\" 成功移动至 \"" + docClasses.get(w).getName() + " \"");
                                        d.dismiss();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        document.setDocClass(motoClass);
                                        showAlert(getActivity(), "错误", message);
                                    }

                                    @Override
                                    public void onFailed(Throwable throwable) {
                                        document.setDocClass(motoClass);
                                        showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                                    }
                                }
                            );
                        },
                        "返回", null
                    );
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", "分组信息加载失败：" + message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 共享单个文档
     */
    private void PopupShareDocument_Clicked(Document document) {
        if (m_popup_document != null && m_popup_document.isShowing())
            m_popup_document.dismiss();

        if (document == null) {
            showAlert(getActivity(), "错误", "没有选择文件。");
            return;
        }
        if (!AuthManager.getInstance().isLogin()) {
            showToast(getContext(), "未登录");
            return;
        }

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        showAlert(getContext(), "共享",
            "确定要共享文件 \"" + document.getFilename() + "\" ？",
            "共享", (d, w) -> {
                d.dismiss();

                // Ex
                Spinner exChooser = new Spinner(getActivity());
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_common_spinner, exTexts);
                exChooser.setAdapter(spinnerAdapter);
                exChooser.setSelection(Arrays.asList(exTexts).indexOf("1个小时"), true);

                showAlert(getActivity(),
                    "文件共享时间", exChooser,
                    "确定", (d1, w1) -> {
                        int exp = exTextNumbers.get(exTexts[exChooser.getSelectedItemPosition()]);
                        exp *= 3600;

                        ProgressDialog progressDialog = showProgress(getActivity(), "生成共享码...", false, null);

                        ShareCodeNetInteract shareCodeNetInteract = new ShareCodeNetInteract();
                        ProgressHandler.process(getContext(), "生成共享码...", true,
                            shareCodeNetInteract.newShareCode(new Document[]{document}, exp), new InteractInterface<String>() {
                                @Override
                                public void onSuccess(String shareCode) {
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
                                    showAlert(getActivity(), "共享二维码", qrCodeImageView,
                                        "复制共享码", (d2, w2) -> {
                                            if (CommonUtil.copyText(getContext(), shareCode))
                                                showToast(getContext(), "共享码：" + shareCode + " 复制成功");
                                        },
                                        "返回", null
                                    );
                                }

                                @Override
                                public void onError(String message) {
                                    showAlert(getActivity(), "错误", message);
                                }

                                @Override
                                public void onFailed(Throwable throwable) {
                                    showAlert(getActivity(), "错误", "网络错误：" + throwable.getMessage());
                                }
                            }
                        );
                    },
                    "取消", null
                );

            }, "取消", null
        );
    }

    // endregion

    // region Toolbar

    /**
     * Toolbar 菜单点击事件
     */
    private Toolbar.OnMenuItemClickListener menuItemClickListener = (MenuItem item) -> {

        switch (item.getItemId()) {

//            case R.id.action_new_fileclass:
//                ToolbarAddDocClass_Clicked();
//                break;
//            case R.id.action_rename_fileclass:
//                ToolbarRenameDocClass_Clicked(m_docClassAdapter.getCurrentItem());
//                break;
//            case R.id.action_delete_fileclass:
//                ToolDeleteDocClass_Clicked(m_docClassAdapter.getCurrentItem());
//                break;

            case R.id.action_share_documents:
                ToolbarShareWholeDocClassDoc_Clicked(m_docClassAdapter.getCurrentItem());
                break;
            case R.id.action_import_documents:
                ToolbarImportFile_Clicked();
                break;
            case R.id.action_scan_share_code:
                scanShareCodeQrCode();
                break;
            case R.id.action_downloaded_documents:
                RxActivityResult.on(this).startIntent(new Intent(getActivity(), FileDownloadActivity.class));
                break;
            case R.id.action_all_share_documents:
                showToast(getContext(), "未实现");
                break;
        }
        return true;
    };

    /**
     * 新建分组
     */
    @OnClick(R.id.id_docclass_list_view_add)
    void ToolbarAddDocClass_Clicked() {
        showInputDialog(getActivity(),
            "新建文档分组", "", "新文档分组名...", 1,
            "确定", (d, w, text) -> {
                text = text.trim();
                if (text.isEmpty()) {
                    showAlert(getActivity(), "错误", "没有输入分组名。");
                    return;
                }
                if (CommonUtil.isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名不合法，仅允许由1-30个中文、字母、数字和下划线组成。");
                    return;
                }
                DocClass newDocClass = new DocClass(text);

                IDocClassInteract docClassInteract = InteractStrategy.getInstance().getDocClassInteract(getActivity());
                ProgressHandler.process(getContext(), "新建分组中...", true,
                    docClassInteract.insertDocClass(newDocClass), new InteractInterface<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            showToast(getActivity(), "分组 \"" + newDocClass.getName() + "\" 新建成功");
                            pageData.docClassListItems.add(newDocClass);
                            onDocClassItemClicked(newDocClass);
                            m_docClassAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(getContext(), "错误", message);
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                        }
                    }
                );
            },
            "取消", null
        );
    }

    /**
     * 重命名分组
     */
    private void ToolbarRenameDocClass_Clicked(DocClass docClass) {
        if (m_popup_docclass != null && m_popup_docclass.isShowing())
            m_popup_docclass.dismiss();

        if (docClass == null) {
            showAlert(getActivity(), "错误", "没有选择分组。");
            return;
        }
        if (docClass.getName().equals(DocClass.DEF_DOCCLASS.getName())) {
            showAlert(getActivity(), "错误", "无法修改默认分组名。");
            return;
        }

        showInputDialog(getActivity(),
            "重命名分组", docClass.getName(), "", 1,
            "重命名", (v, d, text) -> {
                if (text.trim().isEmpty()) {
                    showAlert(getActivity(), "错误", "没有输入分组名。");
                    return;
                }
                if (CommonUtil.isIllegalName(text)) {
                    showAlert(getActivity(), "错误", "分组名不合法，仅允许由1-30个中文、字母、数字和下划线组成。");
                    return;
                }
                IDocClassInteract docClassInteract = InteractStrategy.getInstance().getDocClassInteract(getContext());
                ProgressHandler.process(getContext(), "重命名分组中...", true,
                    docClassInteract.updateDocClass(new DocClass(docClass.getId(), text.trim())), new InteractInterface<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            docClass.setName(text.trim());
                            showToast(getActivity(), "分组 \"" + text.trim() + "\" 修改成功");
                            m_docClassAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(getContext(), "错误", message);
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                        }
                    }
                );
            },
            "返回", null
        );
    }

    /**
     * 删除分组
     */
    private void ToolDeleteDocClass_Clicked(DocClass docClass) {
        if (m_popup_docclass != null && m_popup_docclass.isShowing())
            m_popup_docclass.dismiss();

        if (docClass == null) {
            showAlert(getActivity(), "错误", "没有选择分组。");
            return;
        }
        if (docClass.getName().equals(DocClass.DEF_DOCCLASS.getName())) {
            showAlert(getActivity(), "错误", "无法删除默认分组。");
            return;
        }

        IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getActivity());
        ProgressHandler.process(getContext(), "获取分组信息中...", true,
            documentInteract.queryDocumentByClassId(docClass.getId()), new InteractInterface<List<Document>>() {
                @Override
                public void onSuccess(List<Document> documents) {
                    if (documents.isEmpty()) {
                        showAlert(getActivity(), "删除", "是否删除分组 \"" + docClass.getName() + "\"？",
                            "删除", (d, w) -> onDeleteDocClass(docClass),
                            "取消", null
                        );
                    } else {
                        showAlert(getActivity(), "删除", "分组 \"" + docClass.getName() + "\" 有相关联的 " + documents.size() + " 条文档，是否同时删除？该操作不会删除本地文件。",
                            "删除分组及文档记录", (d, w) -> onDeleteDocClass(docClass, false),
                            "删除分组并修改为默认分组", (d, w) -> onDeleteDocClass(docClass, true),
                            "取消", null
                        );
                    }
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    private void onDeleteDocClass(DocClass docClass) {
        onDeleteDocClass(docClass, true);
    }

    private void onDeleteDocClass(DocClass docClass, boolean isToDefault) {
        IDocClassInteract docClassInteract = InteractStrategy.getInstance().getDocClassInteract(getContext());
        ProgressHandler.process(getContext(), "删除分组中...", true,
            docClassInteract.deleteDocClass(docClass.getId(), isToDefault), new InteractInterface<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    initData(); // 文档也可能删除，需要全部加载
                    showToast(getContext(), "分组删除成功");
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    // endregion

    // region Share

    /**
     * 导入资料
     */
    private void ToolbarImportFile_Clicked() {
        if (m_docClassAdapter.getCurrentIndex() == -1) {
            showAlert(getActivity(), "错误", "未选择文档分组，无法导入。");
            return;
        }
        DocClass docClass = m_docClassAdapter.getCurrentItem();

        List<FileItem> importedDocuments = new ArrayList<>();
        FileImportDialog importDialog = new FileImportDialog(getActivity(), importedDocuments);
        importDialog.setOnFinishScanListener(() -> {
            List<FileItem> fileItems = new ArrayList<>();
            for (FileItem fileItem : importedDocuments)
                if (fileItem.getTag() == FileItem.CHECKED)
                    fileItems.add(fileItem);
            IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getContext());
            final ProgressDialog[] progressDialog = new ProgressDialog[1]; // 上传等待框

            int[] complete = new int[]{0}; // 完成的个数
            CompositeDisposable compositeDisposable = new CompositeDisposable(); // 上传的每一个文件
            List<Document> uploaded = new ArrayList<>(); // 上传成功的文件

            // 订阅 所有文件的上传进度
            Disposable disposable = Observable.create((ObservableEmitter<MessageVO<Document>> emitter) -> {
                for (FileItem f : fileItems) {
                    Document newDocument = new Document(-1, f.getFilePath(), docClass); // 完整路径

                    // 每一个文件的 Observable
                    Disposable d = documentInteract.insertDocument(newDocument).subscribe(
                        (MessageVO<Boolean> msg) -> {
                            if (msg.isSuccess())
                                emitter.onNext(new MessageVO<>(newDocument)); // 上传成功一个
                        }, throwable -> {
                            if (throwable instanceof HttpException) {
                                ResponseBody responseBody = ((HttpException) throwable).response().errorBody();
                                if (responseBody != null) {
                                    ResponseDTO resp = new Gson().fromJson(responseBody.string(), ResponseDTO.class);
                                    emitter.onNext(new MessageVO<>(false, resp.getMessage())); // 上传失败一个
                                } else
                                    emitter.onNext(new MessageVO<>(false, "File Upload Failed")); // 上传失败一个
                            } else {
                                throwable.printStackTrace();
                                emitter.onError(throwable); // 网络出错
                            }
                        });
                    compositeDisposable.add(d);
                }
            }).subscribe((MessageVO<Document> msg) -> { // msg 可能为 false
                // 每上传得到一次响应 onNext
                complete[0]++;
                progressDialog[0].setMessage(String.format(Locale.CHINA, "上传 %d/%d 个文件中...", complete[0], fileItems.size()));
                if (msg.isSuccess()) // 未上传完，添加上传成功到 uploaded
                    uploaded.add(msg.getData());

                if (complete[0] >= fileItems.size()) { // 所有上传完毕
                    progressDialog[0].dismiss();
                    pageData.documentListItems.addAll(uploaded);
                    pageData.showDocumentList.addAll(uploaded);
                    m_documentAdapter.notifyDataSetChanged();
                    m_txt_document_header.setText(String.format(Locale.CHINA, "%s (共 %d 项)", docClass.getName(), pageData.showDocumentList.size()));
                    showToast(getContext(), "上传成功 " + uploaded.size() + " 个文件。");
                }
            }, (throwable) -> { // 网络出错，暂停所有
                progressDialog[0].dismiss();
                showAlert(getContext(), "错误", "网络出错：" + throwable.getMessage());
            });

            progressDialog[0] = showProgress(getContext(),
                String.format(Locale.CHINA, "上传 0/%d 个文件中...", fileItems.size()), true,
                (v) -> {
                    disposable.dispose();
                    compositeDisposable.dispose();
                }
            );
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
    private void ToolbarShareWholeDocClassDoc_Clicked(DocClass docClass) {
        if (m_popup_docclass != null && m_popup_docclass.isShowing())
            m_popup_docclass.dismiss();

        if (docClass == null) {
            showAlert(getActivity(), "错误", "没有选择分组。");
            return;
        }
        if (!AuthManager.getInstance().isLogin()) {
            showToast(getContext(), "未登录");
            return;
        }

        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        showAlert(getContext(), "共享",
            "确定要共享分组 \"" + docClass.getName() + "\" 内的所有文档 (共 " + filterDocumentByDocClass(docClass, pageData.documentListItems).size() + " 项) 吗？",
            "共享", (d, w) -> {
                d.dismiss();

                // Ex
                Spinner exChooser = new Spinner(getActivity());
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.layout_common_spinner, exTexts);
                exChooser.setAdapter(spinnerAdapter);
                exChooser.setSelection(Arrays.asList(exTexts).indexOf("1个小时"), true);

                showAlert(getActivity(),
                    "文件共享时间", exChooser,
                    "确定", (d1, w1) -> {
                        int exp = exTextNumbers.get(exTexts[exChooser.getSelectedItemPosition()]);
                        exp *= 3600;

                        ProgressDialog progressDialog = showProgress(getActivity(), "生成共享码...", false, null);

                        ShareCodeNetInteract shareCodeNetInteract = new ShareCodeNetInteract();
                        ProgressHandler.process(getContext(), "生成共享码...", true,
                            shareCodeNetInteract.newShareCode(docClass, exp), new InteractInterface<String>() {
                                @Override
                                public void onSuccess(String shareCode) {
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
                                    showAlert(getActivity(), "共享二维码", qrCodeImageView,
                                        "复制共享码", (d2, w2) -> {
                                            if (CommonUtil.copyText(getContext(), shareCode))
                                                showToast(getContext(), "共享码：" + shareCode + " 复制成功");
                                        },
                                        "返回", null
                                    );
                                }

                                @Override
                                public void onError(String message) {
                                    showAlert(getActivity(), "错误", message);
                                }

                                @Override
                                public void onFailed(Throwable throwable) {
                                    showAlert(getActivity(), "错误", "网络错误：" + throwable.getMessage());
                                }
                            }
                        );
                    },
                    "取消", null
                );

            }, "取消", null
        );
    }

    /*
        本地文件上传服务器 -> 文件路径名不变，增加 uuid
        本地删除文件，下载 -> 直接下载到默认路径，更新服务器文件路径名
        共享 -> redis 存储，获取共享码 -> 扫码 -> 组成 url 下载
     */

    /**
     * 扫描共享码
     */
    private void scanShareCodeQrCode() {
        showAlert(getContext(), "获取共享文件", "请选择操作：",
            "扫描二维码", (d, w) -> QRCodeManager.getInstance()
                .with(getActivity())
                .scanningQRCode(new OnQRCodeScanCallback() {

                    public void onError(Throwable throwable) {
                        showToast(getActivity(), throwable.getMessage());
                    }

                    public void onCancel() {
                        showToast(getActivity(), "操作已取消");
                    }

                    @Override
                    public void onCompleted(String shareCode) {
                        if (shareCode.isEmpty())
                            showAlert(getContext(), "错误", "共享码错误。");
                        else
                            onGetScContent(shareCode);
                    }
                }),
            "直接输入共享码", (d, w) -> showInputDialog(getContext(), "获取共享文件", "", "请输入共享码", 1,
                "确定", (d2, w2, shareCode) -> {
                    if (shareCode.isEmpty())
                        showAlert(getContext(), "错误", "共享码错误。");
                    else
                        onGetScContent(shareCode);
                },
                "取消", null
            ),
            "取消", null
        );
    }

    /**
     * 获取共享文件
     */
    private void onGetScContent(String shareCode) {
        ShareCodeNetInteract shareCodeNetInteract = new ShareCodeNetInteract();
        ProgressHandler.process(getContext(), "加载共享码内容中...", true,
            shareCodeNetInteract.getShareCodeContents(shareCode), new InteractInterface<List<Document>>() {
                @Override
                public void onSuccess(List<Document> data) {
                    StringBuilder hint = new StringBuilder();
                    for (Document document : data)
                        hint.append(document.getBaseFilename()).append("\n");

                    showAlert(getContext(), "共享", "该共享码包含了 " + data.size() + " 个文件\n" + hint + "是否下载？",
                        "下载", (d, v) -> {
                            String filename = (data.size() == 1) ? data.get(0).getBaseFilename() : data.get(0).getBaseFilename() + "等" + data.size() + "个文件.zip";
                            File[] newFile = new File[]{new File(AppPathUtil.getDownloadDir(), filename)};
                            if (newFile[0].exists()) {
                                showAlert(getContext(), "下载文件", "文件 \"" + shareCode + "\" 已存在，是否覆盖？",
                                    "覆盖", (d2, w2) -> {
                                        AppPathUtil.deleteFile(newFile[0].getAbsolutePath());
                                        onDownload(newFile[0], ServerUrl.getShareCodeUrl(shareCode), false);
                                    },
                                    "重命名", (d2, w2) -> {
                                        newFile[0] = new File(AppPathUtil.getDownloadDir(), shareCode + " (1)");
                                        onDownload(newFile[0], ServerUrl.getShareCodeUrl(shareCode), false);
                                    }
                                );
                            } else {
                                onDownload(newFile[0], ServerUrl.getShareCodeUrl(shareCode), false);
                            }
                        },
                        "取消", null
                    );
                }

                @Override
                public void onError(String message) {
                    showAlert(getContext(), "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                }
            });
    }

    // endregion

    /**
     * Popup / List 打开文档
     */
    private void OpenDocument(Document document) {
        if (m_popup_document != null && m_popup_document.isShowing())
            m_popup_document.dismiss();

        showAlert(getContext(), "打开文档", "是否打开文档 \"" + document.getBaseFilename() + "\"？",
            "打开", (d, w) -> {

                File file = new File(document.getFilename());

                // String path = AppPathUtil.getFilePathByUri(getContext(), Uri.fromFile(file));
                Log.i("", "document.getFilename(): " + document.getFilename());
                Log.i("", "file: " + file.getAbsolutePath());
                Log.i("", "Uri: " + Uri.fromFile(file));
                // Log.i("", "path: " + path);

                /*
                    I/: document.getFilename(): /storage/emulated/0/Biji/NoteFile/5555255512125.docx
                    I/: file: /storage/emulated/0/Biji/NoteFile/5555255512125.docx
                    I/: Uri: file:///storage/emulated/0/Biji/NoteFile/5555255512125.docx
                    I/: path: /storage/emulated/0/Biji/NoteFile/5555255512125.docx
                 */

                if (document.isLocalFile()) { // 本地文件
                    if (file.exists()) { // 打开文件
                        if (!DocService.openFile(getActivity(), file)) {
                            showAlert(getContext(), "错误", "打开文件错误，文件格式不支持。");
                        }
                    } else { // 文件不存在
                        showAlert(getContext(), "打开文档", "文件 \"" + document.getBaseFilename() + "\" 不存在，是否删除记录？",
                            "删除", (d1, w1) -> {
                                IDocumentInteract documentInteract = InteractStrategy.getInstance().getDocumentInteract(getContext());
                                ProgressHandler.process(getContext(), "删除记录...", true,
                                    documentInteract.deleteDocument(document.getId()), new InteractInterface<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean data) {
                                            pageData.documentListItems.remove(document);
                                            pageData.showDocumentList.remove(document);
                                            m_documentAdapter.notifyDataSetChanged();
                                            showToast(getContext(), "记录 \"" + document.getBaseFilename() + "\" 删除成功");
                                        }

                                        public void onError(String message) {
                                            showAlert(getContext(), "错误", message);
                                        } // 应该不会出现

                                        public void onFailed(Throwable throwable) {
                                            showAlert(getContext(), "错误", "网络错误：" + throwable.getMessage());
                                        }
                                    }
                                );
                            },
                            "取消", null
                        );
                    }
                } else { // 远程文件
                    showAlert(getContext(), "错误", "文档 \"" + document.getBaseFilename() + "\" 不是本地文件，是否下载？",
                        "下载", (d1, w1) -> {
                            File[] newFile = new File[]{new File(AppPathUtil.getDownloadDir(), document.getBaseFilename())};
                            if (newFile[0].exists()) {
                                showAlert(getContext(), "下载文件", "文件 \"" + document.getBaseFilename() + "\" 已存在，是否覆盖？",
                                    "覆盖", (d2, w2) -> {
                                        AppPathUtil.deleteFile(newFile[0].getAbsolutePath());
                                        onDownload(newFile[0], ServerUrl.getRawUrl(document.getUuid()), true);
                                    },
                                    "重命名", (d2, w2) -> {
                                        newFile[0] = new File(AppPathUtil.getDownloadDir(), document.getBaseFilename() + " (1)");
                                        onDownload(newFile[0], ServerUrl.getRawUrl(document.getUuid()), true);
                                    }
                                );
                            } else {
                                onDownload(newFile[0], ServerUrl.getRawUrl(document.getUuid()), true);
                            }
                        }, "取消", null
                    );
                }
            },
            "取消", null
        );
    }

    /**
     * 下载文件，用户判断完文件名后
     */
    private void onDownload(File file, String url, boolean hasToken) {
        ProgressHandler.download(getContext(), "下载中...", file, url, hasToken, new ProgressHandler.OnDownloadListener() {
            @Override
            public void onFailed(String message) {
                showAlert(getContext(), "错误", message);
            }

            @Override
            public void onComplete() {
                showToast(getContext(), "下载完成");
                new DownloadedDao(getContext()).InsertDownloadItem(file.getAbsolutePath(), new Date()); // 本地路径
            }
        });
    }
}
