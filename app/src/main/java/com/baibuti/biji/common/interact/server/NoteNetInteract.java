package com.baibuti.biji.common.interact.server;

import android.util.Log;
import android.util.Pair;

import com.baibuti.biji.common.interact.contract.INoteInteract;
import com.baibuti.biji.common.retrofit.ServerUrl;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.vo.MessageVO;
import com.baibuti.biji.util.imgTextUtil.StringUtil;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class NoteNetInteract implements INoteInteract {

    @Override
    public Observable<MessageVO<List<Note>>> queryAllNotes() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllNotes()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Note>>(false, responseDTO.getMessage());
                List<Note> fromNotes = new ArrayList<>();
                Collections.addAll(fromNotes, NoteDTO.toNotes(responseDTO.getData()));
                return new MessageVO<>(fromNotes);
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
                List<Note> fromNotes = new ArrayList<>();
                Collections.addAll(fromNotes, NoteDTO.toNotes(responseDTO.getData()));
                return new MessageVO<>(fromNotes);
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
        Pair<Observable<Note>, Integer> pair = uploadImage(note);
        return pair.first
            .concatMap((Note newNote) -> RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .insertNote(newNote.getTitle(), newNote.getContent(), newNote.getGroup().getId(), newNote.getCreateTime_FullString(), newNote.getUpdateTime_FullString())
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
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> updateNote(Note note) {
        Pair<Observable<Note>, Integer> pair = uploadImage(note);
        return pair.first
            .concatMap((Note newNote) -> RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .updateNote(newNote.getId(), newNote.getTitle(), newNote.getContent(), newNote.getGroup().getId())
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
        Integer[] newIds = new Integer[ids.length];
        for (int i = 0; i < ids.length; i++)
            newIds[i] = ids[i];
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNotes(newIds)
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
    private Pair<Observable<Note>, Integer> uploadImage(Note note) {
        List<String> textList = StringUtil.cutStringByImgTag(note.getContent()); // 所有
        Set<String> uploadUrl = new TreeSet<>(); // 所有本地图片

        for (String tag : textList) {
            if (tag.contains("<img") && tag.contains("src=")) {
                String url = StringUtil.getImgSrc(tag);
                File file = new File(url);
                if (file.exists()) // 非远程连接
                    uploadUrl.add(url);
            }
        }

        Observable<Note> observable =  Observable.create((ObservableEmitter<Note> emitter) -> {

            int[] count = new int[]{0}; // 总共上传的数量
            CompositeDisposable compositeDisposable = new CompositeDisposable();
            Note newNote = new Note(note);

            for (String url : uploadUrl) {
                File file = new File(url);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                compositeDisposable.add(RetrofitFactory.getInstance()
                    .createRequest(AuthManager.getInstance().getAuthorizationHead())
                    .uploadImage(body, OneFieldDTO.RawImageType_Note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io()) // <<
                    .subscribe((ResponseDTO<OneFieldDTO.FilenameDTO> responseDTO) -> {
                        count[0]++;
                        if (responseDTO.getCode() == 200) {
                            Log.i("", "uploadImage: " + newNote.getContent());
                            Log.i("", "uploadImage: " + url + "->" + responseDTO.getData().getFilename());
                            newNote.setContent(newNote.getContent().replace(url,
                                ServerUrl.BaseServerEndPoint + responseDTO.getData().getFilename().substring(1)));
                            Log.i("", "uploadImage: " + newNote.getContent());
                        }

                        if (count[0] == uploadUrl.size()) {
                            Log.i("", "uploadImage: xxx");
                            // NetworkOnMainThreadException
                            emitter.onNext(newNote);
                            emitter.onComplete();
                        }
                    }, (throwable) -> {
                        if (throwable instanceof HttpException) {
                            ResponseBody responseBody = ((HttpException) throwable).response().errorBody();

                            Gson gson = new Gson();
                            String res = responseBody == null ? "Null Response Error" : responseBody.string();
                            ResponseDTO resp = gson.fromJson(res, ResponseDTO.class);
                            Log.e("", "uploadImage: " + resp.getCode() + " " + resp.getMessage());
                        } else
                            throwable.printStackTrace();

                        emitter.onError(new Exception("Image Upload Failed"));
                    })
                );
            }
        });

        return new Pair<>(observable, uploadUrl.size());
    }
}
