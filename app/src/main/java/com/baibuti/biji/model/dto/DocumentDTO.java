package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Document;

import java.io.Serializable;

import lombok.Data;

@Data
public class DocumentDTO implements Serializable {

    private int id;
    private String filename;
    private DocClassDTO docClass;
    private String uuid;

    private DocumentDTO(int id, String filename, DocClassDTO docClass, String uuid) {
        this.id = id;
        this.filename = filename;
        this.docClass = docClass;
        this.uuid = uuid;
    }

    /**
     * DocumentDTO -> Document
     */
    public Document toDocument() {
        return new Document(id, filename, docClass.toFileClass(), uuid);
    }

    /**
     * DocumentDTO[] -> Document[]
     */
    public static Document[] toDocuments(DocumentDTO[] documentsDTO) {
        if (documentsDTO == null)
            return null;
        Document[] documents = new Document[documentsDTO.length];
        for (int i = 0; i < documentsDTO.length; i++)
            documents[i] = documentsDTO[i].toDocument();
        return documents;
    }
}
