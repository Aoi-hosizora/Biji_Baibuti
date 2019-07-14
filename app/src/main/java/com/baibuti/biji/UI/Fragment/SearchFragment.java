package com.baibuti.biji.UI.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.baibuti.biji.Data.Adapters.SearchItemAdapter;
import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Interface.IShowLog;
import com.baibuti.biji.R;
import com.baibuti.biji.UI.View.SpacesItemDecoration;
import com.baibuti.biji.UI.Widget.RecyclerViewEmptySupport;
import com.baibuti.biji.Utils.SearchNetUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener, IShowLog {

    private View view;
    private Toolbar m_toolbar;

    private Button m_SearchButton;
    private EditText m_QuestionEditText;
    private RecyclerViewEmptySupport m_SearchRetList;
    private ProgressDialog m_SearchingDialog;

    private ArrayList<SearchItem> searchItems = new ArrayList<>();
    private SearchItemAdapter searchItemAdapter;
    private boolean isSearching = false;


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

        m_SearchingDialog = new ProgressDialog(getContext());
        m_SearchingDialog.setMessage(getString(R.string.SearchFrag_SearchingProgressText));
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
        // m_toolbar.inflateMenu(R.menu.xxxx);
        // m_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        //     @Override
        //     public boolean onMenuItemClick(MenuItem item) {
        //     switch (item.getItemId()) {
        //         //
        //     }
        //     return true;
        //     }
        // });
        m_toolbar.setNavigationIcon(R.drawable.tab_menu);
        m_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "ddd", Toast.LENGTH_SHORT).show();
            }
        });
        m_toolbar.setTitle(R.string.SearchFrag_Header);
    }

    /**
     * 设置 ListView Adapter
     */
    private void initListView() {
        // EmptyView:
        View ListEmptyView = view.findViewById(R.id.note_emptylist);
        m_SearchRetList.setEmptyView(ListEmptyView);

        // LayoutMgr:
        m_SearchRetList.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        m_SearchRetList.setLayoutManager(layoutManager);

        // Adapter:
        searchItemAdapter = new SearchItemAdapter();
        searchItemAdapter.setSearchItems(searchItems);

        m_SearchRetList.setAdapter(searchItemAdapter);
        searchItemAdapter.notifyDataSetChanged();
        searchItemAdapter.setOnItemClickListener(new SearchItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, SearchItem searchItem) {
                Toast.makeText(getActivity(), searchItem.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void ShowLogE(String FunctionName, String Msg) {
        String ClassName = "SearchFragment";
        Log.e(getResources().getString(R.string.IShowLog_LogE),
                ClassName + ": " + FunctionName + "###" + Msg);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_SearchFrag_SearchButton:
                SearchButton_Click();
            break;
        }
    }

    /**
     * 点击搜索，并发送信号接收处理
     */
    private void SearchButton_Click() {
        String question = m_QuestionEditText.getText().toString();
        if (question.isEmpty()) {
            ShowMessageBox(getString(R.string.SearchFrag_SearchNullMessage));
            return;
        }

        new Thread() {

            @Override
            public void run() {
                isSearching = true;

                // 搜索并处理，访问网络
                searchItems = SearchNetUtil.SearchBaidu(question);

                // 获取结果，更新数据

                Message message = new Message();
                message.what = HandleWhat.HND_SearchedRet;

                Bundle bundle = new Bundle();
                bundle.putSerializable(HandleWhat.BND_SearchItems, searchItems);

                message.setData(bundle);

                handler.sendMessage(message);
            }
        }.start();

        isSearching = true;
        m_SearchingDialog.show();
    }

    /**
     * 收到更新 searchItems 列表 信号
     * @param msg
     */
    private void onHandleSearchedRet(Message msg) {
        Bundle bundle = msg.getData();
        ArrayList<SearchItem> searchItems = (ArrayList<SearchItem>) bundle.getSerializable(HandleWhat.BND_SearchItems);

        searchItemAdapter.setSearchItems(searchItems);
        searchItemAdapter.notifyDataSetChanged();
        m_SearchingDialog.cancel();
    }





    // 处理网络

    /**
     * HandleWhat 和 HandleBundle 集成类
     */
    private static class HandleWhat {
        static final int HND_SearchedRet = 1;

        static final String BND_SearchItems = "BND_SearchItems";
    }

    /**
     * 网络信号处理
     */
    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HandleWhat.HND_SearchedRet:
                    onHandleSearchedRet(msg);
                break;
            }
            return false;
        }
    });
}
