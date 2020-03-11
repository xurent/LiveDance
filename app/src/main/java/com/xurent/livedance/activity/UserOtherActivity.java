package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.model.UserInfo;
import com.xurent.livedance.utils.OKhttpUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserOtherActivity extends AppCompatActivity {

    private CircleImageView headImg;
    private TextView nickname;
    private TextView userId;
    private TextView introduction;
    private List<FileBean> lists=new ArrayList<>();
    private MyAdapter adapter;
    private UserInfo info;
    private LiveRoomBean room;
    private ListView iv;
    private TabLayout tabLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruser);
        iv=findViewById(R.id.iv);
        tabLayout=findViewById(R.id.tabLayoutHome);
        headImg=findViewById(R.id.headImg);
        nickname=findViewById(R.id.nickname);
        userId=findViewById(R.id.userId);
        introduction=findViewById(R.id.introduction);
         info= (UserInfo) getIntent().getSerializableExtra("userinfo");
         System.out.println(info.toString());
        nickname.setText(info.getNickName());
        userId.setText("ID: "+info.getUserName());
        introduction.setText(info.getIntroduction());
        Glide.with(this)
                .load(info.getHeadImg())
                .thumbnail(Glide.with(this)
                        .load(info.getHeadImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(headImg);
        adapter=new MyAdapter();
        iv.setAdapter(adapter);
        getMyWorks();
        getData();
        Listner();
    }

    @SuppressLint("WrongConstant")
    public void liveRoom(View view) {
        if(room==null){
            Toast.makeText(this,"对方未创建直播间",0).show();
            return;
        }
        Intent intent=new Intent(this, LiveRoomActivity.class);
        intent.putExtra("anchor",room);
        startActivity(intent);
    }




    private  void getData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getMyRoom(info.getUserName());
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                int code=data.get("code").getAsInt();
                if(code==0){
                    if( !data.get("data").isJsonNull()){
                        Gson gson=new Gson();
                        Type type = new TypeToken<LiveRoomBean>(){}.getType();
                        room=gson.fromJson(data.get("data").toString(),type);
                        System.out.println(room.toString());
                    }
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    private void getMyWorks(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.getHer(info.getUserName());
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data.toString());
                if(data.get("code").getAsInt()==0&&!data.get("data").isJsonNull()){
                    Gson gson=new Gson();
                    Type type = new TypeToken<List<FileBean>>(){}.getType();
                    ArrayList<FileBean> files=gson.fromJson(data.get("data").getAsJsonArray().toString(),type);
                    lists.clear();
                    lists.addAll(files);
                    System.out.println(lists.size());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    private void getMyLike(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.Herlikes(info.getUserName());
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0&&!data.get("data").isJsonNull()){
                    Gson gson=new Gson();
                    Type type = new TypeToken<List<FileBean>>(){}.getType();
                    ArrayList<FileBean> files=gson.fromJson(data.get("data").getAsJsonArray().toString(),type);
                    lists.clear();
                    lists.addAll(files);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("获取作品网络异常");
            }
        });
    }



    public void Listner(){

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        getMyWorks();
                        break;
                    case 1:
                        getMyLike();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return lists.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View tv =View.inflate(UserOtherActivity.this, R.layout.ctrl_list, null);
            ImageView  img=tv.findViewById(R.id.work1);
            TextView number=tv.findViewById(R.id.love_number);
            number.setText(lists.get(i).getLikeNumber()+"");
            String URL= lists.get(i).getUrl();
            //final Bitmap bitmap= BitmapUtil.createVideoThumbnail(DataUtils.getDatas().get(i).url,200,200);
            Glide.with(viewGroup.getContext())
                    .load(URL)
                    .thumbnail(Glide.with(viewGroup.getContext())
                            .load(URL))
                    .centerCrop()
                    .placeholder(R.drawable.default_tv)
                    .into(img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(UserOtherActivity.this, PlayerX.class);
                    intent.putExtra("fileBean",lists.get(i));
                    startActivity(intent);
                }
            });
            return tv;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(lists!=null){
            lists.clear();;
            lists=null;
        }
        userId=null;
        info=null;
        room=null;
        adapter=null;
    }
}
