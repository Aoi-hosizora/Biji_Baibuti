package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.service.auth.dto.LoginDTO;
import com.baibuti.biji.service.auth.dto.RegisterDTO;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ServerApi {

    // region Auth (3)

    @POST("/auth/login")
    Observable<ResponseDTO<AuthRespDTO>> login(@Body() LoginDTO loginDTO);

    @POST("/auth/logout")
    Observable<ResponseDTO<AuthRespDTO>> logout();

    @POST("/auth/register")
    Observable<ResponseDTO<AuthRespDTO>> register(@Body() RegisterDTO registerDTO);

    // endregion Auth

    // region Note (5)

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

    // region Group (5)

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

}
