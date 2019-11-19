package com.baibuti.biji.model.vo;

/**
 * 一致 结巴分词 搜索接口
 */
public interface ISearchEntity {

    /**
     * 获得搜索时用到的内容
     *
     * 可以直接将所有字符串连接到一起
     */
    String getSearchContent();
}
