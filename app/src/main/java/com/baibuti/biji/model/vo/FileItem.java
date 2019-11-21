package com.baibuti.biji.model.vo;

import java.io.Serializable;
import java.util.HashMap;

import lombok.Data;

/**
 * FileImportDialog 文件列表项
 */
@Data
public class FileItem implements Serializable {

    /**
     * 被选择
     */
    public static final int CHECKED = 1;

    private int tag;

    private String filePath;
    private String fileName;
    private String fileType;

    public FileItem(String fileName, String filePath, String fileType) {
        this.tag = 0;
        this.setFileName(fileName);
        this.setFilePath(filePath);
        this.setFileType(fileType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileItem))
            return false;
        FileItem that = (FileItem) obj;
        return this.filePath.equals(that.getFilePath());
    }

    @Override
    public int hashCode() {
        return (filePath + fileName + fileType).hashCode();
    }
}
