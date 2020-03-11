package com.xurent.livedance.LiveRoom;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.push.LivePushActivity;
import com.xurent.livedance.utils.BitmapUtil;
import com.xurent.livedance.utils.FileUtil;
import com.xurent.livedance.utils.OKhttpUtils;

import java.io.File;
import java.util.Date;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.FileProvider;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateLiveRoom extends AppCompatActivity {

    public static final int RC_TAKE_PHOTO = 1;
    public static final int RC_CHOOSE_PHOTO = 2;
    private String mTempPhotoPath;
    private File file;
    private ProgressDialog pd;
    private ShapeableImageView roomImg;
    private TextInputEditText acount;
    private TextInputEditText room_title;
    private TextView roomId;
    private int index=0;
    String[] COUNTRIES = new String[] {"音乐专区", "舞蹈欣赏", "风景欣赏", "游戏娱乐","其它"};

    private AutoCompleteTextView editTextFilledExposedDropdown;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_room);
        roomImg=findViewById(R.id.image_view);
        pd = new ProgressDialog(this);
        acount=findViewById(R.id.acount);
        room_title=findViewById(R.id.room_title);
        roomId=findViewById(R.id.roomId);
        roomId.setText("房间号 "+Constants.uid);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.cat_popup_item,
                        COUNTRIES);
        System.out.println(COUNTRIES.length+","+adapter.getCount());
        editTextFilledExposedDropdown = findViewById(R.id.filled_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(adapter);

        editTextFilledExposedDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                index=i;
                System.out.println("被选中:"+i);
            }
        });
        getRoomInfo();

    }



    public void getRoomInfo(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(CreateLiveRoom.this))
                .build();
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getMyRoom(Constants.uid);
        System.out.println(call.request());
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println("data:"+data+","+response.headers().toString());
                if(data!=null&&data.get("code").getAsInt()==0){
                    if(data.get("data").isJsonNull())return;
                    JsonObject room= data.getAsJsonObject("data");
                    if(room.get("roomImg")!=null){
                        Glide.with(CreateLiveRoom.this)
                                .load(room.get("roomImg").getAsString())
                                .thumbnail(Glide.with(CreateLiveRoom.this)
                                        .load(room.get("roomImg").getAsString()))
                                .centerCrop()
                                .placeholder(R.drawable.ic_add_a_photo_black_24dp)
                                .into(roomImg);
                    }
                    room_title.setText(room.get("title").getAsString());
                    //editTextFilledExposedDropdown.setText(room.get("kind").getAsString());
                    index=getIndex(room.get("kind").getAsString());
                    acount.setText(room.get("announcement").getAsString());
                }else{
                    Toast.makeText(CreateLiveRoom.this,data.get("msg").getAsString(),0).show();
                }

                pd.cancel();
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CreateLiveRoom.this,"服务器繁忙,上传失败",0).show();
                pd.cancel();
            }

        });



    }

    public void openLive(View view) {
        String fenlei=editTextFilledExposedDropdown.getText().toString().trim();
        String title= room_title.getText().toString().trim();
        String gonggao=acount.getText().toString().trim();
        if(TextUtils.isEmpty(fenlei)){
            editTextFilledExposedDropdown.setError("请选择分类");
            return;
        }
        if(TextUtils.isEmpty(title)){
            room_title.setError("请选择分类");
            return;
        }
        pd.setMessage("正在提交请稍后");
        pd.show();
        MultipartBody.Part body =null;
        if(file!=null){
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), file);
             body = MultipartBody.Part.createFormData("file",file.getName(),requestFile);
        }
        RequestBody titlebody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody kind = RequestBody.create(MediaType.parse("text/plain"), COUNTRIES[index]);
        RequestBody acount = RequestBody.create(MediaType.parse("text/plain"), gonggao);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(CreateLiveRoom.this))
                .build();
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.uploadHeadImg(titlebody,kind,acount,body);
        System.out.println(call.request());
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println("data:"+data+","+response.headers().toString());
                if(data!=null&&data.get("code").getAsInt()==0){
                    System.out.println("上传成功");
                    Intent intent=new Intent(CreateLiveRoom.this, LivePushActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(CreateLiveRoom.this,"提交失败",0).show();
                }

                pd.cancel();
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(CreateLiveRoom.this,"服务器繁忙,上传失败",0).show();
                pd.cancel();
            }

        });
    }

    //上传
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


    public void loadImg(String path){
        file =new File(path);
        if(!file.exists())return;
        pd.setMessage("正在处理请稍后...");
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
                /*Bitmap bitmap= BitmapFactory.decodeFile(path);
                bitmap= BitmapUtil.ClipSquareBitmap(bitmap,720,600);
                BitmapUtil.savePicToSdcard(path,bitmap);*/
                file=BitmapUtil.compressThumble(path);
                Message msg=Message.obtain();
                msg.what=1;
                handler.sendMessage(msg);
            }
        }.start();

    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    pd.cancel();
                    RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
                    Glide.with(CreateLiveRoom.this).load(mTempPhotoPath).apply(requestOptions).into(roomImg);
                    break;
            }
        }
    };



    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_TAKE_PHOTO:
                System.out.println("选择拍照");
                loadImg(mTempPhotoPath);
                break;
            case RC_CHOOSE_PHOTO:
                System.out.println("选择图片");
                Uri uri = data.getData();
                String filePath = FileUtil.getFilePathByUri(this, uri);
                if (!TextUtils.isEmpty(filePath)) {
                    mTempPhotoPath=filePath;
                    loadImg(filePath);
                }
                break;
        }

    }


    private int getIndex(String src){

        for(int i=0;i<COUNTRIES.length;i++){
            if(COUNTRIES[i].equals(src)){
                return i;
            }
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        file=null;
        COUNTRIES=null;
        super.onDestroy();
    }
}
