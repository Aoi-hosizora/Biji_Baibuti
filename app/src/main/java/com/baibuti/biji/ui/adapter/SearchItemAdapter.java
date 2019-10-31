package com.baibuti.biji.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baibuti.biji.data.dao.db.SearchItemDao;
import com.baibuti.biji.data.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.otherView.EllipsizeTextView;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private Context m_Context;
    private List<SearchItem> m_searchItems;

    /**
     * 加载更多的标志 URL
     */
    public static final String ITEM_MORE_URL = "$more";

    /**
     * 外部设置的监听
     */
    private OnRecyclerViewItemClickListener m_OnItemClickListener;
    private OnRecyclerViewItemLongClickListener m_OnItemLongClickListener;
//    private OnRecyclerViewItemSideClickListener m_OnItemSideClickListener;

    public SearchItemAdapter() {
        m_searchItems = new ArrayList<>();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, SearchItem searchItem);
        void onMoreClick(View view);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, SearchItem searchItem);
    }


//    public interface OnRecyclerViewItemSideClickListener {
//        void onItemSideClick(View view, int position);
//    }

    /**
     * 适配器列表内容
     * @param searchItems
     */
    public void setSearchItems(List<SearchItem> searchItems) {
        this.m_searchItems = searchItems;
    }

    /**
     * 适配器点击事件
     * @param listener
     */
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.m_OnItemClickListener = listener;
    }

    /**
     * 适配器长按事件
     * @param listener
     */
    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.m_OnItemLongClickListener = listener;
    }

//    /**
//     * 适配器辅助按钮点击事件
//     * @param listener
//     */
//    public void setOnItemSideClickListener(OnRecyclerViewItemSideClickListener listener) {
//        this.m_OnItemSideClickListener = listener;
//    }

    /**
     * 触发点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (m_OnItemClickListener != null) {
            SearchItem clickedItem = (SearchItem) v.getTag();
            if (!clickedItem.getUrl().equals(ITEM_MORE_URL))
                m_OnItemClickListener.onItemClick(v, clickedItem);
            else
                m_OnItemClickListener.onMoreClick(v);
        }
    }

    /**
     * 触发长按事件
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        if (!((SearchItem) v.getTag()).getUrl().equals(ITEM_MORE_URL)) {
            if (m_OnItemLongClickListener != null)
                m_OnItemLongClickListener.onItemLongClick(v, (SearchItem) v.getTag());
        }
        return true;
    }

    /**
     * 创建 ViewHolder，绑定点击和长按事件
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        m_Context = parent.getContext();
        View view = LayoutInflater.from(m_Context).inflate(R.layout.modulelayout_searchfrag_searchitem, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    /**
     * 绑定 ViewHolder 数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem searchItem = m_searchItems.get(position);
        holder.itemView.setTag(searchItem);
        holder.setupUI(searchItem);
    }

    @Override
    public int getItemCount() {
        if (m_searchItems != null)
            return m_searchItems.size();
        return 0;
    }

    public class ViewHolder extends RecyclerViewEmptySupport.ViewHolder {

        private View m_view;

        private TextView m_title;
        private EllipsizeTextView m_content;
        private TextView m_url;
        private ImageView m_stared;

        ViewHolder(View view) {
            super(view);

            m_view = view;

            m_title = view.findViewById(R.id.id_adapter_SearchItem_Title);
            m_content = view.findViewById(R.id.id_adapter_SearchItem_Content);
            m_url = view.findViewById(R.id.id_adapter_SearchItem_Url);
            m_stared = view.findViewById(R.id.id_adapter_SearchItem_IsStared);
        }

        /**
         * 设置 ViewHolder UI
         * @param searchItem
         */
        void setupUI(SearchItem searchItem) {
            if (!searchItem.getUrl().equals(ITEM_MORE_URL))
                setupNormalItemUI(searchItem);
            else
                setupMoreItemUI(searchItem);
        }

        /**
         * 默认内容项
         * @param searchItem
         */
        private void setupNormalItemUI(SearchItem searchItem) {

            SearchItemDao searchItemDao = new SearchItemDao(m_view.getContext());

            m_title.setGravity(Gravity.START);
            m_content.setVisibility(View.VISIBLE);
            m_url.setVisibility(View.VISIBLE);
            m_stared.setVisibility(View.VISIBLE);

            m_content.setMaxLines(2);

            m_title.setText(searchItem.getTitle());
            m_content.setText(searchItem.getContent());
            m_url.setText(searchItem.getUrl());

            if (searchItemDao.querySearchItemByUrl(searchItem.getUrl()) == null) {
                // not star
                m_stared.setImageDrawable(m_view.getContext().getDrawable(R.drawable.ic_star_border_theme_24dp));
            }
            else {
                // stared
                m_stared.setImageDrawable(m_view.getContext().getDrawable(R.drawable.ic_star_theme_24dp));
            }

            // layout_marginEnd
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_title.getLayoutParams();
            params.setMarginEnd(m_view.getContext().getResources().getDimensionPixelSize(R.dimen.SearchItem_TitlePaddingEnd_30));
            m_title.setLayoutParams(params);
        }

        /**
         * 更多项
         * @param searchItem
         */
        private void setupMoreItemUI(SearchItem searchItem) {
            m_title.setGravity(Gravity.CENTER);
            m_content.setVisibility(View.GONE);
            m_url.setVisibility(View.GONE);
            m_stared.setVisibility(View.GONE);

            m_title.setText(searchItem.getTitle());

            // layout_marginEnd
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_title.getLayoutParams();
            params.setMarginEnd(m_view.getContext().getResources().getDimensionPixelSize(R.dimen.SearchItem_TitlePaddingEnd_0));
            m_title.setLayoutParams(params);
        }
    }
}
