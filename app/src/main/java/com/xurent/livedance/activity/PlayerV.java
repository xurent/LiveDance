package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.xurent.livedance.model.Bean;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.model.UserInfo;
import com.xurent.livedance.utils.BitmapUtil;
import com.xurent.livedance.utils.DataUtils;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.livedance.widget.PagerLayoutManager;
import com.xurent.myplayer.TimeInfoBean;
import com.xurent.myplayer.listener.MyLaoutCallBackListener;
import com.xurent.myplayer.listener.WlOnCompleteListener;
import com.xurent.myplayer.listener.WlOnErrorListener;
import com.xurent.myplayer.listener.WlOnParparedListener;
import com.xurent.myplayer.listener.WlOnPauseResumeListener;
import com.xurent.myplayer.listener.WlOnloadListener;
import com.xurent.myplayer.listener.WlOntimeInfoListener;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.player.WLPlayer;
import com.xurent.myplayer.util.WlTimeUtil;
import com.xurent.myplayer.vr.VrSurfaceView;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlayerV extends AppCompatActivity implements View.OnClickListener{


    private ImageView changeDisplayModeBtn;
    private ImageView changeInteRactionModeBtn;

    private ArrayList<Bean> mDatas = new ArrayList<>();

    private VrSurfaceView vrSurfaceView;
    private WLPlayer player;
    private SensorManager sensorManager;
    private Sensor mRotation;
    private final float[] mRotateMatrix = new float[16];
    private float[] mTempRotateMatrix = new float[16];
    private int mDeviceRotation = Surface.ROTATION_90;
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
    private FileBean fileBean;
    private boolean glasses=false;
    private boolean state_3d=true;

    private UserInfo info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_vr_glsurface_view);
        seekBar=findViewById(R.id.seekbar);
        tv_Time=findViewById(R.id.tv_time);
        playState=findViewById(R.id.play_state);
        linearLayout=findViewById(R.id.linerlayout);
        likeImg=findViewById(R.id.like);
        headImg=findViewById(R.id.head);
        numberLike=findViewById(R.id.number_like);
        fileBean= (FileBean) getIntent().getSerializableExtra("fileBean");
        numberLike.setText(fileBean.getLikeNumber()+"");
        linearLayout.setVisibility(View.GONE);



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position=progress*player.getDuration()/100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seek=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seek(position);
                seek=false;
            }
        });

        initView();
        Init();
        initLike();
        initSensor();
    }

    //传感器的数据监听
    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotation=sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                setRotateMatrix(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, mRotation, SensorManager.SENSOR_DELAY_GAME);
    }

    private void setRotateMatrix(SensorEvent event) {
        //Log.e("setRotateMatrix","setRotateMatrix");
        //横竖屏转换处理
        SensorManager.getRotationMatrixFromVector(mTempRotateMatrix, event.values);
        float[] values = event.values;
        switch (mDeviceRotation){
            case Surface.ROTATION_0:
                SensorManager.getRotationMatrixFromVector(mRotateMatrix, values);
                break;
            case Surface.ROTATION_90:
                SensorManager.getRotationMatrixFromVector(mTempRotateMatrix, values);
                SensorManager.remapCoordinateSystem(mTempRotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotateMatrix);
                break;
        }

        vrSurfaceView.getVrGlassGLVideoRenderer().setuRotateMatrix(mRotateMatrix);
    }

    private void initView() {
        changeDisplayModeBtn = findViewById(R.id.change_display_mode_btn);
        changeInteRactionModeBtn = findViewById(R.id.change_interaction_mode_btn);
        changeDisplayModeBtn.setOnClickListener(this);
        changeInteRactionModeBtn.setOnClickListener(this);

        PagerLayoutManager mLayoutManager = new PagerLayoutManager(this, OrientationHelper.VERTICAL);
        mDatas.addAll(DataUtils.getDatas());

        vrSurfaceView=findViewById(R.id.play_vr_glsv);
        vrSurfaceView.setListen(false);
        vrSurfaceView.setOnClickListener(new View.OnClickListener() {
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

        player=new WLPlayer();
        player.setWlOnParparedListener(new WlOnParparedListener() {
            @Override
            public void onParpared() {
                MyLog.d("开始播放----");
                System.out.println("播放");
                player.start();
            }
        });
        player.setWlOnloadListener(new WlOnloadListener() {
            @Override
            public void onload(boolean load) {
                if(load){
                    MyLog.d("加载中....");
                }else{
                    MyLog.d("播放中....");
                }
            }
        });
        player.setWlOnPauseResumeListener(new WlOnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if(pause){
                    MyLog.d("暂停中...");
                }else{
                    MyLog.d("播放----中。。。");
                }
            }
        });
        player.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                MyLog.d("code:"+code+",msg:"+msg);
            }
        });
        player.setWlOnCompleteListener(new WlOnCompleteListener() {
            @Override
            public void onComplete() {
                MyLog.d("播放完成了");
            }
        });
        player.setWlOntimeInfoListener(new WlOntimeInfoListener() {
            @Override
            public void onTimeInfo(TimeInfoBean timeInfoBean) {
                // MyLog.d(timeInfoBean.toString());
                Message msg=Message.obtain();
                msg.what=1;
                msg.obj=timeInfoBean;
                handler.sendMessage(msg);
            }
        });
        player.setOpenVr(true);
        player.setVrSurfaceView(vrSurfaceView);
        player.setSource(fileBean.getUrl());
        player.parpared();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mDeviceRotation = windowManager.getDefaultDisplay().getRotation();

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
                        Glide.with(PlayerV.this)
                                .load(headUrl)
                                .thumbnail(Glide.with(PlayerV.this)
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

    public void  touchHead(View view) {
        if(info!=null){
            Intent intent=new Intent(this,UserOtherActivity.class);
            intent.putExtra("userinfo",info);
            startActivity(intent);
            if(player!=null){
                player.stop();
            }
        }
    }

    @Override
    protected void onStop() {
        if(player!=null){
            player.stop();
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_display_mode_btn:
                changeDisplayMode();
                break;
            case R.id.change_interaction_mode_btn:
                changeInteRactionMode();
                break;
        }
    }

    private void changeInteRactionMode() {
        vrSurfaceView.getVrGlassGLVideoRenderer().changeInteractionMode();
        if(changeInteRactionModeBtn==null)return;
        state_3d=!state_3d;
        if(state_3d){
            changeInteRactionModeBtn.setImageResource(R.mipmap.on_3d);
        }else {
            changeInteRactionModeBtn.setImageResource(R.mipmap.off_3d);
        }
    }

    private void changeDisplayMode() {
        vrSurfaceView.getVrGlassGLVideoRenderer().changeDisplayMode();
        glasses=!glasses;
        if(glasses){
            changeDisplayModeBtn.setImageResource(R.mipmap.glasses_3d);
        }else{
            changeDisplayModeBtn.setImageResource(R.mipmap.glasses_2d);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
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

    //暂停播放
    public void ChanegState(View view) {
        state=!state;
        if(state){
            playState.setImageResource(R.mipmap.pause);
            player.resume();
        }else{
            playState.setImageResource(R.mipmap.play);
            player.pause();
        }

    }

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

                    Toast.makeText(PlayerV.this,"成功",0).show();

                }
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    //用户
    public void AboutUser(View view) {
        if(player!=null){
            player.stop();
        }
        Intent intent=new Intent(this, UserOtherActivity.class);
        startActivity(intent);
    }

    //转发
    public void send(View view) {

    }
}
