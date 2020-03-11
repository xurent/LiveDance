package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xurent.livedance.R;
import com.xurent.livedance.model.FileBean;
import com.xurent.livedance.model.VideoBean;
import com.xurent.livedance.model.LiveRoomBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppBar2Adapter extends RecyclerView.Adapter<AppBar2Adapter.ViewHolder> {

    private Context mContext;
    private LinkedList<FileBean> mdata ;
    private OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public AppBar2Adapter(Context mContext, LinkedList<FileBean> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }

    public void updateMdata(ArrayList<LiveRoomBean> mdata) {
       mdata.addAll(mdata);
    }

    @NonNull
    @Override
    public AppBar2Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.list_video1, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppBar2Adapter.ViewHolder holder, int position) {
        FileBean data=mdata.get(position);
        Glide.with(mContext)
                .load(data.getUrl())
                .thumbnail(Glide.with(mContext)
                        .load(data.getUrl()))
                .error(R.drawable.banner3)
                .placeholder(R.drawable.default_tv)
                .into(holder.imageView);
        holder.tv_name.setText(data.getNickName());
        holder.love_num.setText(data.getLikeNumber()+"");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        holder.time.setText(sdf.format(data.getCreateTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClick!=null){
                    onItemClick.OnItemClick(view,position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(onItemClick!=null){
                    onItemClick.OnItemLongClick(view,position);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView  tv_name;
        private  TextView love_num;
        private  TextView time;




        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.video_img);
            tv_name=itemView.findViewById(R.id.acter_name);
            love_num=itemView.findViewById(R.id.video_number);
            time=itemView.findViewById(R.id.video_time);
        }

    }

    public interface  OnItemClick{
        void OnItemClick(View v,int position);
        void OnItemLongClick(View v,int position);
    }

}
