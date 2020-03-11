package com.xurent.livedance.http;



import com.google.gson.JsonObject;
import com.xurent.livedance.common.Constants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthorizationService {
    @FormUrlEncoded
    @POST("register")
    public Call<JsonObject> register(@Field("username") String phone, @Field("password") String pwd);


    @FormUrlEncoded
    @POST("login")
    public Call<JsonObject> login(@Field("username") String username, @Field("password") String pwd);

    @GET("logout")
    public Call<JsonObject> logout();

    @GET("expire")
    public Call<JsonObject> expire(@Header("Cookie")String cookie);

}
