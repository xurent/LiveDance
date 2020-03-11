package com.xurent.livedance.http;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface MusicService {

    @GET("getMusic")
    public Call<JsonObject> getMusics();

    @Streaming
    @GET
    public Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
