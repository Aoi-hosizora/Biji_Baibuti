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

import com.baibuti.biji.model.dao.local.SearchItemDao;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.R;
import com.baibuti.biji.ui.widget.otherView.EllipsizeTextView;
import com.baibuti.biji.ui.widget.listView.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.ViewHolder>
    implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private List<SearchItem> m_searchItems;

    public static final String ITEM_MORE_URL = "$more";

    private OnRecyclerViewItemClickListener m_OnItemClickListener;
    private OnRecyclerViewItemLongClickListener m_OnItemLongClickListener;

    public SearchItemAdapter(Context context) {
        this.context = context;
        m_searchItems = new ArrayList<>();
    }

    public void setSearchItems(List<SearchItem> searchItems) {
        this.m_searchItems = searchItems;
    }

    ///

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, SearchItem searchItem);
        void onMoreClick(View view);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, SearchItem searchItem);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.m_OnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.m_OnItemLongClickListener = listener;
    }

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

    @Override
    public boolean onLongClick(View v) {
        if (!((SearchItem) v.getTag()).getUrl().equals(ITEM_MORE_URL)) {
            if (m_OnItemLongClickListener != null)
                m_OnItemLongClickListener.onItemLongClick(v, (SearchItem) v.getTag());
        }
        return true;
    }

    ///

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.adapter_search_item, parent, false);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        return new ViewHolder(view);
    }

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

        View m_view;

        TextView m_title;
        EllipsizeTextView m_content;
        TextView m_url;
        ImageView m_stared;

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
         */
        void setupUI(SearchItem searchItem) {
            if (!searchItem.getUrl().equals(ITEM_MORE_URL))
                setupNormalItemUI(searchItem);
            else
                setupMoreItemUI(searchItem);
        }

        /**
         * 默认内容项
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

            if (searchItemDao.querySearchItemById(searchItem.getId()) == null) {
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
         */
        private void setupMoreItemUI(SearchItem searchItem) {
            m_title.setGravity(Gravity.CENTER);
            m_content.setVisibility(View.GONE);
            m_url.setVisibility(View.GONE);
            m_stared.setVisibility(View.GONE);

            m_title.setText(searchItem.getTitle());

            // layout_marginEnd
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_title.getLayoutParams();
            params.setMarginEnd(0);
            m_title.setLayoutParams(params);
        }
    }
}
