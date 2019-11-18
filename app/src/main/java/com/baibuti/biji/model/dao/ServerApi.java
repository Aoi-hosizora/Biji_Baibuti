package com.baibuti.biji.model.dao;

import com.baibuti.biji.model.dto.DocClassDTO;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 标注需要 Authorization 头
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@interface NeedAuth { }

public interface ServerApi {

    // region Auth (4)

    @Multipart
    @POST("/auth/login")
    Observable<Response<ResponseDTO<AuthRespDTO>>> login(
        @Part("username") String username,
        @Part("password") String password,
        @Part("expiration") int expiration
    );

    @Multipart
    @POST("/auth/register")
    Observable<ResponseDTO<AuthRespDTO>> register(
        @Part("username") String username,
        @Part("password") String password
    );

    @NeedAuth
    @GET("/auth/")
    Observable<ResponseDTO<AuthRespDTO>> currentUser();

    @NeedAuth
    @POST("/auth/logout")
    Observable<ResponseDTO<OneFieldDTO.CountDTO>> logout();

    // endregion Auth

    // region Note (7)

    @NeedAuth
    @GET("/note/")
    Observable<ResponseDTO<NoteDTO[]>> getAllNotes();

    @NeedAuth
    @GET("/note/group/{gid}")
    Observable<ResponseDTO<NoteDTO[]>> getNotesByGroupId(@Path("gid") int id);

    @NeedAuth
    @GET("/note/{nid}")
    Observable<ResponseDTO<NoteDTO>> getNoteById(@Path("nid") int id);

    @NeedAuth
    @Multipart
    @POST("/note/")
    Observable<ResponseDTO<NoteDTO>> insertNote(
        @Part("title") String title,
        @Part("content") String content,
        @Part("group_id") int groupId
    );

    @NeedAuth
    @Multipart
    @PUT("/note/")
    Observable<ResponseDTO<NoteDTO>> updateNote(
        @Part("id") int id,
        @Part("title") String title,
        @Part("content") String content,
        @Part("group_id") int groupId
    );

    @NeedAuth
    @DELETE("/note/delete/{id}")
    Observable<ResponseDTO<NoteDTO>> deleteNote(@Path("id") int id);

    @NeedAuth
    @Multipart
    @DELETE("/note/delete/")
    Observable<ResponseDTO<NoteDTO>> deleteNotes(
        @Part("id") int[] id
    );

    // endregion Note

    // region Group (6)

    @NeedAuth
    @GET("/group/")
    Observable<ResponseDTO<GroupDTO[]>> getAllGroups();

    @NeedAuth
    @GET("/group/{gid}")
    Observable<ResponseDTO<GroupDTO>> getGroupById(@Path("gid") int id);

    @NeedAuth
    @GET("/group/")
    Observable<ResponseDTO<GroupDTO>> getGroupByName(@Query("name") String name);

    @NeedAuth
    @GET("/group/default")
    Observable<ResponseDTO<GroupDTO>> getDefaultGroup();

    @NeedAuth
    @POST("/group/")
    Observable<ResponseDTO<GroupDTO>> insertGroup(
        @Part("name") String name,
        @Part("color") String color
    );

    @NeedAuth
    @PUT("/group/")
    Observable<ResponseDTO<GroupDTO>> updateGroup(
        @Part("id") int id,
        @Part("name") String name,
        @Part("order") int order,
        @Part("color") String color
    );

    @NeedAuth
    @DELETE("/group/{gid}")
    Observable<ResponseDTO<GroupDTO>> deleteGroup(@Path("gid") int id);

    // endregion Group

    // region SearchItem (5)

    @NeedAuth
    @GET("/star/")
    Observable<ResponseDTO<SearchItemDTO[]>> getAllStars();

    @NeedAuth
    @GET("/star/{sid}")
    Observable<ResponseDTO<SearchItemDTO>> getStarById(@Path("sid") int id);

    @NeedAuth
    @Multipart
    @POST("/star/insert")
    Observable<ResponseDTO<SearchItemDTO>> insertStar(
        @Part("title") String title,
        @Part("url") String url,
        @Part("content") String content
    );

