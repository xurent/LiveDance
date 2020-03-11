package com.xurent.livedance.http;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileService {

    @Multipart
    @Streaming
    @POST("upload")
    public Call<JsonObject> uploadFile(@Part MultipartBody.Part file,@Part("type") RequestBody type);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String s);

    @GET("like")
    public Call<JsonObject> collection(@Query("workId") Integer wid,@Query("make") Integer type);//1点赞,0取消


    @GET("mylikes")
    public Call<JsonObject> mylikes();

    @GET("myworks")
    public Call<JsonObject> myWorks();

    @GET("getall")
    public Call<JsonObject> getAll();


    @GET("iscollect")
    public Call<JsonObject> isCollect(@Query("wid") Integer wid);


    @GET("Herlikes")
    public Call<JsonObject> Herlikes(@Query("uid") String uid);

    @GET("works_by_uid")
    public Call<JsonObject> getHer(@Query("uid") String uid);



    @GET("get_files_by_type")
    public Call<JsonObject> getFilesByType(@Query("type") Integer type);

}
