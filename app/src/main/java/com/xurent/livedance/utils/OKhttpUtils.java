package com.xurent.livedance.utils;

import android.content.Context;
import com.xurent.livedance.utils.cookie.AddCookiesInterceptor;
import com.xurent.livedance.utils.cookie.ReceivedCookiesInterceptor;

import okhttp3.OkHttpClient;

public class OKhttpUtils {

    private  static OkHttpClient okHttpClient;


    public static OkHttpClient getOkHttpClient(Context context) {

        if(okHttpClient==null){
            synchronized (Object.class){
                if(okHttpClient==null){
                   /* ClearableCookieJar cookieJar =
                            new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));*/
                    okHttpClient = new OkHttpClient.Builder()
                           // .cookieJar(cookieJar)
                            .addInterceptor(new AddCookiesInterceptor(context))
                            .addInterceptor(new ReceivedCookiesInterceptor(context))
                            .build();
                }
            }
        }
        return okHttpClient;
    }


}
