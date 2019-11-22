package com.baibuti.biji.model.dao;

/**
 * 数据库操作后的状态
 * 没必要加 NOT_FOUND | FOUNDED
 */
public enum DbStatusType {

    /**
     * 操作成功
     */
    SUCCESS(0),

    /**
     * 操作失败
     */
    FAILED(1),

    /**
     * Update Delete: 修改默认
     */
    DEFAULT(2),

    /**
     * Insert Update: Unique 重复
     */
    DUPLICATED(3),

    /**
     * 文件上传失败
     */
    UPLOAD_FAILED(4);

    int type;

    DbStatusType(int type) {
        this.type = type;
    }
}
