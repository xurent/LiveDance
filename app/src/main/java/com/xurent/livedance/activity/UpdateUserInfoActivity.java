package com.xurent.livedance.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.UserService;
import com.xurent.livedance.utils.BitmapUtil;
import com.xurent.livedance.utils.FileUtil;
import com.xurent.livedance.utils.OKhttpUtils;
import java.io.File;
import java.util.Date;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateUserInfoActivity extends AppCompatActivity {

    public static final int RC_TAKE_PHOTO = 1;
    public static final int RC_CHOOSE_PHOTO = 2;
    private String mTempPhotoPath;
    private TextInputEditText  name;
    private TextInputEditText  jiesao;
    private ProgressDialog pd;
    private File file;
    String cookie;
    private  String img="https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3017334715,3640667871&fm=26&gp=0.jpg";
    private ShapeableImageView headImg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_userinfo);
        name=findViewById(R.id.name);
        jiesao=findViewById(R.id.jiesao);
        headImg=findViewById(R.id.image_view);
        Intent intent=getIntent();
        img=intent.getStringExtra("headImg");
        Glide.with(this)
                .load(img)
                .thumbnail(Glide.with(this)
                        .load(img))
                .centerCrop()
                .placeholder(R.drawable.ic_add_a_photo_black_24dp)
                .into(headImg);
        String nickname=intent.getStringExtra("nickname");
        String introduce=intent.getStringExtra("jianjie");
        if(nickname!=null){
            name.setText(nickname);
        }
        if(introduce!=null){
            jiesao.setText(introduce);
        }
        pd = new ProgressDialog(this);
        cookie=intent.getStringExtra("cookie");

    }

    public void jump(View view) {
        setResult(1);
        finish();
    }


    //上传头像
    public void uploadImg(View view) {
        new MaterialAlertDialogBuilder(this)
                        .setTitle("选择上传方式")
                        .setNeutralButton("关闭", null)
                        .setItems(new String[]{"相机拍照", "本地相册"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0){
                                    takePhoto();
                                }else{
                                    Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                                    intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
                                }
                            }
                        }).create().show();
    }

    //上传
    public void submit(View view) {
        String nickname=name.getText().toString().trim();
        String jie=jiesao.getText().toString().trim();
        if(TextUtils.isEmpty(nickname)){
            name.setError("请填写昵称");
            return;
        }
        pd.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .build();
        UserService service=retrofit.create(UserService.class);
        Call<JsonObject> call=service.updateUser(nickname,jie,img);

        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                pd.cancel();
                if(data.get("code").getAsInt()==0){
                    setResult(1);
                    finish();
                }

            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("update 连接失败");
                pd.cancel();
            }
        });

    }

    public void loadImg(String path){

        file =new File(path);
        if(!file.exists())return;
        pd.setMessage("正在上传请稍后...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        makeFileSize(path);

    }
    /**
     * 压缩
     */
    private void makeFileSize(String path){
        /**
         * 压缩
         */
        new Thread(){
            @Override
            public void run() {
                super.run();
                 Bitmap bitmap= BitmapFactory.decodeFile(path);
                 bitmap= BitmapUtil.ClipSquareBitmap(bitmap,720,600);
                 BitmapUtil.savePicToSdcard(path,bitmap);
                    Message msg=Message.obtain();
                    msg.what=1;
                    handler.sendMessage(msg);
            }
        }.start();

    }
    private void takePhoto() {
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fileDir = new File(Environment.getExternalStorageDirectory() + File.separator + "LiveDance" + File.separator);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File photoFile = new File(fileDir, new Date().getTime()+".png");
        mTempPhotoPath = photoFile.getAbsolutePath();
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority=this.getPackageName() + ".fileProvider";
            //大于等于版本24（7.0）的场合
            imageUri = FileProvider.getUriForFile(this, authority, photoFile);
        } else {
            //小于android 版本7.0（24）的场合
            imageUri = Uri.fromFile(fileDir);
        }

        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intentToTakePhoto, RC_TAKE_PHOTO);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_TAKE_PHOTO:
                System.out.println("选择拍照");
                //将图片显示在ivImage上
                //Glide.with(this).load(mTempPhotoPath).apply(requestOptions).into(headImg);
                loadImg(mTempPhotoPath);
                break;
            case RC_CHOOSE_PHOTO:
                System.out.println("选择图片");
                if(data!=null) {
                    Uri uri = data.getData();
                    String filePath = FileUtil.getFilePathByUri(this, uri);
                    if (!TextUtils.isEmpty(filePath)) {
                        mTempPhotoPath = filePath;
                        loadImg(filePath);

                    }
                }
                break;
        }

    }


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file",file.getName(),requestFile);
                    System.out.println(Constants.file_url);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.file_url) //设置网络请求的Url地址
                            .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                            .client(OKhttpUtils.getOkHttpClient(UpdateUserInfoActivity.this))
                            .build();
                    UserService service=retrofit.create(UserService.class);
                    Call<JsonObject> call=service.uploadHeadImg(body);
                    System.out.println(call.request());
                    call.enqueue(new retrofit2.Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject data=response.body();
                            System.out.println("data:"+data+","+response.headers().toString());
                            if(data!=null&&data.get("code").getAsInt()==0){
                                System.out.println("上传成功");
                                img=data.get("data").getAsString();
                                pd.cancel();
                                Toast.makeText(UpdateUserInfoActivity.this,"上传成功",0).show();
                                RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
                                Glide.with(UpdateUserInfoActivity.this).load(mTempPhotoPath).apply(requestOptions).into(headImg);
                            }
                        }
                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(UpdateUserInfoActivity.this,"服务器繁忙,上传失败",0).show();
                            System.out.println("连接失败");
                            pd.cancel();
                        }

                    });
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        cookie=null;
        file=null;
        super.onDestroy();
    }
}
