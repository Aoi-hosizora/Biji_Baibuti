package com.baibuti.biji.Data.Models;

import java.io.Serializable;

public class Document implements Serializable {

    private int id;
    private String documentType;
    private String documentName;
    private String documentPath;
    private String documentClassName;

    @Override
    public boolean equals(Object obj) {
        Document that = (Document) obj;
        return this.documentPath.equals(that.getDocumentPath());
    }

    public Document(){
    }

    public Document(String documentClassName, String documentPath){
        this.id = 0;
        this.setDocumentClassName(documentClassName);
        this.setDocumentPath(documentPath);
    }

    public Document(String documentType, String documentName, String documentPath, String documentClassName){
        this.id = 0;
        this.documentType = documentType;
        this.documentName = documentName;
        this.documentPath = documentPath;
        this.documentClassName = documentClassName;
    }

    public Document(int id, String documentType, String documentName, String documentPath){
        this.id = id;
        this.documentType = documentType;
        this.documentName = documentName;
        this.documentPath = documentPath;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setDocumentType(String documentType){
        this.documentType = documentType;
    }

    public String getDocumentType(){
        return documentType;
    }

    public void setDocumentName(String documentName){
        this.documentName = documentName;
    }

    public String getDocumentName(){
        return documentName;
    }

    public void setDocumentPath(String documentPath){
        this.documentPath = documentPath;
    }

    public String getDocumentPath(){
        return documentPath;
    }

    public void setDocumentClassName(String documentClassName){
        this.documentClassName = documentClassName;
    }

    public String getDocumentClassName(){
        return documentClassName;
    }
}
