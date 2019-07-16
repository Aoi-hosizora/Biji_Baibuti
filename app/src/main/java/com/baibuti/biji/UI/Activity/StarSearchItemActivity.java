package com.baibuti.biji.UI.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baibuti.biji.Data.Adapters.SearchItemAdapter;
import com.baibuti.biji.Data.DB.SearchItemDao;
import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.View.SpacesItemDecoration;
import com.baibuti.biji.UI.Widget.RecyclerViewEmptySupport;
import com.baibuti.biji.Utils.CommonUtil;

import java.util.ArrayList;

public class StarSearchItemActivity extends AppCompatActivity implements View.OnClickListener, IShowLog {


    private Menu m_menu;
    private com.wyt.searchbox.SearchFragment searchFragment;

    private RecyclerViewEmptySupport m_StarListView;
    private SearchItem m_LongClickedSearchItem;

    private Dialog m_LongClickItemPopupMenu;
    private SwipeRefreshLayout m_SwipeRefresh;

    private ArrayList<SearchItem> searchItemList;
    private SearchItemAdapter searchItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starsearchitem);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setTitle(R.string.StarSearchItemAct_Title);

        m_StarListView = findViewById(R.id.id_StarSearchItemActivity_StarListView);

        initListView();
        initSearchFrag();
        initSRL();
    }

    /**
     * 初始化 ListView
     */
    private void initListView() {
        // EmptyView:
        View EmptyView = findViewById(R.id.id_StarSearchItemActivity_StarListView_EmptyView);
        m_StarListView.setEmptyView(EmptyView);

        // LayoutMgr:
        m_StarListView.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        m_StarListView.setLayoutManager(layoutManager);

        searchItemAdapter = new SearchItemAdapter();
        searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                SearchItem_Click(searchItem);
            }

            @Override
            public void onMoreClick(View view) {
                // Nothing
            }
        });

        searchItemAdapter.setOnItemLongClickListener(new SearchItemAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, SearchItem searchItem) {
                SearchItem_LongClick(searchItem);
            }
        });

        refreshListData();
        refreshListView();
    }

    private void initSRL() {
        m_SwipeRefresh = findViewById(R.id.id_StarSearchItemActivity_Srl);
        m_SwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        // m_SwipeRefresh.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN);
        m_SwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshListData();
                        refreshListView();
                        m_SwipeRefresh.setRefreshing(false);
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Popup
            case R.id.id_SSIActivity_PopupMenu_CancelStar:
                if (m_LongClickedSearchItem != null)
                    SearchItem_CancelStarClick(m_LongClickedSearchItem);
                m_LongClickItemPopupMenu.dismiss();
                break;
            case R.id.id_SSIActivity_PopupMenu_Cancel:
                m_LongClickItemPopupMenu.dismiss();
                break;
        }
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "StarSearchItemActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    /**
     * 点击顶部菜单项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        CommonUtil.closeSoftKeyInput(this);

        switch (item.getItemId()) {
            case android.R.id.home:
                backToLastCmd();
                break;
            case R.id.action_FindSearchStar:
                searchFragment.show(getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取 Menu 实例
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_ssiact, menu);
        this.m_menu = menu;
        return true;
    }

    /**
     * 初始化搜索框
     */
    private void initSearchFrag() {
        // 添加搜索框
        searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        searchFragment.setAllowReturnTransitionOverlap(true);
        searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {

            @Override
            public void OnSearchClick(String keyword) {
                try {
                    if (!keyword.isEmpty()) {
                        Toast.makeText(StarSearchItemActivity.this, keyword, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新列表内容
     */
    private void refreshListView() {
        searchItemAdapter.setSearchItems(searchItemList);
        m_StarListView.setAdapter(searchItemAdapter);
        searchItemAdapter.notifyDataSetChanged();
    }

    /**
     * 更新列表数据为所有新收藏
     */
    private void refreshListData() {
        SearchItemDao searchItemDao = new SearchItemDao(this);
        searchItemList = searchItemDao.queryAllStarSearchItem();
        setTitle(String.format(getString(R.string.StarSearchItemAct_Title), searchItemList.size()));
    }

    /**
     * 获取当前所有收藏数
     * @return
     */
    private int getSearchItemsCnt() {
        SearchItemDao searchItemDao = new SearchItemDao(this);
        return searchItemDao.queryAllStarSearchItem().size();
    }

    /**
     * 返回上一次操作
     * 判断是返回上一活动还是取消搜索
     */
    private void backToLastCmd() {
        if (getTitle().equals(String.format(getString(R.string.StarSearchItemAct_Title), getSearchItemsCnt())))
            finish();
        else {
            refreshListData();
            refreshListView();
        }
    }

    /**
     * 使用浏览器打开链接
     * TODO ********
     * @param searchItem
     */
    private void SearchItem_Click(SearchItem searchItem) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(searchItem.getUrl());
        intent.setData(content_url);
        startActivity(intent);
    }

    /**
     * 长按弹出选项
     * @param searchItem
     */
    private void SearchItem_LongClick(SearchItem searchItem) {
        // 记录长按项目
        m_LongClickedSearchItem = searchItem;
        ShowItemLongClickPopupMenu(searchItem);
    }

    /**
     * 显示长按菜单
     */
    private void ShowItemLongClickPopupMenu(SearchItem searchItem) {
        m_LongClickItemPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.dialog_starsearchitem_bottompopupmenu, null);

        //初始化视图
        root.findViewById(R.id.id_SSIActivity_PopupMenu_CancelStar).setOnClickListener(this);
        root.findViewById(R.id.id_SSIActivity_PopupMenu_Cancel).setOnClickListener(this);

        m_LongClickItemPopupMenu.setContentView(root);
        Window dialogWindow = m_LongClickItemPopupMenu.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度

        dialogWindow.setAttributes(lp);

        m_LongClickItemPopupMenu.show();
    }

    /**
     * 取消收藏
     * @param searchItem
     */
    private void SearchItem_CancelStarClick(SearchItem searchItem) {
        SearchItemDao searchItemDao = new SearchItemDao(this);

        if (searchItemDao.deleteStarSearchItem(searchItem) != -1)
            Toast.makeText(this, String.format(getString(R.string.SearchFrag_CancelStarSuccess), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, String.format(getString(R.string.SearchFrag_CancelStarFailed), searchItem.getTitle()), Toast.LENGTH_SHORT).show();

        refreshListData();
        refreshListView();
    }
}
