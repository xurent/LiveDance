package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.bumptech.glide.Glide;
import com.xurent.livedance.R;
import com.xurent.livedance.model.Fans;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FansAdapter extends RecyclerView.Adapter<FansAdapter.ViewHolder> {

    private Context mContext;
    private List<Fans> mdata ;



    public FansAdapter(Context mContext, List<Fans> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }


    @NonNull
    @Override
    public FansAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.item_rank, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FansAdapter.ViewHolder holder, int position) {
        Fans fans=mdata.get(position);
        holder.fans_name.setText(fans.getNickName());
        holder.money.setText(fans.getAcount()+"");
        Glide.with(mContext)
                .load(fans.getHeadImg())
                .thumbnail(Glide.with(mContext)
                        .load(fans.getHeadImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(holder.head);
        if(position==0){
            holder.rank.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView head;
        private TextView  fans_name;
        private TextView money;
        private ImageView rank;
        public ViewHolder(View view) {
            super(view);
            head=view.findViewById(R.id.fans_head);
            fans_name=view.findViewById(R.id.fans_name);
            money=view.findViewById(R.id.money);
            rank=view.findViewById(R.id.rank);
        }

    }



}
