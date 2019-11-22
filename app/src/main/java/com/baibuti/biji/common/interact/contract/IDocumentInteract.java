package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface IDocumentInteract {

    // 查
    Observable<MessageVO<List<Document>>> queryAllDocuments();
    Observable<MessageVO<List<Document>>> queryDocumentByClassId(int cid);
    Observable<MessageVO<Document>> queryDocumentById(int id);

    // 增删改
    Observable<MessageVO<DbStatusType>> insertDocument(Document document);
    Observable<MessageVO<DbStatusType>> updateDocument(Document document);
    Observable<MessageVO<DbStatusType>> deleteDocument(int id);
}
