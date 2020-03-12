package com.xurent.livedance.LiveRoom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.JsonObject;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.myplayer.listener.WlOnCompleteListener;
import com.xurent.myplayer.listener.WlOnErrorListener;
import com.xurent.myplayer.listener.WlOnParparedListener;
import com.xurent.myplayer.listener.WlOnPauseResumeListener;
import com.xurent.myplayer.listener.WlOnloadListener;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.opengl.GlSurfaceView;
import com.xurent.myplayer.player.WLPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 直播界面，用于对接直播功能
 */
public class LiveFrag extends Fragment implements OnOpenLiveListner{

    private static final int VIDEO_LOAD =0;
    private static final int VIDEO_PLAY=1;
    private GlSurfaceView video_view;
    private WLPlayer wlPlayer;
    private Retrofit retrofit;
    String anchorId;
    private boolean isload=false;
    private int canPlay=0;
    private ProgressDialog pd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_live, null);
        video_view = view.findViewById(R.id.video_view);
        LiveRoomActivity activity= (LiveRoomActivity) getActivity();
        anchorId=activity.data.getUsername();
      /*  pd=new ProgressDialog(getContext());
        pd.setMessage("正在加载中...");*/
        init();
        return view;
    }

    private void init(){
        initPlayer();
        initNet();
        InitData();
    }

    public void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

    }
    private void InitData(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject>call=service.getLiveUrl(anchorId);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                int code=data.get("code").getAsInt();
                System.out.println(data.toString());
                if(code==0){
                    String url=data.getAsJsonObject("data").get("playUrlRtmp").getAsString();
                    play(url);
                }else {
                    Toast.makeText(getContext(),"主播暂未开播",0).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }

    private void play(String url){
        wlPlayer.setSource(url);
        wlPlayer.parpared();
        System.out.println(url);
    }


    private void initPlayer(){
        wlPlayer=new WLPlayer();
        wlPlayer.setWlOnParparedListener(new WlOnParparedListener() {
            @Override
            public void onParpared() {
                MyLog.d("直播播放----");
                System.out.println("播放");
               if(wlPlayer!=null){
                   wlPlayer.start();
               }
                canPlay=1;
            }
        });
        wlPlayer.setWlOnloadListener(new WlOnloadListener() {
            @Override
            public void onload(boolean load) {
                Message message=Message.obtain();
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
                //reload.setVisibility(View.VISIBLE);
            }
        });
        wlPlayer.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {

            }
        });
        wlPlayer.setGlSurfaceView(video_view);
    }


    private Handler handler=new Handler(){
        @SuppressLint("WrongConstant")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           switch (msg.what){
               case VIDEO_LOAD:
                   if (canPlay!=0){
                       pd.show();
                       isload=true;
                   }else{
                       Toast.makeText(getContext(),"主播未开播",0).show();
                   }
                   break;
               case VIDEO_PLAY:
                   pd.dismiss();
                   isload=false;
                   break;
           }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wlPlayer!=null){
            wlPlayer.stop();
            wlPlayer=null;
        }
    }

    /**
     * 房主进入房间，开始直播
     */
    @Override
    public void openLive() {
        InitData();
    }
}