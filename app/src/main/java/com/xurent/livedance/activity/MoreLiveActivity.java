package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.ui.adapter.LiveAdapter;
import com.xurent.livedance.utils.OKhttpUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MoreLiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LiveAdapter liveAdapter;
    LinkedList<LiveRoomBean> Datas=new LinkedList<>();
    private Retrofit retrofit=null;
    private TextView tv_msg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_live);
        recyclerView=findViewById(R.id.recycle_more_live);
        liveAdapter=new LiveAdapter(this,Datas);
        tv_msg=findViewById(R.id.tv_msg);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(liveAdapter);
        init();
    }

    private  void init(){

        initNet();//初始化网络
        getData();//初始化数据
    }

    private  void getData(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getAllRoom(1,20);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                int code=data.get("code").getAsInt();
                if(code==0){
                    if( !data.get("data").isJsonNull()){
                        JsonObject obj=data.getAsJsonObject("data");
                        JsonArray array=obj.getAsJsonArray("content");
                        Gson gson=new Gson();
                        Type type = new TypeToken<List<LiveRoomBean>>(){}.getType();
                        ArrayList<LiveRoomBean> datas=gson.fromJson(array.toString(), type);
                        for(LiveRoomBean bean:datas){
                            System.out.println(bean.toString());
                        }
                        Datas.addAll(datas);
                        liveAdapter.notifyDataSetChanged();
                        if(Datas.size()==0){
                            tv_msg.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    Toast.makeText(MoreLiveActivity.this,data.get("msg").getAsString(),0).show();
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    private void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .build();
    }

}
