package com.baibuti.biji.model.po;

import com.baibuti.biji.ui.adapter.SearchItemAdapter;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchItem implements Serializable, ISearchEntity {

    private String title;
    private String url;
    private String content;

    /**
     * 加载更多
     */
    public static SearchItem MORE_ITEM = new SearchItem("加载更多...", "", SearchItemAdapter.ITEM_MORE_URL);

    public SearchItem(String title, String content, String url) {
        this.title = title;
        this.url = url;
        this.content = content;
    }

    @Override
    public String toString() {
        return "title: " + title + ", url: " + url + ", content: " + content;
    }

    @Override
    public String getSearchContent() {
        return this.title + " " + this.content + " " + this.url;
    }
}
