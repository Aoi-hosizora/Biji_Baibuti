package com.baibuti.biji.ui.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.model.dto.ServerException;
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

    @BindView(R.id.id_StarSearchItemActivity_StarListView)
    RecyclerViewEmptySupport m_list_star;

    @BindView(R.id.id_StarSearchItemActivity_Srl)
    SwipeRefreshLayout m_srl;

    // com.wyt.searchbox.SearchFragment.newInstance()
    private com.wyt.searchbox.SearchFragment m_searchFragment;

    private Dialog m_LongClickItemPopupMenu;

    private SearchItemAdapter searchItemAdapter;

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
                if (!keyword.trim().isEmpty()) {
                    FindSearchItem_Click(keyword.trim());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Srl
        m_srl.setColorSchemeResources(R.color.colorPrimary);
        m_srl.setOnRefreshListener(() -> refreshListData(null));

        ////////////
        // EmptyView:
        m_list_star.setEmptyView(findViewById(R.id.id_StarSearchItemActivity_StarListView_EmptyView));

        // LayoutMgr:
        m_list_star.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_list_star.setLayoutManager(layoutManager);

        searchItemAdapter = new SearchItemAdapter(this);
        searchItemAdapter.setSearchItems(pageData.currentList);
        searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                showBrowser(SearchItemActivity.this, new String[] { searchItem.getUrl() });
            }

            @Override
            public void onMoreClick(View view) { }
        });
        searchItemAdapter.setOnItemLongClickListener((View view, SearchItem searchItem) -> ListItem_LongClicked(searchItem));

        updateTitle();
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
     * 加载数据 并更新页面状态
     * @param searchItems 加载数据库或者搜索
     */
    private void refreshListData(List<SearchItem> searchItems) {

        ProgressDialog progressDialog = showProgress(this, "加载中...", false, null);

        if (searchItems == null || searchItems.isEmpty()) { // 加载数据库内的

            ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(this);
            try {
                pageData.currentList = searchItemDao.queryAllSearchItems();
                searchItemAdapter.notifyDataSetChanged();
                pageData.pageState = PageData.PageState.SEARCHING;
            } catch (ServerException ex) {
                showAlert(this, "错误", ex.getMessage());
            }
        } else { // 加载搜索结果
            pageData.currentList = searchItems;
            searchItemAdapter.notifyDataSetChanged();
            pageData.pageState = PageData.PageState.NORMAL;
        }

        // 更新标题
        updateTitle();

        m_srl.setRefreshing(false);
        if (progressDialog.isShowing())
            progressDialog.cancel();
    }

    /**
     * 更新标题
     */
    private void updateTitle() {
        if (pageData.pageState == PageData.PageState.SEARCHING)
            setTitle(String.format(Locale.CHINA, "\"%s\" 的搜索结果 (共 %d 项)", pageData.searchKeyWord, pageData.currentList.size()));
        else
            setTitle(String.format(Locale.CHINA, "已收藏搜索结果 (共 %d 项)", pageData.currentList.size()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_FindSearchStar:
                FindSearch_Clicked();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * 菜单 搜索
     */
    private void FindSearch_Clicked() {
        m_searchFragment.show(getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
    }

    /**
     * 点击搜索内容
     */
    private void FindSearchItem_Click(String searchStr) {
        pageData.searchKeyWord = searchStr;
        ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(this);

        try {
            List<SearchItem> searchItems = searchItemDao.queryAllSearchItems();
            refreshListData(SearchUtil.getSearchItems(searchItems.toArray(new SearchItem[0]), searchStr));
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(this, "错误", ex.getMessage());
        }
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
        ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(this);
        try {
             if (!searchItemDao.deleteSearchItem(searchItem.getId())) {
                 Toast.makeText(SearchItemActivity.this, String.format("取消收藏 \"%s\" 失败", searchItem.getTitle()), Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(SearchItemActivity.this, String.format("取消收藏 \"%s\" 成功", searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                 pageData.currentList.remove(searchItem);
                 searchItemAdapter.notifyDataSetChanged();
             }
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(this, "错误", ex.getMessage());
        }
    }

    /**
     * 取消全部收藏
     */
    private void SearchItem_CancelAllStarClick() {
        showAlert(this,
            "提示", "确定要取消全部收藏吗？",
            "确定", (d, w) -> {
                ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(this);
                try {
                    int deleteLen = searchItemDao.deleteSearchItems(pageData.currentList);
                    if (deleteLen == pageData.currentList.size()) {
                        showToast(this, String.format(Locale.CHINA, "成功取消收藏 %d 项", deleteLen));
                        pageData.currentList.clear();
                        searchItemAdapter.notifyDataSetChanged();
                    } else {
                        showToast(this, "取消全部收藏失败");
                        // 返回全部收藏
                        refreshListData(null);
                    }
                } catch (ServerException ex) {
                    ex.printStackTrace();
                    showAlert(this, "错误", ex.getMessage());
                }
            },
            "取消", null
        );
    }
}
