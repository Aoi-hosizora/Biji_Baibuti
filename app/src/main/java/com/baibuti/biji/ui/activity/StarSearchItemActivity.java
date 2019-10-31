package com.baibuti.biji.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baibuti.biji.ui.adapter.SearchItemAdapter;
import com.baibuti.biji.model.dao.local.SearchItemDao;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.layoutUtil.PopupMenuUtil;
import com.baibuti.biji.util.strSrchUtil.SearchUtil;

import java.util.ArrayList;

public class StarSearchItemActivity extends AppCompatActivity implements View.OnClickListener {

    // region 定义界面元素 m_menu m_searchFragment m_StarListView m_LongClickItemPopupMenu m_SwipeRefresh

    private Menu m_menu;
    private com.wyt.searchbox.SearchFragment m_searchFragment;

    private RecyclerViewEmptySupport m_StarListView;

    private Dialog m_LongClickItemPopupMenu;
    private SwipeRefreshLayout m_SwipeRefresh;

    private ProgressDialog m_LoadingProgress;

    // endregion 定义界面元素

    // region 定义临时搜索数据 m_LongClickedSearchItem searchItemList searchItemAdapter

    private SearchItem m_LongClickedSearchItem;
    private ArrayList<SearchItem> searchItemList = new ArrayList<SearchItem>();
    private SearchItemAdapter searchItemAdapter;

    /**
     * 搜索记录
     */
    private ArrayList<SearchItem> FindSearchedItemList = null;
    private String FindSearchedItemString = "";

    // endregion 定义临时搜索数据

    // region 定义零碎数据 TIME_SRL_MS

    /**
     * 下拉等待
     */
    private int TIME_SRL_MS = 500;

    // endregion 定义零碎数据

    ///

    // region 初始化界面 onCreate initView initSearchFrag initSRL initListView ShowLogE

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
        m_StarListView = findViewById(R.id.id_StarSearchItemActivity_StarListView);

        m_LoadingProgress = new ProgressDialog(this);
        m_LoadingProgress.setCancelable(false);
        m_LoadingProgress.setMessage(getString(R.string.StarSearchItemAct_BackSearchLoading));

        initSearchFrag();
        initSRL();
        initListView();
    }

    /**
     * 初始化搜索框
     */
    private void initSearchFrag() {
        // 添加搜索框
        m_searchFragment = com.wyt.searchbox.SearchFragment.newInstance();
        m_searchFragment.setAllowReturnTransitionOverlap(true);
        m_searchFragment.setOnSearchClickListener(new com.wyt.searchbox.custom.IOnSearchClickListener() {

            @Override
            public void OnSearchClick(String keyword) {
                try {
                    if (!keyword.trim().isEmpty()) {
                        Find_SearchItem_Click(keyword.trim());
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * 初始化 下拉加载
     */
    private void initSRL() {
        m_SwipeRefresh = findViewById(R.id.id_StarSearchItemActivity_Srl);
        m_SwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        // m_SwipeRefresh.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN);
        m_SwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!m_LoadingProgress.isShowing())
                    m_LoadingProgress.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshListData();
                        refreshListView();
                        m_SwipeRefresh.setRefreshing(false);
                        m_LoadingProgress.cancel();
                    }
                }, TIME_SRL_MS);
            }
        });
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
                // 显示菜单
                SearchItem_LongClick(searchItem);
            }
        });

        refreshListData();
    }

    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "StarSearchItemActivity";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    // endregion 初始化界面

    // region 菜单点击绑定处理 onCreateOptionsMenu onClick onOptionsItemSelected ShowItemLongClickPopupMenu

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
     * 弹出下拉菜单 Click
     * @param v
     */
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
            case R.id.id_SSIActivity_PopupMenu_CancelAllStar:
                SearchItem_CancelAllStarClick();
                m_LongClickItemPopupMenu.dismiss();
            break;
        }
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
                m_searchFragment.show(getSupportFragmentManager(), com.wyt.searchbox.SearchFragment.TAG);
                break;
