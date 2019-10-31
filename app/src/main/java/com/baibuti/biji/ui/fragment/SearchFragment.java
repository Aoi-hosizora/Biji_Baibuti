package com.baibuti.biji.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.baibuti.biji.ui.adapter.SearchItemAdapter;
import com.baibuti.biji.model.dao.local.SearchItemDao;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.StarSearchItemActivity;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.otherUtil.CommonUtil;
import com.baibuti.biji.util.layoutUtil.PopupMenuUtil;
import com.baibuti.biji.service.searchng.SearchEngineService;

import java.util.ArrayList;
import java.util.Locale;

public class SearchFragment extends Fragment implements View.OnClickListener {

    // region 定义界面元素 view m_toolbar m_SearchButton m_QuestionEditText m_SearchRetList m_SearchingDialog m_LongClickItemPopupMenu m_LongClickedSearchItem

    private View view;
    private Toolbar m_toolbar;

    private ImageButton m_SearchButton;
    private EditText m_QuestionEditText;
    private RecyclerViewEmptySupport m_SearchRetList;
    private ProgressDialog m_SearchingDialog;

    private Dialog m_LongClickItemPopupMenu;
    private SearchItem m_LongClickedSearchItem;

    // endregion 定义界面元素

    // region 定义数据适配器与列表元素 searchItems searchItemAdapter ITEM_MORE

    private ArrayList<SearchItem> searchItems = new ArrayList<>();
    private SearchItemAdapter searchItemAdapter;

    public SearchItem ITEM_MORE;

    // endregion 定义数据适配器与列表元素

    // region 定义搜索判断用的数据 isSearching SearchPageCnt Question isNewSearch

    /**
     * 判断当前是否在搜索以及回调是否显示，鸵鸟代码用
     */
    private boolean isSearching = false;

    /**
     * 记录当前显示的页数
     */
    private int SearchPageCnt = 0;

    /**
     * 记录当前搜索中的问题，更多搜索点击查看用
     */
    private String Question = "";

    /**
     * 判断当前搜索是新搜索还是更多
     */
    private boolean isNewSearch = true;

    // endregion 定义搜索判断用的数据

