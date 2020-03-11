package com.xurent.livedance.http;

import com.google.gson.JsonObject;
import com.xurent.livedance.common.Constants;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {

    @FormUrlEncoded
    @POST("updateUserInfo")
    public Call<JsonObject> updateUser(@Field("nickName") String name, @Field("Introduction") String jiesao, @Field("headImg") String img);

    @GET("getMyInfo")
    public Call<JsonObject> getMyInfo();

    @GET("getUserInfo")
    public Call<JsonObject> getUserInfo(@Query("userId") String uid);

    @Multipart
    @POST("uploadImage")
    public Call<JsonObject> uploadHeadImg( @Part MultipartBody.Part file);

}