//            case R.id.action_starUpdate:
//                UpdateData();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示长按菜单
     */
    private void ShowItemLongClickPopupMenu(SearchItem searchItem) {
        m_LongClickItemPopupMenu = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = PopupMenuUtil.initPopupMenu(this, m_LongClickItemPopupMenu, R.layout.popupmenu_starsearchitem_longclickstaritem);

        root.findViewById(R.id.id_SSIActivity_PopupMenu_CancelStar).setOnClickListener(this);
        root.findViewById(R.id.id_SSIActivity_PopupMenu_Cancel).setOnClickListener(this);

        Button CancelAllStar = root.findViewById(R.id.id_SSIActivity_PopupMenu_CancelAllStar);
        CancelAllStar.setOnClickListener(this);

        TextView label = root.findViewById(R.id.id_SSIActivity_PopupMenu_Label);
        label.setText(String.format(getString(R.string.StarSearchItemAct_PopupMenu_Label), searchItem.getTitle()));

        if (FindSearchedItemList != null) {
            CancelAllStar.setEnabled(false);
            CancelAllStar.setTextColor(getResources().getColor(R.color.disable));
        }
        else {
            CancelAllStar.setEnabled(true);
            CancelAllStar.setTextColor(getResources().getColor(R.color.popUpMenu_btn_textColor));
        }

        m_LongClickItemPopupMenu.show();
    }

    // endregion 菜单点击绑定处理

    // region 数据更新处理 refreshListData refreshListView

    /**
     * 更新列表数据为所有新收藏
     */
    private void refreshListData() {
        SearchItemDao searchItemDao = new SearchItemDao(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchItemList = searchItemDao.queryAllSearchItems();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTitle();
                        refreshListView();
                    }
                });
            }
        }).start();
    }

    /**
     * 更新列表内容
     */
    private void refreshListView() {
        searchItemAdapter.setSearchItems(searchItemList);
        m_StarListView.setAdapter(searchItemAdapter);
        searchItemAdapter.notifyDataSetChanged();
        updateTitle();
    }

    /**
     * 更新标题
     */
    private void updateTitle() {
        if (FindSearchedItemList != null)
            setTitle(String.format(getString(R.string.StarSearchItemAct_TitleSearching), FindSearchedItemString, FindSearchedItemList.size()));
        else
            setTitle(String.format(getString(R.string.StarSearchItemAct_TitleNormal), searchItemList.size()));
    }

    // endregion 数据更新处理

    // region 弹出菜单按钮点击事件 SearchItem_CancelStarClick SearchItem_CancelAllStarClick backToLastCmd onBackPressed

    /**
     * 取消收藏
     * @param searchItem
     */
    private void SearchItem_CancelStarClick(SearchItem searchItem) {
        SearchItemDao searchItemDao = new SearchItemDao(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (searchItemDao.deleteSearchItem(searchItem.getUrl()) != -1)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StarSearchItemActivity.this, String.format(getString(R.string.SearchFrag_CancelStarSuccess), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StarSearchItemActivity.this, String.format(getString(R.string.SearchFrag_CancelStarFailed), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                        }
                    });
                searchItemList.remove(searchItem);

                if (FindSearchedItemList != null)
                    FindSearchedItemList.remove(searchItem);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshListView();
                    }
                });
            }
        }).start();
    }

    /**
     * 取消全部收藏
     */
    private void SearchItem_CancelAllStarClick() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.StarSearchItemAct_AlertDlg_NormalTitle)
            .setMessage(R.string.StarSearchItemAct_AllCancelStarAlertDlg_Message)
            .setPositiveButton(R.string.StarSearchItemAct_AlertDlg_PosButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SearchItemDao searchItemDao = new SearchItemDao(StarSearchItemActivity.this);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<SearchItem> s = searchItemDao.queryAllSearchItems();
                            if (searchItemDao.deleteStarSearchItems(s) != -1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(StarSearchItemActivity.this, getString(R.string.SearchFrag_CancelAllStarSuccess), Toast.LENGTH_SHORT).show();
                                        refreshListData();
                                    }
                                });
                            }
                            else
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(StarSearchItemActivity.this, getString(R.string.SearchFrag_CancelAllStarFailed), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    }).start();
                }
            })
            .setNegativeButton(R.string.StarSearchItemAct_AlertDlg_NegButton, null)
            .create()
            .show();
    }

    /**
     * 返回上一次操作
     * 判断是返回上一活动还是取消搜索
     */
    private void backToLastCmd() {
        // 已收藏搜索结果 (共 %d 项)
        if (FindSearchedItemList == null)
            finish();
        else {
            // 返回搜索前

            if (!m_LoadingProgress.isShowing())
                m_LoadingProgress.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    m_SwipeRefresh.setEnabled(true);
                    FindSearchedItemList = null;
                    refreshListData();
                    m_LoadingProgress.dismiss();
                }
            }, TIME_SRL_MS);
        }
    }

    /**
     * 返回按键点击
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToLastCmd();
    }

    // endregion 顶部菜单按钮点击事件处理

    // region 列表点击事件处理 SearchItem_Click SearchItem_LongClick

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

    // endregion 列表点击事件处理

    // region 搜索处理 Find_SearchItem_Click

    /**
     * 搜索处理
     * @param searchStr
     */
    private void Find_SearchItem_Click(String searchStr) {
        FindSearchedItemString = searchStr;

        SearchItemDao searchItemDao = new SearchItemDao(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<SearchItem> AllSearchItems = searchItemDao.queryAllSearchItems();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FindSearchedItemList = SearchUtil.getSearchItems(AllSearchItems.toArray(new SearchItem[0]), FindSearchedItemString);

                        searchItemList = FindSearchedItemList;
                        refreshListView();
                        m_SwipeRefresh.setEnabled(false);
                    }
                });
            }
        }).start();
    }

    // endregion 搜索处理

    // region DEBUG

    // private void UpdateData() {
//
    //     // TODO
//
    //     new Thread(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 SearchItemDao searchItemDao = new SearchItemDao(StarSearchItemActivity.this);
    //                 List<SearchItem> searchItems = searchItemDao.queryAllSearchItems();
    //                 for (SearchItem searchItem: searchItems)
    //                     StarUtil.insertStar(searchItem);
    //             }
    //             catch (ServerErrorException ex) {
    //                 ex.printStackTrace();
    //                 runOnUiThread(new Runnable() {
    //                     @Override
    //                     public void run() {
    //                         new android.app.AlertDialog.Builder(StarSearchItemActivity.this)
    //                                 .setTitle("错误")
    //                                 .setMessage(ex.getMessage())
    //                                 .setPositiveButton("确定", null)
    //                                 .create().show();
    //                     }
    //                 });
    //             }
    //         }
    //     }).start();
    // }

    // endregion DEBUG
}