    @NeedAuth
    @DELETE("/star/{sid}")
    @FormUrlEncoded
    Observable<ResponseDTO<SearchItemDTO>> deleteStar(@Path("sid") int id);

    @NeedAuth
    @Multipart
    @DELETE("/star/")
    Observable<ResponseDTO<SearchItemDTO[]>> deleteStars(
        @Part("id") int[] id
    );

    // endregion SearchItem

    // region Schedule (3)

    @NeedAuth
    @GET("/schedule/")
    Observable<ResponseDTO<OneFieldDTO.ScheduleDTO>> getSchedule();

    @NeedAuth
    @PUT("/schedule/")
    Observable<ResponseDTO<OneFieldDTO.ScheduleDTO>> updateSchedule();

    @NeedAuth
    @DELETE("/schedule/")
    Observable<ResponseDTO<OneFieldDTO.ScheduleDTO>> deleteSchedule();

    // endregion Schedule

    // region DocClass (7)

    @NeedAuth
    @GET("/docclass/")
    Observable<ResponseDTO<DocClassDTO[]>> getAllDocClasses();

    @NeedAuth
    @GET("/docclass/{cid}")
    Observable<ResponseDTO<DocClassDTO>> getDocClassById(@Path("cid") int id);

    @NeedAuth
    @GET("/docclass/}")
    Observable<ResponseDTO<DocClassDTO>> getDocClassByNane(@Query("name") String name);

    @NeedAuth
    @GET("/docclass/default")
    Observable<ResponseDTO<DocClassDTO>> getDefaultDocClass();

    @NeedAuth
    @Multipart
    @POST("/docclass/")
    Observable<ResponseDTO<DocClassDTO>> insertDocClass(
        @Part("name") String name
    );

    @NeedAuth
    @Multipart
    @PUT("/docclass/")
    Observable<ResponseDTO<DocClassDTO>> updateDocClass(
        @Part("id") int id,
        @Part("name") String name
    );

    @NeedAuth
    @DELETE("/docclass/{cid}")
    Observable<ResponseDTO<DocClassDTO>> deleteDocClass(@Path("cid") int id);

    // TODO 接口待改
    // @GET("/docclass/share")
    // Observable<ResponseDTO<>> getShareCode(@Path("id") int id);

    // endregion DocClass

    // region Document (7)

    @NeedAuth
    @GET("/document/")
    Observable<ResponseDTO<DocumentDTO[]>> getAllDocuments();

    @NeedAuth
    @GET("/document/class/{cid}")
    Observable<ResponseDTO<DocumentDTO[]>> getDocumentByClassId(@Path("cid") String fileClass);

    @NeedAuth
    @GET("/document/{did}")
    Observable<ResponseDTO<DocumentDTO>> getDocumentById(@Path("did") int id);

    @NeedAuth
    @Multipart
    @POST("/document/")
    Observable<ResponseDTO<DocumentDTO>> insertDocument(
        @Part("file") File file,
        @Part("doc_class_id") int classId
    );

    @NeedAuth
    @Multipart
    @PUT("/document/")
    Observable<ResponseDTO<DocumentDTO>> updateDocument(
        @Part("id") int id,
        @Part("filename") String filename,
        @Part("doc_class_id") int classId
    );

    @NeedAuth
    @DELETE("/document/{did}")
    Observable<ResponseDTO<DocumentDTO>> deleteDocument(@Path("did") int id);

    // TODO 接口待改
    // @GET("/file/get_share")
    // Observable<ResponseDTO<>> getShareCode(@Path("id") int id);

    // endregion Document

    // region Raw (2)

    @NeedAuth
    @Multipart
    @POST("/raw/image")
    Observable<ResponseDTO<OneFieldDTO.FilenameDTO>> uploadImage(
        @Part("image") File image,
        @Part("type") String type
    );

    @NeedAuth
    @Multipart
    @DELETE("/rae/image")
    Observable<ResponseDTO<OneFieldDTO.CountDTO>> deleteImages(
        @Part("urls") String[] urls,
        @Part("type") String type
    );

    // endregion Raw

}
