package com.baibuti.biji.model.po;

import com.baibuti.biji.model.vo.ISearchEntity;
import com.baibuti.biji.ui.adapter.SearchItemAdapter;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchItem implements Serializable, ISearchEntity {

    private int id;
    private String title;
    private String url;
    private String content;

    /**
     * 加载更多
     */
    public static SearchItem MORE_ITEM = new SearchItem(-1, "加载更多...", "", SearchItemAdapter.ITEM_MORE_URL);

    /**
     * BO
     */
    public SearchItem(String title, String content, String url) {
        this(-1, title, content, url);
    }

    /**
     * PO
     */
    public SearchItem(int id, String title, String content, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.content = content;
    }

    @Override
    public String toString() {
        return "id: " + id + ", title: " + title + ", url: " + url + ", content: " + content;
    }

    @Override
    public String getSearchContent() {
        return this.title + " " + this.content + " " + this.url;
    }
}
