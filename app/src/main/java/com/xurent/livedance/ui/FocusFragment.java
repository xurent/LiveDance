package com.xurent.livedance.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.model.UserInfo;
import com.xurent.livedance.ui.adapter.FocusAdapter;
import com.xurent.livedance.utils.OKhttpUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FocusFragment extends Fragment {

    private RecyclerView recyclerView;

    private  FocusAdapter adapter;
    private Retrofit retrofit=null;
    private TextView tv_msg;
    LinkedList<LiveRoomBean> datas=new LinkedList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.frag_focus,null);
        Init(view);
        return view;
    }


    private void Init(View view){
        InitView( view);
        initNet();
        InitData();
    }

    private void InitView(View view ){
        recyclerView=view.findViewById(R.id.focus_recycle);
        tv_msg=view.findViewById(R.id.tv_msg);
       /* LinkedList<LiveRoomBean> data=new LinkedList<>();
        LiveRoomBean d1=new LiveRoomBean();
        d1.setTitle("噗归大佬为你讲解Kolin");
        d1.setNickname("陈甫佳");
        d1.setOnline(456);
        d1.setKind("王者荣耀");
        d1.setDate(new Date());
        d1.setRoomImg("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2088350635,1679784498&fm=26&gp=0.jpg");
        LiveRoomBean d2=new LiveRoomBean();
        d2.setTitle("甜面酱开播了");
        d2.setNickname("甜面酱");
        d2.setOnline(456);
        d2.setKind("舞蹈宅女");
        d2.setDate(new Date());
        d2.setRoomImg("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3017334715,3640667871&fm=26&gp=0.jpg");
        data.add(d1);
        data.add(d2);
        data.add(d1);
        data.add(d1);*/
        adapter=new FocusAdapter(view.getContext(),datas);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .build();
    }

    private void InitData(){
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
                        ArrayList<LiveRoomBean> rooms=gson.fromJson(data.get("data").getAsJsonArray().toString(),type);
                        datas.addAll(rooms);
                        adapter.notifyDataSetChanged();
                        if(datas.size()==0){
                            tv_msg.setVisibility(View.VISIBLE);
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

}
