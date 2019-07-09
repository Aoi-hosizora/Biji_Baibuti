package com.baibuti.biji.Data.Models;

import java.io.Serializable;

public class FileClass implements Serializable {

    private int id;
    private int order;
    private String fileClassName;
    public static String GetDefaultFileClassName = "PDF";

    @Override
    public boolean equals(Object obj) {
        FileClass that = (FileClass) obj;
        return (this.id == that.getId()&&
                this.order == that.getOrder()&&
                this.fileClassName.equals(that.getFileClassName()));
    }

    public FileClass(){
        id = order = 0;
        fileClassName = "+";
    }

    public FileClass(int id, int order, String fileClassName){
        this.id = id;
        this.order = order;
        this.fileClassName = fileClassName;
    }

    public FileClass(String fileClassName, int order){
        this.id = id;
        this.fileClassName = fileClassName;
        this.order = order;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setOrder(int order){
        this.order = order;
    }

    public int getOrder(){
        return order;
    }

    public void setFileClassName(String fileClassName){
        this.fileClassName = fileClassName;
    }

    public String getFileClassName(){
        return fileClassName;
    }
}
