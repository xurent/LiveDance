package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.model.LiveRoomBean;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LiveAdapter extends RecyclerView.Adapter<LiveAdapter.ViewHolder> {

    private Context mContext;
    private LinkedList<LiveRoomBean> mdata ;
    private OnItemClickListener mOnItemClickListener;

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public LiveAdapter(Context mContext, LinkedList<LiveRoomBean> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }

    public void updateMdata(ArrayList<LiveRoomBean> mdata) {
       mdata.addAll(mdata);
    }

    @NonNull
    @Override
    public LiveAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.list_live, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LiveAdapter.ViewHolder holder, int position) {
        LiveRoomBean data=mdata.get(position);
        Glide.with(mContext)
                .load(data.getRoomImg())
                .thumbnail(Glide.with(mContext)
                        .load(data.getRoomImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(holder.imageView);
        holder.tv_name.setText(data.getNickname());
        holder.online_num.setText(data.getOnline()+"");
        holder.live_title.setText(data.getTitle());
        holder.fenlei.setText(data.getKind());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemClick(view,position);

                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemLongClick(view,position);
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
        private  TextView online_num;
        private  TextView live_title;
        private  TextView fenlei;
        private LinearLayout liner_live;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.live_img);
            tv_name=itemView.findViewById(R.id.live_name);
            online_num=itemView.findViewById(R.id.live_number);
            live_title=itemView.findViewById(R.id.live_title);
            fenlei=itemView.findViewById(R.id.fenlei);
            liner_live=itemView.findViewById(R.id.liner_live);
        }

    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}
