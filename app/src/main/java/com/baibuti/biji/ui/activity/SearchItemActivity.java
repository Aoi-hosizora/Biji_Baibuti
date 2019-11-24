package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.common.interact.InteractInterface;
import com.baibuti.biji.common.interact.InteractStrategy;
import com.baibuti.biji.common.interact.ProgressHandler;
import com.baibuti.biji.common.interact.contract.ISearchItemInteract;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.SearchItemAdapter;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.util.imgTextUtil.SearchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchItemActivity extends AppCompatActivity implements IContextHelper {

    @BindView(R.id.id_StarSearchItemActivity_Srl)
    SwipeRefreshLayout m_srl;

    private SearchItemAdapter m_searchItemAdapter;

    // com.wyt.searchbox.SearchFragment.newInstance()
    private com.wyt.searchbox.SearchFragment m_searchFragment;

    private Dialog m_LongClickItemPopupMenu;

    /**
     * 存储页面信息
     */
    private PageData pageData = new PageData();

    private static class PageData {

        /**
         * 当前页面状态
         */
        PageState pageState = PageState.NORMAL;

        /**
         * 当前页面的数据
         */
        List<SearchItem> currentList = new ArrayList<>();

        /**
         * 搜索内容
         */
        String searchKeyWord = "";

        enum PageState {
            NORMAL, SEARCHING
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Search Frag
        m_searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        m_searchFragment.setAllowReturnTransitionOverlap(true);
        m_searchFragment.setOnSearchClickListener((keyword) -> {
            try {
                if (keyword.trim().isEmpty())
                    showToast(this, "搜索内容不为空");
                else {
                    searchData(keyword);
                    m_srl.setEnabled(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Srl
        m_srl.setColorSchemeResources(R.color.colorPrimary);
        m_srl.setOnRefreshListener(this::initData);

        ////////////
        // EmptyView:
        RecyclerViewEmptySupport m_list_star = findViewById(R.id.id_StarSearchItemActivity_StarListView);
        m_list_star.setEmptyView(findViewById(R.id.id_StarSearchItemActivity_StarListView_EmptyView));

        // LayoutMgr:
        m_list_star.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_list_star.setLayoutManager(layoutManager);

        m_searchItemAdapter = new SearchItemAdapter(this);
        m_searchItemAdapter.setSearchItems(pageData.currentList);
        m_searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                showAlert(SearchItemActivity.this,
                    "打开", "用浏览器打开链接 \"" + searchItem.getTitle() + "\" ？",
                    "打开", (v, d) -> showBrowser(SearchItemActivity.this, new String[] { searchItem.getUrl() }),
                    "取消", null
                );
            }

            @Override
            public void onMoreClick(View view) { }
        });
        m_searchItemAdapter.setOnItemLongClickListener((View view, SearchItem searchItem) -> ListItem_LongClicked(searchItem));

        m_list_star.setAdapter(m_searchItemAdapter);

        initData();
    }

    @Override
    public void onBackPressed() {
        if (pageData.pageState == PageData.PageState.SEARCHING)
            initData();
        else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_FindSearchStar:
                FindSearch_Clicked();
                break;
            case R.id.action_ClearSearchStar:
                SearchItem_CancelAllStarClick();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     * 新建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.star_act_action, menu);
        return true;
    }

    /**
     * 加载数据
     */
    private void initData() {
        ISearchItemInteract searchInteract = InteractStrategy.getInstance().getSearchInteract(this);
        ProgressHandler.process(this, "刷新中...", true,
            searchInteract.queryAllSearchItems(), new InteractInterface<List<SearchItem>>() {
                @Override
                public void onSuccess(List<SearchItem> data) {
                    pageData.currentList.clear();
                    pageData.currentList.addAll(data);
                    for (SearchItem searchItem : pageData.currentList)
                        searchItem.setStar(true);
                    m_searchItemAdapter.notifyDataSetChanged();

                    pageData.pageState = PageData.PageState.NORMAL;
                    m_srl.setEnabled(true);
                    m_srl.setRefreshing(false);
                    updateTitle();
                }

                @Override
                public void onError(String message) {
                    m_srl.setRefreshing(false);
                    showAlert(SearchItemActivity.this, "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    m_srl.setRefreshing(false);
                    showAlert(SearchItemActivity.this, "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 更新标题
     */
    private void updateTitle() {
        if (pageData.pageState == PageData.PageState.SEARCHING)
            setTitle(String.format(Locale.CHINA, "\"%s\" 的搜索结果 (共 %d 项)", pageData.searchKeyWord, pageData.currentList.size()));
        else
            setTitle(String.format(Locale.CHINA, "收藏 (共 %d 项)", pageData.currentList.size()));
    }

    private void searchData(String keyword) {
        ISearchItemInteract searchInteract = InteractStrategy.getInstance().getSearchInteract(this);
        ProgressHandler.process(this, "搜索 \"" + keyword + "\" 中...", true,
            searchInteract.queryAllSearchItems(), new InteractInterface<List<SearchItem>>() {
                @Override
                public void onSuccess(List<SearchItem> data) {
                    List<SearchItem> result = SearchUtil.getSearchItems(data.toArray(new SearchItem[0]), keyword);
                    pageData.currentList.clear();
                    pageData.currentList.addAll(result);
                    for (SearchItem searchItem : pageData.currentList)
                        searchItem.setStar(true);
                    m_searchItemAdapter.notifyDataSetChanged();

                    showToast(SearchItemActivity.this, "共找到 " + result.size() + " 项");
                    pageData.searchKeyWord = keyword;
                    pageData.pageState = PageData.PageState.SEARCHING;
                    m_srl.setRefreshing(false);
                    updateTitle();
                }

                @Override
                public void onError(String message) {
                    m_srl.setRefreshing(false);
                    showAlert(SearchItemActivity.this, "错误", message);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    m_srl.setRefreshing(false);
                    showAlert(SearchItemActivity.this, "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 菜单 搜索
     */
    private void FindSearch_Clicked() {
        m_searchFragment.show(getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
    }

    /**
     * 长按项 显示菜单
     * @param searchItem 长按项
     */
    private void ListItem_LongClicked(SearchItem searchItem) {
        m_LongClickItemPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(this, m_LongClickItemPopupMenu, R.layout.popup_star_act_long_click_item);

        root.findViewById(R.id.id_SSIActivity_PopupMenu_CancelStar).setOnClickListener((v) -> SearchItem_CancelStarClick(searchItem));
        root.findViewById(R.id.id_SSIActivity_PopupMenu_CancelAllStar).setOnClickListener((v) -> SearchItem_CancelAllStarClick());
        root.findViewById(R.id.id_SSIActivity_PopupMenu_Cancel).setOnClickListener((v) -> m_LongClickItemPopupMenu.dismiss());

        TextView label = root.findViewById(R.id.id_SSIActivity_PopupMenu_Label);
        label.setText(String.format(Locale.CHINA, "当前选中项: %s", searchItem.getTitle()));

        m_LongClickItemPopupMenu.show();
    }

    /**
     * 取消收藏
     */
    private void SearchItem_CancelStarClick(SearchItem searchItem) {
        m_LongClickItemPopupMenu.dismiss();
        ISearchItemInteract searchInteract = InteractStrategy.getInstance().getSearchInteract(this);
        ProgressHandler.process(this, "取消收藏中...", true,
            searchInteract.deleteSearchItem(searchItem.getId()), new InteractInterface<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    Toast.makeText(SearchItemActivity.this, String.format("取消收藏 \"%s\" 成功", searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                    pageData.currentList.remove(searchItem);
                    m_searchItemAdapter.notifyDataSetChanged();
                    updateTitle();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(SearchItemActivity.this, String.format("取消收藏 \"%s\" 失败", searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Throwable throwable) {
                    showAlert(SearchItemActivity.this, "错误", "网络错误：" + throwable.getMessage());
                }
            }
        );
    }

    /**
     * 取消全部收藏
     */
    private void SearchItem_CancelAllStarClick() {
        if (m_LongClickItemPopupMenu != null && m_LongClickItemPopupMenu.isShowing())
            m_LongClickItemPopupMenu.dismiss();

        showAlert(this, "取消收藏", "确定取消全部收藏吗？", "取消全部收藏", (d, w) -> {
                ISearchItemInteract searchInteract = InteractStrategy.getInstance().getSearchInteract(this);
                ProgressHandler.process(this, "取消收藏中...", true,
                    searchInteract.deleteSearchItems(pageData.currentList), new InteractInterface<Integer>() {
                        @Override
                        public void onSuccess(Integer data) {
                            Toast.makeText(SearchItemActivity.this, String.format(Locale.CHINA, "成功取消收藏 %d 项", data), Toast.LENGTH_SHORT).show();
                            pageData.currentList.clear();
                            m_searchItemAdapter.notifyDataSetChanged();
                            updateTitle();
                        }

                        @Override
                        public void onError(String message) {
                            showAlert(SearchItemActivity.this, "错误", message);
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            showAlert(SearchItemActivity.this, "错误", "网络错误：" + throwable.getMessage());
                        }
                    }
                );
            },
            "返回", null
        );
    }
}
