package com.baibuti.biji.Data.Models;

import com.baibuti.biji.Data.Adapters.SearchItemAdapter;
import com.baibuti.biji.R;

import java.io.Serializable;

public class SearchItem implements Serializable {

    private String title;
    private String url;
    private String content;

    public SearchItem(String title, String keyWord, String url) {
        this.title = title;
        this.url = url;
        this.content = keyWord;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String keyWord) {
        this.content = keyWord;
    }

    @Override
    public String toString() {
        return "title: " + title + ", url: " + url + ", content: " + content;
    }
}
