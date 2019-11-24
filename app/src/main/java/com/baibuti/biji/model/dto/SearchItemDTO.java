package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.SearchItem;

import java.io.Serializable;

import lombok.Data;

@Data
public class SearchItemDTO implements Serializable {

    private int id;
    private String title;
    private String url;
    private String content;

    private SearchItemDTO(int id, String title, String url, String content) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.content = content.replaceAll("[\n|\r]", " ");
    }

    /**
     * SearchItemNetInteract -> SearchItem
     */
    public SearchItem toSearchItem() {
        return new SearchItem(id, title, content, url);
    }

    /**
     * SearchItemNetInteract[] -> SearchItem[]
     */
    public static SearchItem[] toSearchItems(SearchItemDTO[] searchItemsDTO) {
        if (searchItemsDTO == null)
            return new SearchItem[0];

        SearchItem[] searchItems = new SearchItem[searchItemsDTO.length];
        for (int i = 0; i < searchItemsDTO.length; i++)
            searchItems[i] = searchItemsDTO[i].toSearchItem();
        return searchItems;
    }
}
