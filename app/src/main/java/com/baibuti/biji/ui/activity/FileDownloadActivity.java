package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baibuti.biji.R;
import com.baibuti.biji.model.dao.local.DownloadedDao;
import com.baibuti.biji.model.po.DownloadItem;
import com.baibuti.biji.service.doc.DocService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DownloadItemAdapter;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;
import com.baibuti.biji.util.otherUtil.LayoutUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadActivity extends AppCompatActivity implements IContextHelper {

    private DownloadItemAdapter m_downloadItemAdapter;

    @BindView(R.id.id_download_srl)
    SwipeRefreshLayout m_srl;

    private Dialog m_LongClickItemPopupMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("文件下载");

        RecyclerViewEmptySupport itemListView = findViewById(R.id.id_download_recycler_view);
        itemListView.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemListView.setLayoutManager(layoutManager);
        itemListView.setEmptyView(findViewById(R.id.id_download_empty_view));

        m_downloadItemAdapter = new DownloadItemAdapter(this);
        itemListView.setAdapter(m_downloadItemAdapter);

        m_downloadItemAdapter.setDownloadItemList(new ArrayList<>());
        m_downloadItemAdapter.setOnDownloadItemClickListener(this::DocumentListItem_Clicked);
        m_downloadItemAdapter.setOnDownloadItemLongClickListener(this::DocumentListItem_LongClicked);
        m_srl.setOnRefreshListener(this::initData);
        m_srl.setColorSchemeResources(R.color.colorPrimary);

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_act_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_DownloadClear)
            ActionClear_Clicked();
        else if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    /**
     * 初始化数据 / srl
     */
    private void initData() {
        DownloadedDao downloadedDao = new DownloadedDao(this);
        List<DownloadItem> downloadItemList = downloadedDao.GetAllDownloadedItem();
        Collections.sort(downloadItemList);
        m_downloadItemAdapter.setDownloadItemList(downloadItemList);
        m_downloadItemAdapter.notifyDataSetChanged();

        setTitle("文件下载 (共 " + m_downloadItemAdapter.getDownloadItemList().size() + " 项)");
        m_srl.setEnabled(true);
        m_srl.setRefreshing(false);
    }

    /**
     * ListView 单击项
     */
    private void DocumentListItem_Clicked(DownloadItem downloadItem) {
        showAlert(this, "打开文档", "是否打开下载的文档 \"" + downloadItem.getBaseFilename() + "\"？",
            "打开", (d, w) -> {

                File file = new File(downloadItem.getFilename());

                if (!file.exists()) {
                    showAlert(this, "打开", "文档 \"" + downloadItem.getBaseFilename() + "\" 不存在，是否删除下载记录？",
                        "删除", (d1, w1) -> {
                            DownloadedDao downloadedDao = new DownloadedDao(this);
                            if (downloadedDao.DeleteDownloadItem(downloadItem.getFilename())) {
                                m_downloadItemAdapter.getDownloadItemList().remove(downloadItem);
                                m_downloadItemAdapter.notifyDataSetChanged();
                            }
                        }, "取消", null
                    );
                } else if (!DocService.openFile(this, file))
                    showAlert(this, "错误", "打开文件错误，文件格式不支持。");
            },
            "取消", null
        );
    }

    /**
     * ListView 长按项弹出菜单
     */
    private void DocumentListItem_LongClicked(DownloadItem downloadItem) {
        m_LongClickItemPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_LongClickItemPopupMenu, R.layout.popup_download_act_long_click_item);

        ((TextView) root.findViewById(R.id.id_download_popup_curr)).setText(String.format(Locale.CHINA, "当前选中项: %s", downloadItem.getBaseFilename()));
        root.findViewById(R.id.id_download_popup_delete_file).setOnClickListener((v) -> PopupDeleteItemAndFile_Clicked(downloadItem));
        root.findViewById(R.id.id_download_popup_delete_all).setOnClickListener((v) -> ActionClear_Clicked());
        root.findViewById(R.id.id_download_popup_cancel).setOnClickListener((v) -> m_LongClickItemPopupMenu.dismiss());

        m_LongClickItemPopupMenu.show();
    }

    /**
     * Popup 删除
     */
    private void PopupDeleteItemAndFile_Clicked(DownloadItem downloadItem) {
        m_LongClickItemPopupMenu.dismiss();
        showAlert(this, "删除", "是否删除下载的文件 \"" + downloadItem.getBaseFilename() + "\"？",
            "删除", (d, w) -> {
                if (AppPathUtil.deleteFile(downloadItem.getFilename())) {
                    DownloadedDao downloadedDao = new DownloadedDao(this);
                    if (downloadedDao.DeleteDownloadItem(downloadItem.getFilename())) {
                        initData();
                        showToast(this, "文件 \"" + downloadItem.getBaseFilename() + "\" 删除成功");
                        return;
                    }
                }
                showToast(this, "文件 \"" + downloadItem.getBaseFilename() + "\" 删除失败");
            },
            "取消", null
        );
    }

    /**
     * ActionBar / Popup 清空 删除
     */
    private void ActionClear_Clicked() {
        if (m_LongClickItemPopupMenu != null && m_LongClickItemPopupMenu.isShowing())
            m_LongClickItemPopupMenu.dismiss();

        showAlert(this, "清空", "是否删除所有下载的文件？",
            "删除", (d, w) -> {
                ProgressDialog progressDialog = showProgress(this, "删除文件中...", false, null);
                DownloadedDao downloadedDao = new DownloadedDao(this);
                downloadedDao.DeleteAllDownloadItem();
                progressDialog.dismiss();
                initData();
            },
            "取消", null
        );
    }
}
