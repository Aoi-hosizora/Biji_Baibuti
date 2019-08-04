package com.baibuti.biji.Net.Models.RespObj;

import java.io.Serializable;

public class UploadStatus implements Serializable {

    private String newFileName;
    private String message;
    private boolean isSuccess;

    /**
     * 上传成功
     * @param newFileName
     * @param message
     */
    public UploadStatus(String newFileName, String message) {
        this.newFileName = newFileName;
        this.message = message;
        this.isSuccess = true;
    }

    /**
     * 上传失败
     * @param message
     */
    public UploadStatus(String message) {
        this.newFileName = "";
        this.message = message;
        this.isSuccess = false;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
