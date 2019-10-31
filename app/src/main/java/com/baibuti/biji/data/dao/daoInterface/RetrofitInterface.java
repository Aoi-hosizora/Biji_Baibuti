package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.dto.GroupDTO;
import com.baibuti.biji.data.dto.NoteDTO;
import com.baibuti.biji.data.dto.SearchItemDTO;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitInterface {

    // region Note (5)

    @GET("/note/all")
    Observable<NoteDTO[]> getAllNotes();

    @GET("/note/one/{id}")
    Observable<NoteDTO> getNoteById(@Path("id") int id);

    @POST("/note/insert")
    Observable<NoteDTO> insertNote(@Body() NoteDTO noteDTO);

    @PUT("/note/update")
    Observable<NoteDTO> updateNote(@Body() NoteDTO noteDTO);

    // TODO 接口待改
    @DELETE("/note/delete/{id}")
    Observable<NoteDTO> deleteNote(@Path("id") int id);

    // endregion Note

    // region Group (5)

    @GET("/group/all")
    Observable<GroupDTO[]> getAllGroups();

    @GET("/group/one/{id}")
    Observable<GroupDTO> getGroupById(@Path("id") int id);

    @POST("/group/insert")
    Observable<GroupDTO> insertGroup(@Body() GroupDTO groupDTO);

    @PUT("/group/update")
    Observable<GroupDTO> updateGroup(@Body() GroupDTO groupDTO);

    // TODO 接口待改
    @DELETE("/group/delete/{id}")
    Observable<GroupDTO> deleteGroup(@Path("id") int id);

    // endregion Group

    // region SearchItem (4)

    @GET("/star/all")
    Observable<SearchItemDTO[]> getAllStars();

    @POST("/star/insert")
    Observable<SearchItemDTO> insertStar(@Body() SearchItemDTO searchItemDTO);

    // TODO 接口待改
    @DELETE("/star/delete")
    @FormUrlEncoded
    Observable<SearchItemDTO> deleteStar(@Field("url") String url);

    // TODO 接口待改
    @DELETE("/star/deletes")
    Observable<SearchItemDTO[]> deleteStars(@Body() SearchItemDTO.SearchItemUrls urls);

    // endregion SearchItem
}
