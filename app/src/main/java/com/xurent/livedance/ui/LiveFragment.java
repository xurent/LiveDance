package com.xurent.livedance.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.CreateLiveRoom;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.MainActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.activity.MoreLiveActivity;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.DataBean;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.ui.adapter.ImageAdapter;
import com.xurent.livedance.ui.adapter.LiveAdapter;
import com.xurent.livedance.utils.OKhttpUtils;
import com.youth.banner.Banner;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.util.BannerUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveFragment extends Fragment {

    private Banner banner;
    private ImageView animation;
    private AnimationDrawable anim;
    private RecyclerView recyclerView;
    private LiveAdapter liveAdapter;
    private BottomNavigationView bottomNavigationView ;
    private TextView mokuai1;
    private TextView mokuai2;
    private FloatingActionButton fb_live;
    private Retrofit retrofit=null;
    LinkedList<LiveRoomBean> Datas=new LinkedList<>();
    private Button bt_all;
    private MaterialCardView focus;
    private ArrayList<LiveRoomBean> rooms=null;
    private  TextView living_num;
    private  ImageView focusImg;
    private CircleImageView foucsHead;
    private  TextView username;
    private  TextView fenlei;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.frag_live_view,null);
        banner = (Banner) view.findViewById(R.id.banner);
        animation=view.findViewById(R.id.live_animation);
        recyclerView=view.findViewById(R.id.recycle);
        mokuai1=view.findViewById(R.id.tx_more1);
        mokuai2=view.findViewById(R.id.tx_more2);
        fb_live=view.findViewById(R.id.fab);
        bt_all=view.findViewById(R.id.bt_all_live);
        focus=view.findViewById(R.id.focus);
        living_num=view.findViewById(R.id.tx_living_num);
        focusImg=view.findViewById(R.id.focus_img);
        foucsHead=view.findViewById(R.id.foucus_head);
        username=view.findViewById(R.id.username);
        fenlei=view.findViewById(R.id.fenlei);
        ImageAdapter adapter=new ImageAdapter(DataBean.getTestData1());
        banner.setAdapter(adapter);
        banner.setIndicator(new CircleIndicator(getContext()));
        banner.setIndicatorGravity(IndicatorConfig.Direction.RIGHT);
        banner.setIndicatorSelectedColor(Color.parseColor("#ffffff"));
        banner.setIndicatorNormalColor(Color.parseColor("#A9A9A9"));
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
        animation.setBackgroundResource(R.drawable.live_playing);
        anim= (AnimationDrawable) animation.getBackground();


        liveAdapter=new LiveAdapter(view.getContext(),Datas);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(),2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(liveAdapter);
        focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rooms!=null){
                    Intent intent=new Intent(getContext(), LiveRoomActivity.class);
                    intent.putExtra("anchor",rooms.get(0));
                    System.out.println("----"+Datas.get(0));
                    startActivity(intent);
                }
            }
        });
        init();

        return view;
    }

    private  void init(){
        initListner();//初始化监听
        initNet();//初始化网络
        getData();//初始化数据
        InitFocus();
    }
    private  void getData(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getAllRoom(1,30);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject data=response.body();
                    int code=data.get("code").getAsInt();
                    if(code==0){
                        if( !data.get("data").isJsonNull()){
                            JsonObject obj=data.getAsJsonObject("data");
                            JsonArray array=obj.getAsJsonArray("content");
                            Gson gson=new Gson();
                            Type type = new TypeToken<List<LiveRoomBean>>(){}.getType();
                            ArrayList<LiveRoomBean> datas=gson.fromJson(array.toString(), type);
                            Datas.addAll(datas);
                            liveAdapter.notifyDataSetChanged();
                            if(Datas.size()>4){
                                bt_all.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                        Toast.makeText(getContext(),data.get("msg").getAsString(),0).show();
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


    private void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .build();
    }
    @Override
    public void onStart() {
        super.onStart();
        banner.start();
        anim.start();
    }


    @Override
    public void onStop() {
        super.onStop();
        banner.stop();
        anim.stop();
    }


    private void InitFocus(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getFocusAnchor();
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data.toString());
                if(data.get("code").getAsInt()==0){
                    if(!data.get("data").isJsonNull()){
                        Gson gson=new Gson();
                        Type type = new TypeToken<List<LiveRoomBean>>(){}.getType();
                        rooms=gson.fromJson(data.get("data").getAsJsonArray().toString(),type);
                        if(rooms.size()>0){
                            focus.setVisibility(View.VISIBLE);
                            Tongji(rooms);
                        }else{
                            focus.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if(getContext()!=null){
                    Toast.makeText(getContext(),"网络异常",0).show();
                }
            }
        });
    }


    private void Tongji(ArrayList<LiveRoomBean> room){
        int count=0;
        for(LiveRoomBean r:room){
            if(r.getState()==1){
                count++;
            }
        }
        living_num.setText(""+count);
        LiveRoomBean x=room.get(0);
        fenlei.setText(x.getKind());
        username.setText(x.getTitle());
        if(getContext()!=null){
            Glide.with(getContext())
                    .load(x.getRoomImg())
                    .thumbnail(Glide.with(getContext())
                            .load(x.getRoomImg()))
                    .placeholder(R.drawable.head)
                    .into(focusImg);
            Glide.with(getContext())
                    .load(x.getAnchorImg())
                    .thumbnail(Glide.with(getContext())
                            .load(x.getAnchorImg()))
                    .centerCrop()
                    .placeholder(R.drawable.head)
                    .into(foucsHead);
        }


    }

    private void  initListner(){

        liveAdapter.setmOnItemClickListener(new LiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                System.out.println("点击");
                Intent intent=new Intent(getContext(), LiveRoomActivity.class);
                intent.putExtra("anchor",Datas.get(position));
                System.out.println("----"+Datas.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                System.out.println("长按");
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("举报对象: "+Datas.get(position).getUsername()+Datas.get(position).getNickname())
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
        });


        mokuai1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomNavigationView= getActivity().findViewById(R.id.navigation);
                bottomNavigationView.getMenu().getItem(2).setChecked(true);
                MainActivity main= (MainActivity) getActivity();
                main.userItem(main.FOCUS);
            }
        });

        mokuai2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                //换一换
                if(Datas.size()>4){
                    changeData();
                }else {
                    Toast.makeText(getContext(),"没有更多了",0).show();
                }

            }
        });
        //我要直播
        fb_live.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if(!Constants.LOGIN){
                    Toast.makeText(getContext(),"请先登录",0).show();
                    return;
                }
                Intent intent=new Intent(getActivity().getApplicationContext(), CreateLiveRoom.class);
                startActivity(intent);
            }
        });

        //更多
        bt_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity().getApplicationContext(), MoreLiveActivity.class);
                startActivity(intent);
            }
        });

    }

    private void changeData(){
        int index=4;
        while ((index--)>0){
            int rand=(int) Math.random()*19;
            if(rand<Datas.size()){
                LiveRoomBean liveRoomBean= Datas.get(rand);
                Datas.remove(liveRoomBean);
                Datas.set(index,liveRoomBean);
            }
         }
        liveAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        retrofit=null;
        super.onDestroy();
    }
}
