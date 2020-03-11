package com.xurent.livedance.http;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RoomService {

    @Multipart
    @POST("update")
    public Call<JsonObject> uploadHeadImg(@Part("title") RequestBody title, @Part("kind") RequestBody kind, @Part("announcement") RequestBody announcement, @Part MultipartBody.Part file);

    @GET("get")
    public Call<JsonObject> getMyRoom(@Query("userId")String uid);

    @GET("getAll")
    public Call<JsonObject> getAllRoom(@Query("page")Integer page,@Query("limit")Integer limit);

    @GET("getbykind")
    public Call<JsonObject> getAllRoomByKind(@Query("kind")String kind);

    @GET("getLiveUrl")
    public Call<JsonObject> getLiveUrl(@Query("uid")String uid);

    @GET("open")
    public Call<JsonObject> openLive();

    @GET("isFocus")
    public Call<JsonObject> isFocus(@Query(("aid"))String aid);

    @GET("likeAnchor")
    public Call<JsonObject> FoucusAnchor(@Query(("aid")) String aid, @Query(("type"))Integer type);//type 0取消关注，1关注

    @GET("getTotal")
    public Call<JsonObject> getFansAndOther(@Query("aid")String aid,@Query("type") Integer type);//0粉丝榜，1打赏榜

    @GET("getAnchors")
    public Call<JsonObject> getFocusAnchor();//获取关注的主播

    @GET("myAcount")
    public Call<JsonObject> myAcount();//

    @FormUrlEncoded
    @POST("giveGift")
    public Call<JsonObject> giveGift(@Field("aid") String aid,@Field("acount") long acount);


}
