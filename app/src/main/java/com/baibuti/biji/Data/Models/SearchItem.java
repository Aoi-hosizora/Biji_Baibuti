package com.baibuti.biji.Data.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class SearchItem implements Serializable {

    private String title;
    private String url;
    private String keyWord;

    public SearchItem(String title, String url, String keyWord) {
        this.title = title;
        this.url = url;
        this.keyWord = keyWord;
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

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }
}
