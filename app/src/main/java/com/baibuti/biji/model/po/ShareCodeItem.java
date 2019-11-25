package com.baibuti.biji.model.po;

import lombok.Data;

@Data
public class ShareCodeItem {

    private String sc;
    private Document[] documents;

    public ShareCodeItem(String sc, Document[] documents) {
        this.sc = sc;
        this.documents = documents;
    }
}
