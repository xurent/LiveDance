package com.xurent.livedance.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.R;
import com.xurent.livedance.activity.PlayerV;
import com.xurent.livedance.activity.PlayerX;
import com.xurent.livedance.activity.VideoActivity;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.AuthorizationService;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.http.UserService;
import com.xurent.livedance.activity.LoginActivity;
import com.xurent.livedance.activity.UpdateUserInfoActivity;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.utils.OKhttpUtils;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfoFragment extends Fragment {

    private ImageView headImg;
    private ListView iv;
    private boolean login=false;
    private Button bt_login;
    private TextView  tv_name;
    private TextView tv_userId;
    private  TextView tv_introduction;
    private  ImageView exit;
    private Retrofit retrofit;
    private MyAdapter adapter;
    private TabLayout tabLayout;


    @LayoutRes
    private int popupItemLayout=R.layout.cat_popup_item;
    private String headUrl=null;
    private List<FileBean> lists=new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.frag_userinfo,null);
        iv=view.findViewById(R.id.iv);
        bt_login=view.findViewById(R.id.login);
        headImg=view.findViewById(R.id.headImg);
        tv_name=view.findViewById(R.id.nickname);
        tv_userId=view.findViewById(R.id.userId);
        tv_introduction=view.findViewById(R.id.introduction);
        exit=view.findViewById(R.id.exit);
        tabLayout=view.findViewById(R.id.tabLayoutHome);
        login=Constants.LOGIN;
        if(login){
            bt_login.setText("编辑资料");
        }
        adapter=new MyAdapter();
        Button listPopupWindowButton = view.findViewById(R.id.uploadfile);
        ListPopupWindow listPopupWindow = initializeListPopupMenu(listPopupWindowButton);
        listPopupWindowButton.setOnClickListener(v -> listPopupWindow.show());
        iv.setAdapter(adapter);
        initNet();
        Login();
        Listner();
        return view;
    }

    public void Login() {

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(login){//已经登录则编辑资料
                    Intent intent=new Intent(getActivity().getApplicationContext(), UpdateUserInfoActivity.class);
                    intent.putExtra("cookie",Constants.token);
                    intent.putExtra("nickname",tv_name.getText());
                    intent.putExtra("jianjie",tv_introduction.getText());
                    intent.putExtra("headImg",headUrl);
                    startActivity(intent);
                }else {
                    Intent intent=new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent,0);

                }

            }
        });

    }

    public void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.token_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                 .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

    }

    public void getUserinfo(){
        UserService service=retrofit.create(UserService.class);
        Call<JsonObject> call=service.getMyInfo();
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    data =data.getAsJsonObject("data");
                    if(!data.get("headImg").isJsonNull()){
                        headUrl=data.get("headImg").getAsString();
                    }
                    RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
                    if(getContext()!=null){
                        Glide.with(getContext())
                                .load(headUrl)
                                .apply(requestOptions)
                                .thumbnail(Glide.with(getContext())
                                        .load(headUrl))
                                .centerCrop()
                                .placeholder(R.drawable.default_tv)
                                .into(headImg);
                    }
                    if(!data.get("nickName").isJsonNull()){
                        tv_name.setText(data.get("nickName").getAsString());
                    }
                    tv_userId.setText(  "ID:"+data.get("username").getAsString());
                    if(!data.get("introduction").isJsonNull()&&!TextUtils.isEmpty(data.get("introduction").getAsString())){
                        tv_introduction.setText(data.get("introduction").getAsString());
                    }else{
                        tv_introduction.setText("暂无简介...");
                    }
                    if(!data.get("username").isJsonNull()){
                        Constants.uid=data.get("username").getAsString();
                    }
                   if(!data.get("nickName").isJsonNull()){
                       Constants.nickName=data.get("nickName").getAsString();
                   }
                    Constants.headImage=headUrl;
                    Constants.LOGIN=true;
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if(Constants.LOGIN&&getContext()!=null){
                    Toast.makeText(getContext(),"网络异常",0).show();
                }
            }
        });
    }

    public void Listner(){
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logot();
            }
        });

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

    private void getMyWorks(){
       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.myWorks();
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
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.mylikes();
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



    private ListPopupWindow initializeListPopupMenu(View v) {
        ListPopupWindow listPopupWindow =
                new ListPopupWindow(getContext(), null, R.attr.listPopupWindowStyle);
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<>(
                        getContext(),
                        popupItemLayout,
                        getResources().getStringArray(R.array.cat_list_popup_window_content));
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setAnchorView(v);
        listPopupWindow.setOnItemClickListener(
                (parent, view, position, id) -> {
                    /*Snackbar.make(
                            getActivity().findViewById(android.R.id.content),
                            adapter.getItem(position).toString(),
                            Snackbar.LENGTH_LONG)
                            .show();
                    listPopupWindow.dismiss();*/
                    Intent intent=new Intent(getActivity().getApplicationContext(), VideoActivity.class);
                    switch (position){
                        case 0: intent=new Intent(getActivity().getApplicationContext(), VideoActivity.class);
                            break;
                        case 1:
                            break;
                    }
                    startActivity(intent);
                    listPopupWindow.dismiss();
                });
        return listPopupWindow;
    }


    public void logot(){
        new MaterialAlertDialogBuilder(getContext())
                .setMessage("您是否退出")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exit();
                    }
                })
                .setNegativeButton("手滑了", null).create().show();
    }

    public void exit(){
        AuthorizationService service=retrofit.create(AuthorizationService.class);
        Call<JsonObject> call=service.logout(/*Constants.token*/);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    Constants.LOGIN=false;
                    bt_login.setText("登录");
                    login=false;
                    headImg.setImageResource(R.drawable.default_tv);
                    tv_name.setText("暂未登录");
                    tv_userId.setText("ID:无");
                    tv_introduction.setText("暂无简介...");
                    Toast.makeText(getContext(),"退出成功",0).show();
                }else{
                    Toast.makeText(getContext(),"请勿重复退出",0).show();
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if(getContext()!=null){
                    Toast.makeText(getContext(),"服务器繁忙,稍后再试",0).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserinfo();
        getMyWorks();
        if(login){
            bt_login.setText("编辑资料");
            exit.setVisibility(View.VISIBLE);
        }else {
            bt_login.setText("登录");
            exit.setVisibility(View.GONE);
        }

    }

    private class MyAdapter extends BaseAdapter{

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
            View tv =View.inflate(viewGroup.getContext(), R.layout.ctrl_list, null);
            ImageView  img=tv.findViewById(R.id.work1);
            TextView number=tv.findViewById(R.id.love_number);
            number.setText(lists.get(i).getLikeNumber()+"");
            String URL= lists.get(i).getUrl();
            //final Bitmap bitmap= BitmapUtil.createVideoThumbnail(DataUtils.getDatas().get(i).url,200,200);
            Glide.with(viewGroup.getContext())
                    .load(URL)
                    .thumbnail(Glide.with(viewGroup.getContext())
                            .load(URL))
                    .error(R.drawable.banner3)
                    .placeholder(R.drawable.default_tv)
                    .into(img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(lists.get(i).getType()==2){
                        Intent intent=new Intent(getContext(), PlayerX.class);
                        intent.putExtra("fileBean",lists.get(i));
                        startActivity(intent);
                    }else{
                        Intent intent=new Intent(getContext(), PlayerV.class);
                        intent.putExtra("fileBean",lists.get(i));
                        startActivity(intent);
                    }

                }
            });
            img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("举报对象: "+lists.get(i).getUserId()+lists.get(i).getNickName())
                            .setView(R.layout.edit_text)
                            .setPositiveButton(
                                    "提交",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TextView input = ((AlertDialog) dialog).findViewById(android.R.id.text1);
                                        }
                                    })
                            .setNegativeButton("取消", null).create().show();
                    return false;
                }
            });

            return tv;
        }



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==0){
            login=true;
            bt_login.setText("编辑资料");
        }

    }

    @Override
    public void onDestroy() {
        retrofit=null;
        headUrl=null;
        super.onDestroy();

    }
}
