package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;

import java.util.List;

public interface IDocumentDao {

    // 查
    List<Document> queryAllDocuments() throws ServerException;
    List<Document> queryDocumentByClassId(String className) throws ServerException;
    Document queryDocumentById(int id) throws ServerException;

    // 增删改
    DbStatusType insertDocument(Document document) throws ServerException;
    DbStatusType updateDocument(Document document) throws ServerException;
    DbStatusType deleteDocument(int id) throws ServerException;
}
