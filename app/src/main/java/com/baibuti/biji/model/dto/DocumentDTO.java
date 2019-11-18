package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Document;

import java.io.Serializable;

import lombok.Data;

@Data
public class DocumentDTO implements Serializable {

    private int id;
    private String filename;
    private DocClassDTO docClass;

    private DocumentDTO(int id, String filename, DocClassDTO docClass) {
        this.id = id;
        this.filename = filename;
        this.docClass = docClass;
    }

    /**
     * DocumentDTO -> Document
     */
    public Document toDocument() {
        return new Document(id, filename, docClass);
    }

    /**
     * Document -> DocumentDTO
     */
    public static DocumentDTO toDocument(Document document) {
        return new DocumentDTO(document.getId(), document.getFilename(), document.getClassName());
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
