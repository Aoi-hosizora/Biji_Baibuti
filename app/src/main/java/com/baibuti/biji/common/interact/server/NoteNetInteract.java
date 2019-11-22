package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.common.interact.contract.INoteInteract;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.common.Urls;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.common.interact.MessageErrorParser;
import com.baibuti.biji.model.vo.MessageVO;
import com.baibuti.biji.util.imgTextUtil.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetInteract implements INoteInteract {

    @Override
    public Observable<MessageVO<List<Note>>> queryAllNotes() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllNotes()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Note>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(NoteDTO.toNotes(responseDTO.getData())));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Note>>> queryNotesByGroupId(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNotesByGroupId(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Note>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(NoteDTO.toNotes(responseDTO.getData())));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Note>> queryNoteById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNoteById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Note>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toNote());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertNote(Note note) {
        // TODO
        return uploadImage(note)
            .flatMap((MessageVO<Note> newNote) -> RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .insertNote(note.getTitle(), note.getContent(), note.getGroup().getId(),
                    note.getCreateTime_FullString(), note.getUpdateTime_FullString())
                .map(responseDTO -> {
                    if (responseDTO.getCode() != 200)
                        return new MessageVO<Boolean>(false, responseDTO.getMessage());
                    note.setId(responseDTO.getData().getId());
                    return new MessageVO<>(true);
                })
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> updateNote(Note note) {
        // TODO
        return uploadImage(note)
            .flatMap((MessageVO<Note> newNote) -> RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .updateNote(note.getId(), note.getTitle(), note.getContent(), note.getGroup().getId())
                .map(responseDTO -> {
                    if (responseDTO.getCode() != 200)
                        return new MessageVO<Boolean>(false, responseDTO.getMessage());
                    return new MessageVO<>(true);
                })
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteNote(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNote(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 20)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Integer>> deleteNotes(int[] ids) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNotes(ids)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Integer>(false,  responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().getCount());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 上传所有笔本地图片
     */
    private Observable<MessageVO<Note>> uploadImage(Note note) {

        return Observable.create(
            (ObservableEmitter<MessageVO<Note>> emitter) -> {
                List<String> textList = StringUtil.cutStringByImgTag(note.getContent()); // 所有
                Set<String> uploadUrl = new TreeSet<>(); // 所有本地图片
                for (String blocks : textList)
                    if (!(blocks.contains("<img") && blocks.contains("src=")))
                        uploadUrl.add(blocks);

                for (String url : uploadUrl.toArray(new String[0])) {
                    Observable<ResponseDTO<OneFieldDTO.FilenameDTO>> observable = RetrofitFactory.getInstance()
                        .createRequest(AuthManager.getInstance().getAuthorizationHead())
                        .uploadImage(new File(url), OneFieldDTO.RawImageType_Note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

                    ResponseDTO<OneFieldDTO.FilenameDTO> response = observable.toFuture().get();
                    if (response.getCode() != MessageErrorParser.SUCCESS)
                        emitter.onNext(new MessageVO<>(false, response.getMessage()));
                    String newUrl = Urls.BaseServerEndPoint + response.getData().getFilename();
                    note.setContent(note.getContent().replace(url, newUrl));
                }
                emitter.onNext(new MessageVO<>(note));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
