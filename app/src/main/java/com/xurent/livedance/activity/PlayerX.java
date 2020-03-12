package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.myplayer.TimeInfoBean;
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

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlayerX extends Activity {

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
    private WLPlayer wlPlayer;
    private FileBean fileBean;
    private UserInfo info;
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
        glSurfaceView.setListen(false);
        fileBean= (FileBean) getIntent().getSerializableExtra("fileBean");
        numberLike.setText(fileBean.getLikeNumber()+"");
        Init();
        initLike();
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
               if(wlPlayer!=null){
                   wlPlayer.playNext(fileBean.getUrl());
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
        wlPlayer.setSource(fileBean.getUrl());
        wlPlayer.parpared();
    }


    private void Init(){
       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        UserService service=retrofit.create(UserService.class);
        Call<JsonObject> call=service.getUserInfo(fileBean.getUserId());
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
                        Glide.with(PlayerX.this)
                                .load(headUrl)
                                .thumbnail(Glide.with(PlayerX.this)
                                        .load(headUrl))
                                .centerCrop()
                                .placeholder(R.drawable.default_tv)
                                .into(headImg);
                        Gson gson=new Gson();
                        Type type=new TypeToken<UserInfo>(){}.getType();
                        info=gson.fromJson(data.toString(),type);
                        System.out.println(info.toString());
                    }

                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

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
        Call<JsonObject> call=service.isCollect(fileBean.getId());
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
                }
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }

    @Override
    protected void onStop() {
        if(wlPlayer!=null){
            wlPlayer.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(wlPlayer!=null){
            wlPlayer.stop();
            wlPlayer=null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//
            if(wlPlayer!=null){
                wlPlayer.setOk(false);
                wlPlayer.stop();
                wlPlayer=null;
            }
        }
        return super.onKeyDown(keyCode, event);
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
        Call<JsonObject> call=service.collection(fileBean.getId(),type);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){

                        Toast.makeText(PlayerX.this,"成功",0).show();

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



    //用户详情
    public void DeepAbout(View view) {
        if(wlPlayer!=null){
            wlPlayer.stop();
        }
        Intent intent=new Intent(this, UserOtherActivity.class);
        startActivity(intent);
    }

    //分享
    public void share(View view) {

    }



}
