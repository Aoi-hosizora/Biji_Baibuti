package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.ShareCodeItem;

import lombok.Data;

@Data
public class ShareCodeDTO {

    private String sc;
    private DocumentDTO[] documents;

    public ShareCodeItem toShareCodeItem() {
        return new ShareCodeItem(sc, DocumentDTO.toDocuments(documents));
    }

    public static ShareCodeItem[] toShareCodeItems(ShareCodeDTO[] shareCodesDTO) {
        ShareCodeItem[] shareCodeItems = new ShareCodeItem[shareCodesDTO.length];
        for (int i = 0; i < shareCodeItems.length; i++)
            shareCodeItems[i] = shareCodesDTO[i].toShareCodeItem();
        return shareCodeItems;
    }
}
