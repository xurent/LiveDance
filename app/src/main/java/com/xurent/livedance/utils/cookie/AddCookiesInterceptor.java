package com.xurent.livedance.utils.cookie;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;


import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Action1;

public class AddCookiesInterceptor implements Interceptor {


    private Context context;


    public AddCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request.Builder builder = chain.request().newBuilder();
        SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        Observable.just(sharedPreferences.getString("token", ""))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String cookie) {
                        //添加cookie
                        builder.addHeader("Cookie", cookie);
                    }
                });
        return chain.proceed(builder.build());
    }
}
