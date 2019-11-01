package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Document;

import java.io.Serializable;

import lombok.Data;

@Data
public class DocumentDTO implements Serializable {

    private int id;
    private String filePath;
    private String className;

    private DocumentDTO(int id, String filePath, String className) {
        this.id = id;
        this.filePath = filePath;
        this.className = className;
    }

    /**
     * DocumentDTO -> Document
     */
    private Document toDocument() {
        return new Document(id, filePath, className);
    }

    /**
     * Document -> DocumentDTO
     */
    public static DocumentDTO toDocument(Document document) {
        return new DocumentDTO(document.getId(), document.getFilePath(), document.getClassName());
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

    // /**
    //  * Document[] -> DocumentDTO[]
    //  */
    // public static DocumentDTO[] toDocumentsDTO(Document[] documents) {
    //     if (documents == null)
    //         return null;
    //     DocumentDTO[] documentsDTO = new DocumentDTO[documents.length];
    //     for (int i = 0; i < documents.length; i++)
    //         documentsDTO[i] = toDocument(documents[i]);
    //     return documentsDTO;
    // }
}
