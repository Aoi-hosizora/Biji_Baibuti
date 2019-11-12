package com.baibuti.biji.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.baibuti.biji.model.dao.DaoStrategyHelper;
import com.baibuti.biji.model.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.ui.IContextHelper;
import com.baibuti.biji.ui.adapter.SearchItemAdapter;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.activity.MainActivity;
import com.baibuti.biji.ui.activity.StarSearchItemActivity;
import com.baibuti.biji.ui.widget.listView.SpacesItemDecoration;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;
import com.baibuti.biji.util.otherUtil.LayoutUtil;
import com.baibuti.biji.service.search.SearchEngineService;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SearchFragment extends BaseFragment implements IContextHelper {

    private View view;

    @BindView(R.id.id_SearchFrag_QuestionEditText)
    private EditText m_edt_question;

    @BindView(R.id.id_SearchFrag_SearchRetList)
    private RecyclerViewEmptySupport m_list_result;

    private Dialog m_itemPopupMenu;

    private SearchItemAdapter searchItemAdapter;

    /**
     * 页面数据记录
     */
    private PageData pageData = new PageData();
    private static class PageData {

        /**
         * 当前所有列表
         */
        List<SearchItem> searchList;

        /**
         * 当前搜索中的问题
         */
        String currentQuestion;

        /**
         * 当前百度搜索的结果
         */
        int currentPage = 0;
    }

    // region 初始化界面 onCreateView initView initToolbar initListView ShowMessageBox ShowLogE

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent)
                parent.removeView(view);
        }
        else {
            view = inflater.inflate(R.layout.fragment_searchtab, container, false);
            initView();

            AuthManager.getInstance().addLoginChangeListener(new AuthManager.OnLoginChangeListener() {
                @Override
                public void onLogin(String username) { }

                @Override
                public void onLogout() { }
            });

        }
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * 初始化界面
     */
    private void initView() {

        // setHasOptionsMenu(true);

        Toolbar m_toolbar = view.findViewById(R.id.tab_search_toolbar);
        m_toolbar.setTitle("网页搜索");
        m_toolbar.inflateMenu(R.menu.actionbar_searchfrag);
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener((View view) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) activity.openNavMenu();
        });

        m_edt_question = view.findViewById(R.id.id_SearchFrag_QuestionEditText);
        m_list_result = view.findViewById(R.id.id_SearchFrag_SearchRetList);

        ////////////////

        // LayoutMgr:
        m_list_result.addItemDecoration(new SpacesItemDecoration(0));
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        m_list_result.setLayoutManager(layoutManager);

        // EmptyView:
        View ListEmptyView = view.findViewById(R.id.id_SearchFrag_SearchRetList_EmptyView);
        m_list_result.setEmptyView(ListEmptyView);

        // Adapter:
        searchItemAdapter = new SearchItemAdapter();
        searchItemAdapter.setSearchItems(pageData.searchList);
        searchItemAdapter.notifyDataSetChanged();
        searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {

            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                // 浏览器打开
                showBrowser(getActivity(), new String[] { searchItem.getUrl() });
            }

            @Override
            public void onMoreClick(View view) {
                MoreSearchButton_Click();
            }
        });
        searchItemAdapter.setOnItemLongClickListener((View view, SearchItem searchItem) -> ShowItemLongClickPopupMenu(searchItem));

        m_list_result.setAdapter(searchItemAdapter);
    }

    /**
     * Action 打开收藏活动
     */
    @OnItemSelected(R.id.action_SearchStar)
    private void ActionBar_Star_Click() {
        Intent intent = new Intent(getActivity(), StarSearchItemActivity.class);
        startActivity(intent);
    }

    /**
     * 搜索按钮点击
     */
    @OnClick(R.id.id_SearchFrag_SearchButton)
    private void SearchButton_Click() {

        if (m_edt_question.getText().toString().trim().isEmpty()) {
            showAlert(getActivity(), "搜索", "未输入查找内容");
            return;
        }
        pageData.currentQuestion = m_edt_question.getText().toString().trim();

        boolean[] isSearching = new boolean[] { true };

        ProgressDialog progressDialog = showProgress(getActivity(),
            String.format(Locale.CHINA,"搜索 \"%s\" 中", pageData.currentQuestion),
            true, (d) -> isSearching[0] = false);

        new Thread(() -> {
            pageData.searchList = SearchEngineService.getBaiduSearchResult(pageData.currentQuestion, 1);
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    if (!isSearching[0]) return;

                    pageData.currentPage = 1;
                    showToast(getActivity(), String.format(Locale.CHINA, "共获取了 %d 条搜索结果", pageData.searchList.size()));
                    pageData.searchList.remove(SearchItem.MORE_ITEM);
                    pageData.searchList.add(SearchItem.MORE_ITEM);
                    searchItemAdapter.notifyDataSetChanged();
                });
            }
        }).start();

    }

    /**
     * 点击列表内 搜索更多
     */
    private void MoreSearchButton_Click() {

        if (pageData.currentQuestion.trim().isEmpty()) {
            showAlert(getActivity(), "错误", "还没有搜索内容，请现在搜索框内搜索。");
            return;
        }

        boolean[] isSearching = new boolean[] { true };

        ProgressDialog progressDialog = showProgress(getActivity(),
            String.format(Locale.CHINA,"搜索 \"%s\" 中", pageData.currentQuestion),
            true, (d) -> isSearching[0] = false);

        new Thread(() -> {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (!isSearching[0]) return;

            List<SearchItem> newSearchItems = SearchEngineService.getBaiduSearchResult(pageData.currentQuestion, ++pageData.currentPage);
            pageData.searchList.addAll(newSearchItems);

            showToast(getActivity(), String.format(Locale.CHINA, "共新增了 %d 条搜索结果", pageData.searchList.size()));
            pageData.searchList.remove(SearchItem.MORE_ITEM);
            pageData.searchList.add(SearchItem.MORE_ITEM);
            searchItemAdapter.notifyDataSetChanged();

        }).start();
    }

    /**
     * 显示长按菜单
     * @param searchItem 长按项
     */
    private void ShowItemLongClickPopupMenu(SearchItem searchItem) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        m_itemPopupMenu = new Dialog(activity, R.style.BottomDialog);
        LinearLayout root = LayoutUtil.initPopupMenu(activity, m_itemPopupMenu, R.layout.popupmenu_searchitem_longclickitem);

        root.findViewById(R.id.id_SearchFrag_PopupMenu_Star).setOnClickListener((v) -> SearchItem_StarClick(searchItem));
        root.findViewById(R.id.id_SearchFrag_PopupMenu_Cancel).setOnClickListener((v) -> m_itemPopupMenu.dismiss());
        root.findViewById(R.id.id_SearchFrag_PopupMenu_More).setOnClickListener((v) -> {
            MoreSearchButton_Click();
            m_itemPopupMenu.dismiss();
        });

        TextView label = root.findViewById(R.id.id_SearchFrag_PopupMenu_Label);
        label.setText(String.format("当前选中项: %s", searchItem.getTitle()));

        ((Button) root.findViewById(R.id.id_SearchFrag_PopupMenu_Star)).setText("收藏");
        ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(getActivity());
        try {
            if (searchItemDao.querySearchItemByUrl(searchItem.getUrl()) != null)
                ((Button) root.findViewById(R.id.id_SearchFrag_PopupMenu_Star)).setText("取消收藏");
        } catch (ServerException ex) {
            ex.printStackTrace();
        }

        m_itemPopupMenu.show();
    }

    /**
     * 弹出菜单 收藏点击
     */
    private void SearchItem_StarClick(SearchItem searchItem) {

        ISearchItemDao searchItemDao = DaoStrategyHelper.getInstance().getSearchDao(getActivity());
        try {
            if (searchItemDao.querySearchItemByUrl(searchItem.getUrl()) == null) {
                // 收藏
                if (searchItemDao.insertSearchItem(searchItem) != 0)
                    showToast(getActivity(), String.format(Locale.CHINA, "收藏 %s 成功", searchItem.getTitle()));
                else
                    showToast(getActivity(), String.format(Locale.CHINA, "收藏 %s 失败", searchItem.getTitle()));
                searchItemAdapter.notifyDataSetChanged();
            } else {
                // 取消收藏
                if (searchItemDao.deleteSearchItem(searchItem.getUrl()))
                    showToast(getActivity(), String.format(Locale.CHINA, "取消收藏 %s 成功", searchItem.getTitle()));
                else
                    showToast(getActivity(), String.format(Locale.CHINA, "取消收藏 %s 失败", searchItem.getTitle()));
                searchItemAdapter.notifyDataSetChanged();
            }
        } catch (ServerException ex) {
            ex.printStackTrace();
            showAlert(getActivity(), "错误", ex.getMessage());
        }
    }
}