    // region 初始化界面 onCreateView initView initToolbar initListView ShowMessageBox ShowLogE

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_searchtab, container, false);

            initView();
        }
        return view;
    }


    /**
     * 初始化界面
     */
    private void initView() {
        initToolbar(view);

        ITEM_MORE = new SearchItem(getActivity().getString(R.string.SearchFrag_SearchingRetLoadingMore), "", SearchItemAdapter.ITEM_MORE_URL);

        m_SearchingDialog = new ProgressDialog(getContext());
        m_SearchingDialog.setCanceledOnTouchOutside(true);
        m_SearchingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isSearching = false;
            }
        });

        m_SearchButton = view.findViewById(R.id.id_SearchFrag_SearchButton);
        m_QuestionEditText = view.findViewById(R.id.id_SearchFrag_QuestionEditText);
        m_SearchRetList = view.findViewById(R.id.id_SearchFrag_SearchRetList);

        m_SearchButton.setOnClickListener(this);
        initListView();
    }

    /**
     * 设置标题工具栏 包括菜单
     * @param view
     */
    private void initToolbar(View view) {

        setHasOptionsMenu(true);

        m_toolbar = view.findViewById(R.id.tab_search_toolbar);
         m_toolbar.inflateMenu(R.menu.actionbar_searchfrag);
         m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 switch (item.getItemId()) {
                     case R.id.action_SearchStar:
                         ActionBar_Star_Click();
                     break;
                 }
                return true;
             }
         });
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openNavMenu();
            }
        });
       //  m_toolbar.setTitleMarginStart(getResources().getDimensionPixelSize(R.dimen.toolbar_title_margin_left));
        m_toolbar.setTitle(R.string.SearchFrag_Header);
    }

    /**
     * 设置 ListView Adapter
     */
    private void initListView() {
        // LayoutMgr:
        m_SearchRetList.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        // LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        // GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        m_SearchRetList.setLayoutManager(layoutManager);

        // EmptyView:
        View ListEmptyView = view.findViewById(R.id.id_SearchFrag_SearchRetList_EmptyView);
        m_SearchRetList.setEmptyView(ListEmptyView);

        // Adapter:
        searchItemAdapter = new SearchItemAdapter();
        searchItemAdapter.setSearchItems(searchItems);

        searchItemAdapter.notifyDataSetChanged();

        // Click:
        searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {

            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                SearchItem_Click(searchItem);
            }

            @Override
            public void onMoreClick(View view) {
                MoreSearchButton_Click();
            }
        });

        searchItemAdapter.setOnItemLongClickListener(new SearchItemAdapter.OnRecyclerViewItemLongClickListener() {

            @Override
            public void onItemLongClick(View view, SearchItem searchItem) {
                SearchItem_LongClick(searchItem);
            }
        });

        m_SearchRetList.setAdapter(searchItemAdapter);
    }

    /**
     * 显示提示对话框
     * @param Content
     */
    private void ShowMessageBox(String Content) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.SearchFrag_AlertDlg_NormalTitle)
                .setMessage(Content)
                .setPositiveButton(R.string.SearchFrag_AlertDlg_PosButton, null)
                .create().show();
    }

    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "SearchFragment";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
    }

    // endregion 初始化界面 ShowMessageBox

    // region 数据变化 onResume updateListAdapter

    @Override
    public void onResume() {
        super.onResume();
        searchItemAdapter.notifyDataSetChanged();
    }

    /**
     * 更新适配器连接数据
     */
    private void updateListAdapter() {
        searchItemAdapter.setSearchItems(searchItems);
        m_SearchRetList.setAdapter(searchItemAdapter);
        searchItemAdapter.notifyDataSetChanged();
    }

    // endregion 数据变化

    // region 菜单按钮 onClick ShowItemLongClickPopupMenu

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // UI
            case R.id.id_SearchFrag_SearchButton:
                SearchButton_Click();
            break;

            // Popup
            case R.id.id_SearchFrag_PopupMenu_Star:
                if (m_LongClickedSearchItem != null)
                    SearchItem_StarClick(m_LongClickedSearchItem);
                m_LongClickItemPopupMenu.dismiss();
            break;
            case R.id.id_SearchFrag_PopupMenu_Cancel:
                m_LongClickItemPopupMenu.dismiss();
            break;
            case R.id.id_SearchFrag_PopupMenu_More:
                MoreSearchButton_Click();
                m_LongClickItemPopupMenu.dismiss();
            break;
        }
    }

    /**
     * 显示长按菜单
     * @param searchItem
     */
    private void ShowItemLongClickPopupMenu(SearchItem searchItem) {
        m_LongClickItemPopupMenu = new Dialog(getActivity(), R.style.BottomDialog);
        LinearLayout root = PopupMenuUtil.initPopupMenu(getActivity(), m_LongClickItemPopupMenu, R.layout.popupmenu_searchitem_longclickitem);

        SearchItemDao searchItemDao = new SearchItemDao(getContext());

        if (searchItemDao.querySearchItemByUrl(searchItem.getUrl()) != null) // 已收藏
            ((Button) root.findViewById(R.id.id_SearchFrag_PopupMenu_Star)).setText(R.string.SearchFrag_PopupMenu_CancelStar);
        else // 未收藏
            ((Button) root.findViewById(R.id.id_SearchFrag_PopupMenu_Star)).setText(R.string.SearchFrag_PopupMenu_Star);

        root.findViewById(R.id.id_SearchFrag_PopupMenu_Star).setOnClickListener(this);
        root.findViewById(R.id.id_SearchFrag_PopupMenu_Cancel).setOnClickListener(this);
        root.findViewById(R.id.id_SearchFrag_PopupMenu_More).setOnClickListener(this);

        TextView label = root.findViewById(R.id.id_SearchFrag_PopupMenu_Label);
        label.setText(String.format(getString(R.string.SearchFrag_PopupMenu_Label), searchItem.getTitle()));

        m_LongClickItemPopupMenu.show();
    }

    // endregion 菜单按钮

    // region 列表和搜索点击更新处理 onHandleSearchedRet SearchButton_Click MoreSearchButton_Click SearchItem_Click SearchItem_LongClick

    /**
     * 收到更新 searchItems 列表 信号
     * @param msg
     */
    private void onHandleSearchedRet(Message msg) {

        if (isSearching && searchItems != null && searchItems.size() != 0) {

            ShowLogE("onHandleSearchedRet", "isNewSearch" + isNewSearch);

            if (isNewSearch) { // 新搜索

                SearchPageCnt = 1;
                Toast.makeText(getActivity(), String.format(Locale.CHINA,
                        getString(R.string.SearchFrag_NewSearchToast), searchItems.size()), Toast.LENGTH_SHORT).show();
            }
            else { // 更多

                Bundle bundle = msg.getData();
                int newCnt = bundle.getInt(HandleWhat.BND_SearchMoreItemCnt, 0);

                SearchPageCnt++;
                Toast.makeText(getActivity(), String.format(Locale.CHINA,
                        getString(R.string.SearchFrag_MoreSearchToast), newCnt), Toast.LENGTH_SHORT).show();
            }

            searchItems.remove(ITEM_MORE);
            searchItems.add(ITEM_MORE);
            updateListAdapter();

        }
        m_SearchingDialog.cancel();
    }

    /**
     * 点击搜索，并发送信号接收处理
     */
    private void SearchButton_Click() {
        Question = m_QuestionEditText.getText().toString().trim();
        if (Question.isEmpty()) {
            ShowMessageBox(getString(R.string.SearchFrag_SearchNullMessage));
            return;
        }

        isSearching = true;
        isNewSearch = true;

        CommonUtil.closeSoftKeyInput(getActivity());

        m_SearchingDialog.setMessage(String.format(getString(R.string.SearchFrag_SearchingProgressText), Question));
        if (!m_SearchingDialog.isShowing())
            m_SearchingDialog.show();

        new Thread() {

            @Override
            public void run() {

                // 搜索并处理，访问网络
                searchItems = SearchEngineService.getBaiduSearchResult(Question, 1);

                // 获取结果，更新数据
                Message message = new Message();
                message.what = HandleWhat.HND_SearchedRet;

                handler.sendMessage(message);
            }
        }.start();

    }

    /**
     * 点击更多，并发送信号接收处理
     */
    private void MoreSearchButton_Click() {
        isSearching = true;
        isNewSearch = false;

        m_SearchingDialog.setMessage(String.format(getString(R.string.SearchFrag_SearchingProgressText), Question));
        if (!m_SearchingDialog.isShowing())
            m_SearchingDialog.show();

        new Thread() {

            @Override
            public void run() {

                ArrayList<SearchItem> newSearchItems = SearchEngineService.getBaiduSearchResult(Question, SearchPageCnt + 1);
                searchItems.addAll(newSearchItems);

                // 获取结果，更新数据

                Message message = new Message();
                message.what = HandleWhat.HND_SearchedRet;

                Bundle bundle = new Bundle();
                bundle.putInt(HandleWhat.BND_SearchMoreItemCnt, newSearchItems.size());
                message.setData(bundle);

                handler.sendMessage(message);
            }
        }.start();
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

    // endregion 列表点击处理

    // region 弹出菜单和顶部菜单点击事件处理 SearchItem_StarClick ActionBar_Star_Click

    /**
     * 弹出菜单收藏点击
     */
    private void SearchItem_StarClick(SearchItem searchItem) {
        SearchItemDao searchItemDao = new SearchItemDao(getContext());
        if (searchItemDao.querySearchItemByUrl(searchItem.getUrl()) == null) {
            // 未收藏
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (searchItemDao.insertStarSearchItem(searchItem) != -1)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), String.format(getString(R.string.SearchFrag_StarSuccess), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                                searchItemAdapter.notifyDataSetChanged();
                            }
                        });
                    else
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), String.format(getString(R.string.SearchFrag_StarFailed), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                                searchItemAdapter.notifyDataSetChanged();
                            }
                        });
                }
            }).start();
        }
        else {
            // 已收藏
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (searchItemDao.deleteSearchItem(searchItem.getUrl()) != -1)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), String.format(getString(R.string.SearchFrag_CancelStarSuccess), searchItem.getTitle()), Toast.LENGTH_SHORT).show();

                                searchItemAdapter.notifyDataSetChanged();
                            }
                        });
                    else
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), String.format(getString(R.string.SearchFrag_CancelStarFailed), searchItem.getTitle()), Toast.LENGTH_SHORT).show();
                                searchItemAdapter.notifyDataSetChanged();
                            }
                        });
                }
            }).start();
        }
    }

    /**
     * Action 打开收藏活动
     */
    private void ActionBar_Star_Click() {
        Intent intent = new Intent(getActivity(), StarSearchItemActivity.class);
        startActivity(intent);
    }

    // endregion 弹出菜单和顶部菜单点击事件处理

    // ************************************************************************************************************** //
    // ************************************************************************************************************** //

    // region 网络处理 HandleWhat handler

    /**
     * HandleWhat 和 HandleBundle 集成类
     */
    private static class HandleWhat {
        /**
         * 处理搜索响应
         */
        static final int HND_SearchedRet = 1;

        static final String BND_SearchMoreItemCnt = "BND_SearchMoreItemCnt";
    }

    /**
     * 网络信号处理
     */
    private final Handler handler = new Handler((Message msg) -> {
        switch (msg.what) {
            case HandleWhat.HND_SearchedRet:
                onHandleSearchedRet(msg);
                break;
        }
        return false;
    });

    // endregion 网络处理
}
