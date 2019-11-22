package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface IDocClassInteract {

    // 查
    Observable<MessageVO<List<DocClass>>> queryAllDocClasses();
    Observable<MessageVO<DocClass>> queryDocClassById(int id);
    Observable<MessageVO<DocClass>> queryDocClassByName(String name);
    Observable<MessageVO<DocClass>> queryDefaultDocClass();

    // 增删改
    Observable<MessageVO<DbStatusType>> insertDocClass(DocClass docClass);
    Observable<MessageVO<DbStatusType>> updateDocClass(DocClass docClass);
    Observable<MessageVO<DbStatusType>> deleteDocClass(int id, boolean isToDefault);
}
