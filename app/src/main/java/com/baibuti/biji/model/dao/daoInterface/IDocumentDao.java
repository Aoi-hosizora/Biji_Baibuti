package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;

import java.util.List;

public interface IDocumentDao {

    // 查
    List<Document> queryAllDocuments() throws ServerException;
    List<Document> queryDocumentsByClassName(String className) throws ServerException;
    Document queryDocumentById(int id) throws ServerException;

    // 增删改
    long insertDocument(Document document) throws ServerException;
    boolean updateDocument(Document document) throws ServerException;
    boolean deleteDocument(int id) throws ServerException;
}