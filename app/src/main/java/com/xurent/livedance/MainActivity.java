package com.xurent.livedance;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.ui.FocusFragment;
import com.xurent.livedance.ui.IndexFragment;
import com.xurent.livedance.ui.LiveFragment;
import com.xurent.livedance.ui.UserInfoFragment;
import com.xurent.livedance.utils.cookie.PersistenceCookieJar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    //录像需要的权限
    private static final String[] VIDEO_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE};
    private static final int VIDEO_PERMISSIONS_CODE = 1;
    public  final int INDEX=0;
    public final int LIVE=1;
    public  final int FOCUS=3;
    public final int USER_CENTER=4;

    private BottomNavigationView bottomNavigationView;


    private Retrofit retrofit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView=findViewById(R.id.navigation);
        initNet();
        userItem(INDEX);
        navigation();
        requestPermission();
    }




    public void initNet(){
        OkHttpClient client=new OkHttpClient();
        client.newBuilder().cookieJar(new PersistenceCookieJar());
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(client)
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }


    public   void userItem( int type){

        FragmentManager fm=getSupportFragmentManager();
        Fragment fragment=new IndexFragment();
        switch (type){
            case INDEX:fragment=new IndexFragment();
                break;
            case LIVE:fragment=new LiveFragment();
                break;
            case FOCUS:fragment=new FocusFragment();
                break;
            case USER_CENTER:fragment=new UserInfoFragment();
                break;
        }
        //事务
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.context,fragment);
        ft.commit();

    }


    private void navigation(){

        MenuItem home= bottomNavigationView.getMenu().getItem(0);
        MenuItem live= bottomNavigationView.getMenu().getItem(1);
        MenuItem focus= bottomNavigationView.getMenu().getItem(2);
        MenuItem me= bottomNavigationView.getMenu().getItem(3);

        home.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                userItem(INDEX);
                return false;
            }
        });

        focus.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                userItem(FOCUS);
                return false;
            }
        });

        live.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                /*Intent intent=new Intent(MainActivity.this,LiveRoomActivity.class);
                startActivity(intent);*/
                userItem(LIVE);
                return false;
            }
        });

        me.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                userItem(USER_CENTER);
                return false;
            }
        });

    }


    //申请权限
    private void requestPermission() {
        // 当API大于 23 时，才动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this,VIDEO_PERMISSIONS,VIDEO_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case VIDEO_PERMISSIONS_CODE:
                //权限请求失败
                if (grantResults.length == VIDEO_PERMISSIONS.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //弹出对话框引导用户去设置
                            showDialog();
                            Toast.makeText(MainActivity.this, "请求权限被拒绝", Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //弹出提示框
    private void showDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("直播需要相机、录音和读写权限，是否去设置？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToAppSetting();
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setCancelable(false)
                .show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}
