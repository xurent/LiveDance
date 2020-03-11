package com.xurent.livedance.ui.viewpager;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.MainActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.activity.PlayerV;
import com.xurent.livedance.activity.PlayerX;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.model.DataBean;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.ui.adapter.ImageAdapter;
import com.xurent.livedance.activity.Video2DActivity;
import com.xurent.livedance.activity.VRplayActivity;
import com.xurent.livedance.utils.OKhttpUtils;
import com.youth.banner.Banner;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.util.BannerUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppBarFrag1 extends Fragment {

    private Banner banner;
    private ImageView video_2d;
    private ImageView video_3d;
    private TabLayout tabLayout;

    private TextView mokuai1;
    private TextView mokuai2;
    private TextView mokuai3;

    private ImageView video_img;
    private  ImageView video_img1;
    private  TextView number;
    private  TextView number1;
    private   ArrayList<FileBean> files=new ArrayList<>();
    String urlx="http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4";
    String urlx1="http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4";

    private BottomNavigationView bottomNavigationView ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.frag_appbar1,null);
        banner = (Banner) view.findViewById(R.id.banner);
        video_2d=view.findViewById(R.id.video2D);
        video_3d=view.findViewById(R.id.video3D);
        tabLayout=getParentFragment().getView().findViewById(R.id.tabLayoutHome);
        mokuai1=view.findViewById(R.id.index_mokuai1);
        mokuai2=view.findViewById(R.id.index_mokuai2);
        mokuai3=view.findViewById(R.id.index_mokuai3);
        video_img=view.findViewById(R.id.video_img);
        video_img1=view.findViewById(R.id.video_img1);
        number=view.findViewById(R.id.video_number);
        number1=view.findViewById(R.id.video_number1);
        ImageAdapter adapter=new ImageAdapter(DataBean.getTestData());
        banner.setAdapter(adapter);
        banner.setIndicator(new CircleIndicator(getContext()));
        banner.setIndicatorGravity(IndicatorConfig.Direction.RIGHT);
        banner.setIndicatorSelectedColor(Color.parseColor("#C71585"));
        banner.setIndicatorNormalColor(Color.parseColor("#ffffff"));
        banner.setIndicatorSpace(BannerUtils.dp2px(5));
        banner.setIndicatorMargins(new IndicatorConfig.Margins((int) BannerUtils.dp2px(10)));
        banner.setIndicatorWidth(20,20);
        banner.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 30);
            }
        });
        banner.setClipToOutline(true);
        InitListner();
        play2Dvideo();
        vrPlay();
        Listner();
        getData();
        return view;
    }



    private void play2Dvideo(){
        video_2d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity().getApplicationContext(), Video2DActivity.class);
                startActivity(intent);
            }
        });
    }

    private void vrPlay() {
        video_3d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity().getApplicationContext(), VRplayActivity.class);
                startActivity(intent);
            }
        });
    }


    private void getData(){
      Retrofit  retrofit = new Retrofit.Builder()
                .baseUrl(Constants.file_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        FileService service=retrofit.create(FileService.class);
        Call<JsonObject> call=service.getAll();
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject object=response.body();
                System.out.println(object);
                if(object.get("code").getAsInt()==0&&!object.get("data").isJsonNull()){
                    Gson gson=new Gson();
                    Type type = new TypeToken<List<FileBean>>(){}.getType();
                   files=gson.fromJson(object.get("data").getAsJsonArray().toString(),type);
                    if(files.size()>=2&&getContext()!=null){
                        String url=files.get(0).getUrl();
                        String url1=files.get(1).getUrl();
                        if(files.get(0).getType()==3)url=files.get(0).getThumbleImg();
                        if(files.get(1).getType()==3)url1=files.get(1).getThumbleImg();
                        Glide.with(getContext())
                                .load(url)
                                .thumbnail(Glide.with(getContext())
                                        .load(url))
                                .error(Glide.with(getContext())
                                        .load(urlx))
                                .placeholder(R.drawable.default_tv)
                                .into(video_img);
                        Glide.with(getContext())
                                .load(url1)
                                .thumbnail(Glide.with(getContext())
                                        .load(url1))
                                .error(Glide.with(getContext())
                                        .load(urlx1))
                                .placeholder(R.drawable.default_tv)
                                .into(video_img1);
                        number.setText(""+files.get(0).getLikeNumber());
                        number1.setText(""+files.get(1).getLikeNumber());

                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }


    private void Listner(){
        video_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(files.size()>=2){
                    if(files.get(0).getType()==2) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerX.class);
                        intent.putExtra("fileBean", files.get(0));
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerV.class);
                        intent.putExtra("fileBean", files.get(0));
                        startActivity(intent);
                    }
                }
            }
        });

        video_img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(files.size()>=2) {
                    if(files.get(1).getType()==2) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerX.class);
                        intent.putExtra("fileBean", files.get(1));
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerV.class);
                        intent.putExtra("fileBean", files.get(1));
                        startActivity(intent);
                    }
                }
            }
        });

        video_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(files.size()>=2) {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("举报对象: "+files.get(0).getUserId()+files.get(0).getNickName())
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
                }
                return false;
            }
        });

        video_img1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(files.size()>=2) {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("举报对象: "+files.get(1).getUserId()+files.get(1).getNickName())
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
                }
                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        banner.start();
    }


    @Override
    public void onStop() {
        super.onStop();
        banner.stop();
    }


   private void InitListner(){

        mokuai1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabLayout.getTabAt(1).select();
                System.out.println("切换");
            }
        });
       mokuai2.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               bottomNavigationView= getActivity().findViewById(R.id.navigation);
               bottomNavigationView.getMenu().getItem(1).setChecked(true);
               MainActivity main= (MainActivity) getActivity();
               main.userItem(main.LIVE);

           }
       });
       mokuai3.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

           }
       });
   }

}
