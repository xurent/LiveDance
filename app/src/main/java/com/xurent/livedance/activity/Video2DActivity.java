package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.http.UserService;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.model.UserInfo;
import com.xurent.livedance.utils.BitmapUtil;
import com.xurent.livedance.utils.DataUtils;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.myplayer.TimeInfoBean;
import com.xurent.myplayer.listener.MyLaoutCallBackListener;
import com.xurent.myplayer.listener.WlOnCompleteListener;
import com.xurent.myplayer.listener.WlOnErrorListener;
import com.xurent.myplayer.listener.WlOnParparedListener;
import com.xurent.myplayer.listener.WlOnPauseResumeListener;
import com.xurent.myplayer.listener.WlOnloadListener;
import com.xurent.myplayer.listener.WlOntimeInfoListener;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.opengl.GlSurfaceView;
import com.xurent.myplayer.player.WLPlayer;
import com.xurent.myplayer.util.WlTimeUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Video2DActivity extends AppCompatActivity {


    private GlSurfaceView glSurfaceView;
    private SeekBar seekBar;
    private LinearLayout linearLayout;
    private  int position;
    private  boolean seek=false;
    private TextView tv_Time;
    private ImageView playState;
    private boolean state=true;
    private boolean ontouch=false;
    private ImageView likeImg;
    private boolean likeState=false;
    private CircleImageView headImg;
    private TextView numberLike;
    private int index=0;
    private WLPlayer wlPlayer;
    private UserInfo info;
    private   boolean  slide=false;
    ArrayList<FileBean>  files=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_2d);
        seekBar=findViewById(R.id.seekbar);
        tv_Time=findViewById(R.id.tv_time);
        playState=findViewById(R.id.play_state);
        linearLayout=findViewById(R.id.linerlayout);
        likeImg=findViewById(R.id.like);
        headImg=findViewById(R.id.head);
        numberLike=findViewById(R.id.number_like);
        glSurfaceView=findViewById(R.id.video_view);
        linearLayout.setVisibility(View.GONE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position=progress*wlPlayer.getDuration()/100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seek=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                wlPlayer.seek(position);
                seek=false;
            }
        });

        wlPlayer=new WLPlayer();
        // wlPlayer=mAdapter.wlPlayer;
        wlPlayer.setWlOnParparedListener(new WlOnParparedListener() {
            @Override
            public void onParpared() {
                MyLog.d("开始播放----");
                System.out.println("播放");
                if(wlPlayer!=null){
                    wlPlayer.start();
                }
                slide=false;
            }
        });
        wlPlayer.setWlOnloadListener(new WlOnloadListener() {
            @Override
            public void onload(boolean load) {
                if(load){
                    MyLog.d("加载中....");
                }else{
                    MyLog.d("播放中....");
                }
            }
        });
        wlPlayer.setWlOnPauseResumeListener(new WlOnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if(pause){
                    MyLog.d("暂停中...");
                }else{
                    MyLog.d("播放----中。。。");
                }
            }
        });
        wlPlayer.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                MyLog.d("code:"+code+",msg:"+msg);
            }
        });
        wlPlayer.setWlOnCompleteListener(new WlOnCompleteListener() {
            @Override
            public void onComplete() {
                MyLog.d("播放完成了");
                if(wlPlayer!=null&&!slide){
                    wlPlayer.playNext(files.get(index).getUrl());
                }
            }
        });
        wlPlayer.setWlOntimeInfoListener(new WlOntimeInfoListener() {
            @Override
            public void onTimeInfo(TimeInfoBean timeInfoBean) {
                // MyLog.d(timeInfoBean.toString());
                Message msg=Message.obtain();
                msg.what=1;
                msg.obj=timeInfoBean;
                handler.sendMessage(msg);
            }
        });
        wlPlayer.setGlSurfaceView(glSurfaceView);
        glSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ontouch=!ontouch;
                if(ontouch){
                    linearLayout.setVisibility(View.VISIBLE);

                }else{
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });
        glSurfaceView.setMyLayoutCallBack(new MyLaoutCallBackListener() {
            @Override
            public void onUp() {
                slide=true;
                if(index>0){
                    wlPlayer.playNext(files.get(--index).getUrl());
                }else{
                    index=files.size();
                    wlPlayer.playNext(files.get(--index).getUrl());
                }
                Init();
                initLike();
                //slide=false;
            }

            @Override
            public void onDown() {
                slide=true;
                if (index < files.size()-1) {
                    wlPlayer.playNext(files.get(++index).getUrl());
                } else {
                    index = 0;
                    wlPlayer.playNext(files.get(index++).getUrl());
                }
                Init();
                initLike();
                //slide=false;
            }
        });


        InitAll();
    }


    private void InitAll(){
        getData();

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
            if(wlPlayer!=null){
                wlPlayer.setOk(false);
                wlPlayer.stop();
            }
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        if(wlPlayer!=null){
            wlPlayer.setOk(false);
            wlPlayer.stop();
        }
        super.onDestroy();

    }

    private void Init(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService service=retrofit.create(UserService.class);
        Call<JsonObject> call=service.getUserInfo(files.get(index).getUserId());
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    data =data.getAsJsonObject("data");
                    if(!data.get("headImg").isJsonNull()){
                        String  headUrl=data.get("headImg").getAsString();
                        Glide.with(Video2DActivity.this)
                                .load(headUrl)
                                .thumbnail(Glide.with(Video2DActivity.this)
                                        .load(headUrl))
                                .centerCrop()
                                .placeholder(R.drawable.default_tv)
                                .into(headImg);
                        Gson gson=new Gson();
                        Type type=new TypeToken<UserInfo>(){}.getType();
                         info=gson.fromJson(data.toString(),type);

                    }

                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }
    private void play(String url){
        wlPlayer.setSource(url);
        wlPlayer.parpared();
    }
    private void getData(){
        Retrofit  retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(Video2DActivity.this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.getFilesByType(2);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject object=response.body();
                System.out.println(object);
                if(object.get("code").getAsInt()==0&&!object.get("data").isJsonNull()){
                    Gson gson=new Gson();
                    Type type = new TypeToken<List<FileBean>>(){}.getType();
                    ArrayList<FileBean> file=gson.fromJson(object.get("data").getAsJsonArray().toString(),type);
                    files.addAll(file);
                    if(files.size()>0){
                        play(files.get(0).getUrl());
                        Init();
                        initLike();
                    }else{
                        glSurfaceView.setListen(false);
                        Toast.makeText(Video2DActivity.this,"没有视频数据",0).show();
                    }
                }else{
                    Toast.makeText(Video2DActivity.this,"获取数据失败",0).show();
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(Video2DActivity.this,"网络异常",0).show();
            }
        });
    }

    private void initLike(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.isCollect(files.get(index).getId());
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    if(data.get("data").getAsInt()==1){
                        likeState=true;
                        likeImg.setImageResource(R.mipmap.like);

                    }else{
                        likeState=false;
                        likeImg.setImageResource(R.mipmap.dislike);
                    }
                    numberLike.setText(files.get(index).getLikeNumber()+"");
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }


    //播放暂停
    public void ChanegState(View view) {
        state=!state;
        if(state){
            playState.setImageResource(R.mipmap.pause);
            wlPlayer.resume();
        }else{
            playState.setImageResource(R.mipmap.play);
            wlPlayer.pause();
        }
    }


    public void  touchHead(View view) {
        if(info!=null){
            Intent intent=new Intent(this,UserOtherActivity.class);
            intent.putExtra("userinfo",info);
            startActivity(intent);
            if(wlPlayer!=null){
                wlPlayer.stop();
            }
        }
    }

    //点赞
    public void LikeState(View view) {
        likeState=!likeState;
        if(likeState){
            likeImg.setImageResource(R.mipmap.like);
            Integer num=Integer.parseInt(numberLike.getText().toString())+1;
            numberLike.setText(num.toString());
            dianZan(1);
        }else {
            likeImg.setImageResource(R.mipmap.dislike);
            Integer num=Integer.parseInt(numberLike.getText().toString())-1;
            numberLike.setText(num.toString());
            dianZan(0);
        }

    }

    private void dianZan(int type){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.collection(files.get(index).getId(),type);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    Toast.makeText(Video2DActivity.this,"成功",0).show();
                }
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }


    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                TimeInfoBean timeInfoBean= (TimeInfoBean) msg.obj;
                tv_Time.setText(WlTimeUtil.secdsToDateFormat(timeInfoBean.getTotalTime(),timeInfoBean.getTotalTime())
                        +"/"+WlTimeUtil.secdsToDateFormat(timeInfoBean.getCurrentTime(),timeInfoBean.getTotalTime()));

                if(!seek&&timeInfoBean.getTotalTime()>0){
                    seekBar.setProgress(timeInfoBean.getCurrentTime()*100/timeInfoBean.getTotalTime());
                }
            }

        }
    };




    //分享
    public void share(View view) {

    }

    @Override
    protected void onStop() {
        if(wlPlayer!=null){
           wlPlayer.pause();
            playState.setImageResource(R.mipmap.pause);
            state=!state;
        }
        super.onStop();
    }

}
