package com.baibuti.biji.model.dao;

import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.FileClassDTO;
import com.baibuti.biji.model.dto.FileUrlDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.service.auth.dto.LoginDTO;
import com.baibuti.biji.service.auth.dto.RegisterDTO;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface ServerApi {

    // region Auth (3)

    // TODO Authorization 响应头
    @POST("/auth/login")
    Observable<Response<ResponseDTO<AuthRespDTO>>> login(@Body() LoginDTO loginDTO);

    @POST("/auth/logout")
    Observable<ResponseDTO<AuthRespDTO>> logout();

    @POST("/auth/register")
    Observable<ResponseDTO<AuthRespDTO>> register(@Body() RegisterDTO registerDTO);

    // endregion Auth

    // region Note (6)

    @GET("/note/all")
    Observable<ResponseDTO<NoteDTO[]>> getAllNotes();

    // TODO 接口待加
    @GET("/note/group/{id}")
    Observable<ResponseDTO<NoteDTO[]>> getNotesByGroupId(@Path("id") int id);

    @GET("/note/one/{id}")
    Observable<ResponseDTO<NoteDTO>> getNoteById(@Path("id") int id);

    @POST("/note/insert")
    Observable<ResponseDTO<NoteDTO>> insertNote(@Body() NoteDTO noteDTO);

    @PUT("/note/update")
    Observable<ResponseDTO<NoteDTO>> updateNote(@Body() NoteDTO noteDTO);

    // TODO 接口待改
    @DELETE("/note/delete/{id}")
    Observable<ResponseDTO<NoteDTO>> deleteNote(@Path("id") int id);

    // endregion Note

    // region Group (6)

    @GET("/group/all")
    Observable<ResponseDTO<GroupDTO[]>> getAllGroups();

    @GET("/group/one/{id}")
    Observable<ResponseDTO<GroupDTO>> getGroupById(@Path("id") int id);

    // TODO 接口待加
    @GET("/group/default")
    Observable<ResponseDTO<GroupDTO>> getDefaultGroup();

    @POST("/group/insert")
    Observable<ResponseDTO<GroupDTO>> insertGroup(@Body() GroupDTO groupDTO);

    @PUT("/group/update")
    Observable<ResponseDTO<GroupDTO>> updateGroup(@Body() GroupDTO groupDTO);

    // TODO 接口待改
    @DELETE("/group/delete/{id}")
    Observable<ResponseDTO<GroupDTO>> deleteGroup(@Path("id") int id);

    // endregion Group

    // region Image

    @Multipart
    @POST("/image/upload")
    Observable<ResponseDTO<FileUrlDTO>> uploadImage(@PartMap Map<String, RequestBody> requestBodyMap);

    // TODO DTO 待改
    @DELETE("/image/delete")
    Observable<ResponseDTO<FileUrlDTO>> deleteImage(@Body() FileUrlDTO fileUrlDTO);

    // endregion

    // region SearchItem (4)

    @GET("/star/all")
    Observable<ResponseDTO<SearchItemDTO[]>> getAllStars();

    @POST("/star/insert")
    Observable<ResponseDTO<SearchItemDTO>> insertStar(@Body() SearchItemDTO searchItemDTO);

    // TODO 接口待改
    @DELETE("/star/delete")
    @FormUrlEncoded
    Observable<ResponseDTO<SearchItemDTO>> deleteStar(@Field("url") String url);

    // TODO 接口待改
    @DELETE("/star/deletes")
    Observable<ResponseDTO<SearchItemDTO[]>> deleteStars(@Body() SearchItemDTO.SearchItemUrls urls);

    // endregion SearchItem

    // region Schedule (3)

    @GET("/schedule/download")
    Observable<ResponseDTO<String>> getSchedule();

    @POST("/schedule/new")
    Observable<ResponseDTO<Object>> newSchedule();

    @DELETE("/schedule/delete")
    Observable<ResponseDTO<Object>> deleteSchedule();

    // endregion Schedule

    // region FileClass (7)

    @GET("/fileclass/all")
    Observable<ResponseDTO<FileClassDTO[]>> getAllFileClasses();

    // TODO 接口待加
    @GET("/fileclass/one/{id}")
    Observable<ResponseDTO<FileClassDTO>> getFileClassById(@Path("id") int id);

    // TODO 接口待加
    @GET("/group/default")
    Observable<ResponseDTO<FileClassDTO>> getDefaultFileClass();

    @POST("/fileclass/insert")
    Observable<ResponseDTO<FileClassDTO>> insertFileClass(@Body() FileClassDTO fileClassDTO);

    @PUT("/fileclass/update")
    Observable<ResponseDTO<FileClassDTO>> updateFileClass(@Body() FileClassDTO fileClassDTO);

    // TODO 接口待改
    @DELETE("/fileclass/delete/{id}")
    Observable<ResponseDTO<FileClassDTO>> deleteFileClass(@Path("id") int id);

    // TODO 接口待改
    // @GET("/fileclass/share")
    // Observable<ResponseDTO<>> getShareCode(@Path("id") int id);

    // endregion FileClass

    // region Document (7)

    @GET("/file/all")
    Observable<ResponseDTO<DocumentDTO[]>> getAllDocuments();

    // TODO 接口待加
    @GET("/file/class/{class}")
    Observable<ResponseDTO<DocumentDTO[]>> getDocumentsByFileClass(@Path("class") String fileClass);

    // TODO 接口待改
    @GET("/file/{id}")
    Observable<ResponseDTO<DocumentDTO>> getDocumentById(@Path("id") int id);

    @POST("/file/insert")
    Observable<ResponseDTO<DocumentDTO>> insertDocument(@Body() DocumentDTO documentDTO);

    @PUT("/file/update")
    Observable<ResponseDTO<DocumentDTO>> updateDocument(@Body() DocumentDTO documentDTO);

    // TODO 接口待改
    @DELETE("/file/delete/{id}")
    Observable<ResponseDTO<DocumentDTO>> deleteDocument(@Path("id") int id);

    // TODO 接口待改
    // @GET("/file/get_share")
    // Observable<ResponseDTO<>> getShareCode(@Path("id") int id);

    // endregion Document

}
