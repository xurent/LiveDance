package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.encodec.MediaEncodec;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.imgvideo.WlImgVideoView;
import com.xurent.livedance.model.Music;
import com.xurent.livedance.ui.adapter.MusicAdapter;

import com.xurent.livedance.utils.FileUtil;
import com.xurent.livedance.utils.GlideLoadEngine;
import com.xurent.livedance.utils.OKhttpUtils;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageActivity extends AppCompatActivity {
    private static final int DOWN_FIINISH = 0;
    private static final int DOWN_FAIL = 1;
    private static final int UPDATE_TIME = 2;
    private static final int REQUEST_CODE_SELECT_IMG = 3;
    private static final int MAX_SELECT_COUNT = 9;

    private WlImgVideoView wlImgVideoView;
    private MediaEncodec wlMediaEncodec;
    private WlMusic wlMusic;



    private MaterialButton btnRecord;
    private String  mTempPath;
    private MaterialButton upload;
    private ProgressDialog pd;
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private List<Music> musics=new ArrayList<>();
    private Retrofit retrofit ;
    private String url= "https://sharefs.yun.kugou.com/202003081537/92d212d95e19cd9f71ec86842c0c9966/G189/M00/17/03/XYcBAF4NnfOAdKH5ADn3W1bs4L0215.mp3";
    private SharedPreferences sp;
    private TextView tv_time;
    private boolean isStart=true;
    private UpdateLiveTime updateLiveTime; //更新直播时间
    private int Time=40;
    List<String> paths=new ArrayList<>();
    private boolean stop=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagevideo);
        wlImgVideoView = findViewById(R.id.cameraview);
        wlImgVideoView.setCurrentImg(R.drawable.head);

        btnRecord=findViewById(R.id.btn_record);
        upload=findViewById(R.id.upload);
        recyclerView=findViewById(R.id.music_recycle);
        tv_time=findViewById(R.id.time);
        pd = new ProgressDialog(this);
        sp=this.getSharedPreferences("config", Context.MODE_PRIVATE);
        adapter=new MusicAdapter(this,musics);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        layoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemSelect(new MusicAdapter.OnItemSelect() {
            @Override
            public void OnItemselect(View v, int position) {
                Music music=musics.get(position);
                File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LiveDance/music" + File.separator,musics.get(position).getName()+".mp3");
                if(!fileDir.exists()||fileDir.getTotalSpace()<1024*1024){
                    pd.setMessage("正在下载...");
                    pd.setCancelable(false);
                    pd.show();
                    downFile(music.getUrl(),music.getName());
                }else {
                    System.out.println(fileDir.getPath()+","+fileDir.getTotalSpace());
                    url=fileDir.getAbsolutePath();
                }
                System.out.println(url);
            }
        });
        InitNet();
        initBackgroudMusic();
        getMusicData();
        updateLiveTime=new UpdateLiveTime();


    }

    private void initBackgroudMusic(){
        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);
        File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LiveDance/Video" + File.separator);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File VedioFile = new File(fileDir, new Date().getTime()+".mp4");
        mTempPath = VedioFile.getAbsolutePath();

        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(5, 45);
            }
        });

        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                wlMediaEncodec = new MediaEncodec(ImageActivity.this, wlImgVideoView.getFbotextureid());
                wlMediaEncodec.initEncodec(wlImgVideoView.getEglContext(),
                        mTempPath,
                        720, 1280, samplerate, channels);
                wlMediaEncodec.startRecord();
                startImgs();

            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if(wlMediaEncodec != null)
                {
                    wlMediaEncodec.putPCMData(pcmdata, size);
                }
            }
        });

        wlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                stop=true;
                if( wlMediaEncodec!=null){
                    wlMediaEncodec.stopRecord();
                    wlMusic.stop();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRecord.setText("重新合成");
                            upload.setVisibility(View.VISIBLE);
                            isStart=false;
                            Time=40;
                        }
                    });
                }
            }
        });


    }


    private boolean playing=false;


    @SuppressLint("WrongConstant")
    public void start(View view) {

        if(paths.size()<1){
            Toast.makeText(this,"请选择图片",0).show();
            return;
        }

        if(!playing){
            wlMusic.setSource(url);
            wlMusic.prePared();
            btnRecord.setText("正在合成");
            playing=true;
            recyclerView.setVisibility(View.GONE);
            upload.setVisibility(View.GONE);
            tv_time.setVisibility(View.VISIBLE);
            isStart=true;
            stop=false;
            new Thread(updateLiveTime).start();

        }else {
            tv_time.setVisibility(View.GONE);
            stop=true;
            isStart=false;
            Time=40;
            playing=true;
            wlMediaEncodec.stopRecord();
            btnRecord.setText("重新合成");
            wlMediaEncodec=null;
            wlMusic.stop();

        }

    }

    private void getMusicData(){
        Music music=new Music();
        music.setName("世界那么大还是遇见你");
        music.setUrl("https://sharefs.yun.kugou.com/202003081537/92d212d95e19cd9f71ec86842c0c9966/G189/M00/17/03/XYcBAF4NnfOAdKH5ADn3W1bs4L0215.mp3");
        music.setImageUrl("http://y.gtimg.cn/music/photo_new/T002R300x300M0000012N9hL04EZke_1.jpg?max_age=2592000");
        music.setNativeUrl(sp.getString(music.getName(),""));
        musics.add(music);
        Music music1=new Music();
        music1.setName("等你下课");
        music1.setUrl("https://sharefs.yun.kugou.com/202003081251/5e59112fcfcf10dcd521b4067b3e2e85/G148/M09/0C/0D/1A0DAFviWPyAX0NVAELJjAwqF7U759.mp3");
        music1.setImageUrl("https://dss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=2337860021,588696903&fm=26&gp=0.jpg");
        music1.setNativeUrl(sp.getString(music1.getName(),""));
        musics.add(music1);
        Music music2=new Music();
        music2.setName("蓝");
        music2.setUrl("https://sharefs.yun.kugou.com/202003081529/2dbb06c9717ad08f203a69dc6047aba6/G205/M06/11/18/rZQEAF5A2V6ANBLFAEJnWwvn19Q770.mp3");
        music2.setImageUrl("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=258582879,3667948452&fm=26&gp=0.jpg");
        music2.setNativeUrl(sp.getString(music2.getName(),""));
        musics.add(music2);
        Music music3=new Music();
        music3.setName("追");
        music3.setUrl("https://sharefs.yun.kugou.com/202003081254/fe14e5c19907b023f145df295b3ba534/G184/M09/19/19/-A0DAF3HJIiAb-RmADWHB5Wlh9o288.mp3");
        music3.setImageUrl("https://dss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3896663217,4038164157&fm=26&gp=0.jpg");
        music3.setNativeUrl(sp.getString(music3.getName(),""));
        musics.add(music3);
        adapter.notifyDataSetChanged();
        File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LiveDance/music" + File.separator,musics.get(0).getName()+".mp3");
        if(!fileDir.exists()||fileDir.getTotalSpace()<1024*1024){
            pd.setMessage("正在初始化数据...");
            pd.setCancelable(false);
            pd.show();
            downFile(music.getUrl(),music.getName());
        }else {
            System.out.println(fileDir.getPath()+","+fileDir.getTotalSpace());
            url=fileDir.getAbsolutePath();
        }

    }

    private void downFile( String urlx,String name){
        FileUtil fileUtil=new FileUtil();
        FileService service=retrofit.create(FileService.class);
        new Thread(){
            @Override
            public void run() {
                super.run();
                Call<ResponseBody> call= service.downloadFileWithDynamicUrlSync(urlx);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        System.out.println("music:"+response.isSuccessful());
                        if(response.isSuccessful()){
                            boolean writtenToDisk = fileUtil.writeResponseBodyToDisk(response.body(),name,sp);
                            if(writtenToDisk){
                                Message msg=Message.obtain();
                                msg.what=DOWN_FIINISH;
                                msg.obj=name;
                                handler.sendMessage(msg);
                            }else{
                                Message msg=Message.obtain();
                                msg.what=DOWN_FAIL;
                                handler.sendMessage(msg);
                            }
                        }
                        Message msg=Message.obtain();
                        msg.what=DOWN_FAIL;
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        System.out.println("连接失败");
                        Message msg=Message.obtain();
                        msg.what=DOWN_FAIL;
                        handler.sendMessage(msg);
                    }
                });
            }
        }.start();

    }

    private void startImgs()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i <paths.size(); i=(i%(paths.size()-1))+1)
                {
                    if(stop){
                        break;
                    }
                    wlImgVideoView.setCurrentImg(BitmapFactory.decodeFile(paths.get(i)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }



    //选择照片
    public void choose(View view) {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .capture(true)  // 使用相机，和 captureStrategy 一起使用
                .captureStrategy(new CaptureStrategy(true, "com.xurent.livedance.fileProvider"))
//        R.style.Matisse_Zhihu (light mode)
//        R.style.Matisse_Dracula (dark mode)
                .theme(R.style.Matisse_Dracula)
                .countable(true)
                .maxSelectable(10)
                .addFilter(new Filter() {
                    @Override
                    protected Set<MimeType> constraintTypes() {
                        return new HashSet<MimeType>() {{
                            add(MimeType.PNG);
                        }};
                    }

                    @Override
                    public IncapableCause filter(Context context, Item item) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(item.getContentUri());
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(inputStream, null, options);
                            int width = options.outWidth;
                            int height = options.outHeight;

//                            if (width >= 500)
//                                return new IncapableCause("宽度超过500px");

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }


                        return null;
                    }
                })
//                .gridExpectedSize((int) getResources().getDimension(R.dimen.imageSelectDimen))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.87f)
                .imageEngine(new GlideLoadEngine())
                .forResult(REQUEST_CODE_SELECT_IMG );

    }

    //每隔1秒更新一下进度条线程
    class UpdateLiveTime implements Runnable {
        @Override
        public void run() {
            handler.sendEmptyMessage(UPDATE_TIME);
            if (isStart&&Time>0) {
                handler.postDelayed(updateLiveTime, 1000);
            }
        }
    }

    public void uploadVideo(View view) {
        submit(0);
    }

    private void InitNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.music_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private void submit(Integer type){
        pd.setMessage("正在上传...");
        pd.setCancelable(false);
        pd.show();
        File file=new File(mTempPath);
        MultipartBody.Part body =null;
        if(file.exists()){
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            body = MultipartBody.Part.createFormData("file",file.getName(),requestFile);
        }else{
            return;
        }
        RequestBody types = RequestBody.create(MediaType.parse("text/plain"), type.toString());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);

        Call<JsonObject> call=service.uploadFile(body,types);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                if(data.get("code").getAsInt()==0){
                    Toast.makeText(ImageActivity.this,"上传成功",0).show();
                    setResult(1);
                    finish();
                }else{
                    Toast.makeText(ImageActivity.this,"上传失败",0).show();
                }
                pd.cancel();
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("失败"+call.request());
                Toast.makeText(ImageActivity.this,"网络异常",0).show();
                pd.cancel();
            }
        });


    }

    private boolean choose=false;
    public void chooseMusic(View view) {
        choose=!choose;
        if(choose){
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.GONE);
        }
    }

    private Handler handler=new Handler(){
        @SuppressLint("WrongConstant")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DOWN_FIINISH:
                    String name= (String) msg.obj;
                    File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LiveDance/music" + File.separator,name+".mp3");
                    url= fileDir.getAbsolutePath();
                    System.out.println("完成:"+url);
                    pd.cancel();
                    Toast.makeText(ImageActivity.this,"下载成功",0).show();
                    break;
                case DOWN_FAIL:
                    pd.cancel();
                    Toast.makeText(ImageActivity.this,"下载失败",0).show();
                    break;
                case UPDATE_TIME:
                    tv_time.setText(""+Time--);
                    break;
            }
        }
    };


    @SuppressLint("WrongConstant")
    private void showContent(Intent data) {
        if(data==null){
            Toast.makeText(this,"请选择图片才可以开始录制",0).show();
            return;
        }
         paths = Matisse.obtainPathResult(data);
        if (paths.isEmpty()) {
            Toast.makeText(this,"请选择图片才可以开始录制",0).show();
            return;
        }
        wlImgVideoView.setCurrentImg(BitmapFactory.decodeFile(paths.get(0)));



    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_IMG) {
            showContent(data);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
