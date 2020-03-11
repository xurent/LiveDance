package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.R;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.model.VideoBean;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class FocusAdapter extends RecyclerView.Adapter<FocusAdapter.ViewHolder> {

    private Context mContext;
    private LinkedList<LiveRoomBean> mdata ;


    public FocusAdapter(Context mContext, LinkedList<LiveRoomBean> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }

    public void updateMdata(ArrayList<LiveRoomBean> mdata) {
       mdata.addAll(mdata);
    }

    @NonNull
    @Override
    public FocusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.list_focus, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FocusAdapter.ViewHolder holder, int position) {
        LiveRoomBean data=mdata.get(position);
        Glide.with(mContext)
                .load(data.getRoomImg())
                .thumbnail(Glide.with(mContext)
                        .load(data.getRoomImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(holder.imageView);

        Glide.with(mContext)
                .load(data.getAnchorImg())
                .thumbnail(Glide.with(mContext)
                        .load(data.getAnchorImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(holder.head);
        holder.tv_name.setText(data.getTitle());
        holder.fenlei.setText(data.getKind());
        holder.online_num.setText(data.getOnline()+"");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, LiveRoomActivity.class);
                intent.putExtra("anchor",mdata.get(position));
                mContext.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialAlertDialogBuilder(mContext)
                        .setTitle("举报对象: "+mdata.get(position).getUsername()+mdata.get(position).getNickname())
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

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView head;
        private ImageView imageView;
        private TextView  tv_name;
        private  TextView online_num;
        private  TextView fenlei;




        public ViewHolder(View itemView) {
            super(itemView);
            head=itemView.findViewById(R.id.foucus_head);
            tv_name=itemView.findViewById(R.id.username);
            online_num=itemView.findViewById(R.id.focus_num);
            imageView=itemView.findViewById(R.id.focus_img);
            fenlei=itemView.findViewById(R.id.fenlei);
        }

    }

}
