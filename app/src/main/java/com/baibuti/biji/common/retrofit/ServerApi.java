package com.baibuti.biji.common.retrofit;

import com.baibuti.biji.model.dto.DocClassDTO;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.model.dto.ShareCodeDTO;
import com.baibuti.biji.common.auth.dto.AuthRespDTO;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

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
    Observable<ResponseDTO<AuthRespDTO>> login(
        @Part("username") String username,
        @Part("password") String password,
        @Part("expiration") Integer expiration
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
    Observable<ResponseDTO<NoteDTO[]>> getNotesByGroupId(@Path("gid") Integer id);

    @NeedAuth
    @GET("/note/{nid}")
    Observable<ResponseDTO<NoteDTO>> getNoteById(@Path("nid") Integer id);

    @NeedAuth
    @Multipart
    @POST("/note/")
    Observable<ResponseDTO<NoteDTO>> insertNote(
        @Part("title") String title,
        @Part("content") String content,
        @Part("group_id") Integer groupId,
        @Part("create_time") String createTime,
        @Part("update_time") String updateTime
    );

    @NeedAuth
    @Multipart
    @PUT("/note/")
    Observable<ResponseDTO<NoteDTO>> updateNote(
        @Part("id") Integer id,
        @Part("title") String title,
        @Part("content") String content,
        @Part("group_id") Integer groupId
    );

    @NeedAuth
    @DELETE("/note/delete/{id}")
    Observable<ResponseDTO<NoteDTO>> deleteNote(@Path("id") Integer id);

    @NeedAuth
    @Multipart
    @DELETE("/note/delete/")
    Observable<ResponseDTO<OneFieldDTO.CountDTO>> deleteNotes(
        @Part("id") Integer[] id
    );

    // endregion Note

    // region Group (7)

    @NeedAuth
    @GET("/group/")
    Observable<ResponseDTO<GroupDTO[]>> getAllGroups();

    @NeedAuth
    @GET("/group/{gid}")
    Observable<ResponseDTO<GroupDTO>> getGroupById(@Path("gid") Integer id);

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
        @Part("id") Integer id,
        @Part("name") String name,
        @Part("order") Integer order,
        @Part("color") String color
    );

    @NeedAuth
    @PUT("/group/order")
    Observable<ResponseDTO<OneFieldDTO.CountDTO>> updateGroupsOrder(
        @Part("id_order") String[] id_order
    );

    @NeedAuth
    @DELETE("/group/{gid}")
    Observable<ResponseDTO<GroupDTO>> deleteGroup(@Path("gid") Integer id, @Query("default") Boolean isToDefault);

    // endregion Group

    // region SearchItem (5)

    @NeedAuth
    @GET("/star/")
    Observable<ResponseDTO<SearchItemDTO[]>> getAllStars();

    @NeedAuth
    @GET("/star/{sid}")
    Observable<ResponseDTO<SearchItemDTO>> getStarById(@Path("sid") Integer id);

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
    Observable<ResponseDTO<SearchItemDTO>> deleteStar(@Path("sid") Integer id);

    @NeedAuth
    @Multipart
    @DELETE("/star/")
    Observable<ResponseDTO<OneFieldDTO.CountDTO>> deleteStars(
        @Part("id") Integer[] id
    );

    // endregion SearchItem

    // region Schedule (3)

    @NeedAuth
    @GET("/schedule/")
    Observable<ResponseDTO<OneFieldDTO.ScheduleDTO>> getSchedule();

    @Multipart
    @NeedAuth
    @PUT("/schedule/")
    Observable<ResponseDTO<OneFieldDTO.ScheduleDTO>> updateSchedule(
        @Part("schedule") String schedule,
        @Part("week") Integer currWeek
    );

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
    Observable<ResponseDTO<DocClassDTO>> getDocClassById(@Path("cid") Integer id);

    @NeedAuth
    @GET("/docclass/")
    Observable<ResponseDTO<DocClassDTO>> getDocClassByName(@Query("name") String name);

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
        @Part("id") Integer id,
        @Part("name") String name
    );

    @NeedAuth
    @DELETE("/docclass/{cid}")
    Observable<ResponseDTO<DocClassDTO>> deleteDocClass(@Path("cid") Integer id, @Query("default") Boolean isToDefault);

    // endregion DocClass

    // region Document (7)

    @NeedAuth
    @GET("/document/")
    Observable<ResponseDTO<DocumentDTO[]>> getAllDocuments();

    @NeedAuth
    @GET("/document/class/{cid}")
    Observable<ResponseDTO<DocumentDTO[]>> getDocumentByClassId(@Path("cid") Integer cid);

    @NeedAuth
    @GET("/document/{did}")
    Observable<ResponseDTO<DocumentDTO>> getDocumentById(@Path("did") Integer id);

    @NeedAuth
    @Multipart
    @POST("/document/")
    Observable<ResponseDTO<DocumentDTO>> insertDocument(
        @Part MultipartBody.Part file, // file
        @Part("doc_class_id") Integer classId
    );

    @NeedAuth
    @Multipart
    @PUT("/document/")
    Observable<ResponseDTO<DocumentDTO>> updateDocument(
        @Part("id") Integer id,
        @Part("filename") String filename,
        @Part("doc_class_id") Integer classId
    );

    @NeedAuth
    @DELETE("/document/{did}")
    Observable<ResponseDTO<DocumentDTO>> deleteDocument(@Path("did") Integer id);

    // endregion Document

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // region Raw (2)

    @NeedAuth
    @Multipart
    @POST("/raw/image")
    Observable<ResponseDTO<OneFieldDTO.FilenameDTO>> uploadImage(
        @Part MultipartBody.Part image, // image
        @Part("type") String type
    );

    /**
     * 下载文件 需要认证
     */
    @Streaming
    @NeedAuth
    @GET
    Observable<ResponseBody> downloadWithToken(
        @Header("Authorization") String token,
        @Url String uuid
    );

    /**
     * 下载文件
     */
    @Streaming
    @NeedAuth
    @GET
    Observable<ResponseBody> download(
        @Url String url
    );

    // endregion Raw

    // region Share

    // /**
    //  * 获取用户所有的共享码
    //  */
    // @NeedAuth
    // @GET("/share/")
    // Observable<ResponseDTO<ShareCodeDTO[]>> getAllShareCode();

    /**
     * 将用户文档共享
     */
    @NeedAuth
    @Multipart
    @POST("/share/")
    Observable<ResponseDTO<ShareCodeDTO>> putDocToShare(
        @Part("ex") Integer ex,
        @Part("did") Integer[] ids
    );

    /**
     * 将用户文档分组共享
     */
    @NeedAuth
    @Multipart
    @POST("/share/")
    Observable<ResponseDTO<ShareCodeDTO>> putDocClassToShare(
        @Part("ex") Integer ex,
        @Query("cid") Integer cid
    );

    // /**
    //  * 删除用户共享码
    //  */
    // @NeedAuth
    // @Multipart
    // @DELETE("/share/")
    // Observable<ResponseDTO<OneFieldDTO.CountDTO>> deleteShareCodes(
    //     @Query("sc") String[] scs
    // );

    // /**
    //  * 删除用户所有共享码
    //  */
    // @NeedAuth
    // @DELETE("/share/user")
    // Observable<ResponseDTO<OneFieldDTO.CountDTO>> deleteUserShareCodes();

    // endregion Share

}
