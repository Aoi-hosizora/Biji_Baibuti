package com.baibuti.biji.model.po;

import java.io.Serializable;

/**
 * FileItem为导入文件对话框中的文件列表项
 */
@Deprecated
public class FileItem implements Serializable {

    private int tag;

    private String filePath;
    private String fileName;
    private String fileType;

    @Override
    public boolean equals(Object obj) {
        FileItem that = (FileItem) obj;
        return this.filePath.equals(that.getFilePath());
    }

    public FileItem(){
    }

    public FileItem(String filePath){
        this.setFilePath(filePath);
    }

    public FileItem(String fileName, String filePath) {
        this.setFileName(fileName);
        this.setFilePath(filePath);
    }

    public FileItem(String fileName, String filePath, String fileType){
        this.tag = 0;
        this.setFileName(fileName);
        this.setFilePath(filePath);
        this.setFileType(fileType);
    }

    public void setFileType(String fileType){
        this.fileType = fileType;
    }

    public String getFileType(){
        return fileType;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setTag(int tag){
        this.tag = tag;
    }

    public int getTag(){
        return tag;
    }
}
