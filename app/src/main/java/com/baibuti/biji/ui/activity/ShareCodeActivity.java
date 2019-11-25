package com.baibuti.biji.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ImageView;

import com.baibuti.biji.R;
import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.server.ShareCodeNetInteract;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.po.ShareCodeItem;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.ShareCodeAdapter;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.util.otherUtil.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareCodeActivity extends AppCompatActivity implements IContextHelper {

    private ShareCodeAdapter m_adapter;

    @BindView(R.id.id_sc_srl)
    SwipeRefreshLayout m_srl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_code);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("所有共享码");

        initView();
        initData();
    }

    private void initView() {
        RecyclerViewEmptySupport m_list = findViewById(R.id.id_sc_list);
        m_list.setEmptyView(findViewById(R.id.id_sc_empty_view));
        m_list.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_list.setLayoutManager(layoutManager);

        m_adapter = new ShareCodeAdapter(this);
        m_list.setAdapter(m_adapter);

        m_adapter.setShareCodeItems(new ArrayList<>());
        m_adapter.setOnItemClickListener((v, i) -> ShareCodeItem_Clicked(i));
        m_srl.setOnRefreshListener(this::initData);
        m_srl.setColorSchemeResources(R.color.colorPrimary);
    }

    private void initData() {
        ShareCodeNetInteract shareCodeNetInteract = new ShareCodeNetInteract();
        ProgressHandler.process(this, "加载中...", true,
            shareCodeNetInteract.getAllShareCode(), new InteractInterface<List<ShareCodeItem>>() {
                @Override
                public void onSuccess(List<ShareCodeItem> data) {
                    m_adapter.setShareCodeItems(data);
                    m_adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String message) {
                    showAlert(ShareCodeActivity.this, "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(ShareCodeActivity.this, "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    private void ShareCodeItem_Clicked(ShareCodeItem shareCodeItem) {
        StringBuilder hint = new StringBuilder();
        for (Document document : shareCodeItem.getDocuments())
            hint.append(document.getBaseFilename()).append("\n");
        String message = "该共享码包含 " + shareCodeItem.getDocuments().length + " 个文件：\n" + hint.toString();
        showAlert(this, "内容", message,
            "删除共享码", (d, w) -> showAlert(this, "删除", "是否删除共享码 \"" + shareCodeItem.getSc() + "\"？",
                "删除", (d1, w1) -> {
                    ShareCodeNetInteract shareCodeNetInteract = new ShareCodeNetInteract();
                    ProgressHandler.process(ShareCodeActivity.this, "删除中...", true,
                        shareCodeNetInteract.deleteShareCodes(new String[]{shareCodeItem.getSc()}), new InteractInterface<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {
                                showToast(ShareCodeActivity.this, "删除成功");
                                m_adapter.getShareCodeItems().remove(shareCodeItem);
                                m_adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String message) {
                                showAlert(ShareCodeActivity.this, "错误", message);
                            }

                            @Override
                            public void onFailed(Throwable throwable) {
                                showAlert(ShareCodeActivity.this, "错误", "网络错误：" + throwable.getMessage());
                            }
                        }
                    );
                },
                "取消", null
            ),
            "获取二维码", (d, w) -> {
                Bitmap qrCode = CommonUtil.generateQrCode(shareCodeItem.getSc(), 800, Color.BLACK);
                if (qrCode == null) {
                    showAlert(this, "错误", "二维码生成错误。");
                    return;
                }

                ImageView qrCodeImageView = new ImageView(this);
                qrCodeImageView.setImageBitmap(qrCode);
                qrCodeImageView.setMinimumWidth(qrCode.getWidth());
                qrCodeImageView.setMinimumHeight(qrCode.getHeight());
                showAlert(this, "共享二维码", qrCodeImageView,
                    "复制共享码", (d2, w2) -> {
                        if (CommonUtil.copyText(this, shareCodeItem.getSc()))
                            showToast(this, "共享码：" + shareCodeItem.getSc() + " 复制成功");
                    },
                    "返回", null
                );
            },
            "取消", null
        );
    }
}
