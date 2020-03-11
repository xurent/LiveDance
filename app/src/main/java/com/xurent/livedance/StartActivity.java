package com.xurent.livedance;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.AuthorizationService;
import com.xurent.livedance.utils.OKhttpUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class StartActivity extends Activity {

    private Handler handler = new Handler();
    private SharedPreferences sp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp=this.getSharedPreferences("config", Context.MODE_PRIVATE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 2000);
        Constants.token=sp.getString("token","");
        if(!Constants.token.isEmpty()){//判断登录状态是否有效
            System.out.println("已登录");
            Initconfig(); //获取网络初始化
        }
    }

    private void Initconfig() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        AuthorizationService service=retrofit.create(AuthorizationService.class);
        Call<JsonObject> call=service.expire(sp.getString("token",""));
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                Headers header=response.headers();
                String cookie=header.get("Set-Cookie");
                System.out.println(data+","+response.message()+","+cookie);
                if(data.get("code").getAsInt()==0){
                    Constants.LOGIN=true;
                    Constants.token=sp.getString("token","");
                    if(data.get("data").isJsonNull()){
                        return;
                    }
                    JsonObject info=data.getAsJsonObject("data");
                    if(!info.get("username").isJsonNull()){
                        Constants.uid=info.get("username").getAsString();
                    }
                   if(!info.get("nickName").isJsonNull()){
                       Constants.nickName=info.get("nickName").getAsString();
                   }

                    if(!info.get("headImg").isJsonNull()){
                        Constants.headImage=info.get("headImg").getAsString();
                    }
                }else {
                    Constants.LOGIN=false;
                    sp.edit().putString("token","");
                    sp.edit().commit();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("连接失败");
            }
        });

    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            //If token is null, all callbacks and messages will be removed.
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }


}
