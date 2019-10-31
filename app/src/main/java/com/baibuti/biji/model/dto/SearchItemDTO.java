package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.SearchItem;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SearchItemDTO implements Serializable {

    private String title;
    private String url;
    private String content;

    private SearchItemDTO(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content.replaceAll("[\n|\r]", " ");
    }

    @Data
    public static class SearchItemUrls implements Serializable {

        private List<String> urls;

        SearchItemUrls() { }

        void addUrl(String url) {
            urls.add(url);
        }
    }

    /**
     * SearchItemNetDao -> SearchItem
     */
    public SearchItem toSearchItem() {
        return new SearchItem(title, content, url);
    }

    /**
     * SearchItem -> SearchItemNetDao
     */
    public static SearchItemDTO toSearchItemDTO(SearchItem searchItem) {
        return new SearchItemDTO(searchItem.getTitle(), searchItem.getUrl(), searchItem.getContent());
    }

    /**
     * SearchItemNetDao[] -> SearchItem[]
     */
    public static SearchItem[] toSearchItems(SearchItemDTO[] searchItemsDTO) {
        if (searchItemsDTO == null) return null;

        SearchItem[] searchItems = new SearchItem[searchItemsDTO.length];
        for (int i = 0; i < searchItemsDTO.length; i++)
            searchItems[i] = searchItemsDTO[i].toSearchItem();
        return searchItems;
    }

    // /**
    //  * SearchItem[] -> SearchItemNetDao[]
    //  * @return
    //  */
    // public static SearchItemNetDao[] toSearchItemsDTO(SearchItem[] searchItems) {
    //     if (searchItems == null) return null;
    //
    //     SearchItemNetDao[] searchItemsDTO = new SearchItemNetDao[searchItems.length];
    //     for (int i = 0; i < searchItems.length; i++)
    //         searchItemsDTO[i] = toSearchItemDTO(searchItems[i]);
    //     return searchItemsDTO;
    // }

    public static SearchItemUrls toSearchItemUrls(List<SearchItem> searchItems) {
        SearchItemUrls urls = new SearchItemUrls();
        for (SearchItem searchItem : searchItems)
            urls.addUrl(searchItem.getUrl());

        return urls;
    }
}
