package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;

import java.util.List;

public class DocumentNetDao implements IDocumentDao {

    @Override
    public List<Document> queryAllDocuments() throws ServerException {
        return null;
    }

    @Override
    public List<Document> queryDocumentsByClassName(String className) throws ServerException {
        return null;
    }

    @Override
    public Document queryDocumentById(int id) throws ServerException {
        return null;
    }

    @Override
    public long insertDocument(Document document) throws ServerException {
        return 0;
    }

    @Override
    public boolean updateDocument(Document document) throws ServerException {
        return false;
    }

    @Override
    public boolean deleteDocument(int id) throws ServerException {
        return false;
    }
}
