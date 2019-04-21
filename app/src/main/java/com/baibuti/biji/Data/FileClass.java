package com.baibuti.biji.Data;

public class FileClass {

    private String fileClassName;

    public FileClass(){

    }

    public FileClass(String fileClassName){
        this.fileClassName = fileClassName;
    }

    public void setFileClassName(String fileClassName){
        this.fileClassName = fileClassName;
    }

    public String getFileClassName(){
        return fileClassName;
    }
}
