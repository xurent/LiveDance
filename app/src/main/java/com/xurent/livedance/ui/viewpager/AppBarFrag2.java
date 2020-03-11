package com.xurent.livedance.ui.viewpager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.R;
import com.xurent.livedance.activity.PlayerV;
import com.xurent.livedance.activity.PlayerX;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.FileService;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.ui.adapter.AppBar2Adapter;
import com.xurent.livedance.utils.OKhttpUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppBarFrag2 extends Fragment {

    private RecyclerView recyclerView;
    private AppBar2Adapter adapter;
    private LinkedList<FileBean> files=new LinkedList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.frag_appbar2,null);
        recyclerView=view.findViewById(R.id.appbar1_recycle);
        adapter=new AppBar2Adapter(view.getContext(),files);
        GridLayoutManager layoutManager = new GridLayoutManager(view.getContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getData();
        adapter.setOnItemClick(new AppBar2Adapter.OnItemClick() {
            @Override
            public void OnItemClick(View v, int position) {
                if(files.get(position).getType()==2){
                Intent intent=new Intent(getActivity().getApplicationContext(), PlayerX.class);
                intent.putExtra("fileBean",files.get(position));
                startActivity(intent);}
                else {
                    Intent intent=new Intent(getActivity().getApplicationContext(), PlayerV.class);
                    intent.putExtra("fileBean",files.get(position));
                    startActivity(intent);
                }
            }

            @Override
            public void OnItemLongClick(View v, int position) {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("举报对象: "+files.get(position).getUserId()+files.get(position).getNickName())
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
        return view;
    }


    public void getData(){
        Retrofit retrofit = new Retrofit.Builder()
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
                    ArrayList<FileBean> fileBeans=gson.fromJson(object.get("data").getAsJsonArray().toString(),type);
                    files.addAll(fileBeans);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }


}
