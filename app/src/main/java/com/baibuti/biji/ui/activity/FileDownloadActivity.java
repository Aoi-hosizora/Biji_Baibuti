package com.baibuti.biji.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.baibuti.biji.R;
import com.baibuti.biji.model.dao.local.DownloadedDao;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.service.doc.DocService;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.DocumentAdapter;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.filePathUtil.AppPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileDownloadActivity extends AppCompatActivity implements IContextHelper {

    private DocumentAdapter m_documentAdapter;

    @BindView(R.id.id_download_srl)
    SwipeRefreshLayout m_srl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded);
        ButterKnife.bind(this);

        m_documentAdapter = new DocumentAdapter(this);
        RecyclerViewEmptySupport itemListView = findViewById(R.id.id_download_recycler_view);
        itemListView.setEmptyView(findViewById(R.id.id_download_empty_view));
        itemListView.setAdapter(m_documentAdapter);

        m_documentAdapter.setDocumentList(new ArrayList<>());
        m_documentAdapter.setOnDocumentClickListener(this::DocumentListItem_Clicked);
        m_documentAdapter.setOnDocumentLongClickListener(this::DocumentListItem_LongClicked);
        m_srl.setOnRefreshListener(this::initData);

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_act_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_DownloadSearch:
                ActionSearch_Clicked();
                break;
            case R.id.action_DownloadClear:
                ActionClear_Clicked();
                break;
        }
        return true;
    }

    /**
     * 初始化数据 / srl
     */
    private void initData() {
        DownloadedDao downloadedDao = new DownloadedDao(this);
        List<Document> documentList = downloadedDao.GetAllDownloadedItem();
        Collections.sort(documentList);
        m_documentAdapter.setDocumentList(documentList);
        m_documentAdapter.notifyDataSetChanged();
        if (m_srl.isRefreshing())
            m_srl.setRefreshing(false);
    }

    /**
     * ListView 单击项
     */
    private void DocumentListItem_Clicked(Document document) {
        showAlert(this, "打开文档", "是否打开下载的文档 \"" + document.getBaseFilename() + "\"？",
            "打开", (d, w) -> {

                File file = new File(document.getFilename());
                String path = AppPathUtil.getFilePathByUri(this, Uri.fromFile(file));

                if (!file.exists() || path == null) {
                    showAlert(this, "错误", "文档 \"" + document.getBaseFilename() + "\" 不存在，是否删除下载记录？",
                        "删除", (d1, w1) -> {
                            DownloadedDao downloadedDao = new DownloadedDao(this);
                            if (downloadedDao.DeleteDownloadItem(document.getFilename())) {
                                m_documentAdapter.getDocumentList().remove(document);
                                m_documentAdapter.notifyDataSetChanged();
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
     * ListView 长按项
     */
    private void DocumentListItem_LongClicked(Document document) {
        showAlert(this, "打开文档", "是否删除下载的文档 \"" + document.getBaseFilename() + "\"？",
            "删除记录", (d, w) -> {
                DownloadedDao downloadedDao = new DownloadedDao(this);
                if (downloadedDao.DeleteDownloadItem(document.getFilename())) {
                    m_documentAdapter.getDocumentList().remove(document);
                    m_documentAdapter.notifyDataSetChanged();
                }
            },
            "取消", null
        );
    }

    /**
     * ActionBar 搜索
     */
    private void ActionSearch_Clicked() {

    }

    /**
     * ActionBar 清空
     */
    private void ActionClear_Clicked() {

    }
}
