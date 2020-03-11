package com.xurent.livedance.LiveRoom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xurent.livedance.LiveRoom.utils.ViewHolderUtil;
import com.xurent.livedance.R;
import com.xurent.livedance.model.LiveMsg;

import java.util.List;

/**
 * 评论列表适配器
 */
public class MessageAdapter extends BaseAdapter {

    private Context mContext;
    private List<LiveMsg> data;

    public MessageAdapter(Context context, List<LiveMsg> data) {

        this.mContext = context;
        this.data = data;
    }

    @Override
    public int getCount() {

        return data == null ? 0 : data.size();
    }

    @Override
    public LiveMsg getItem(int position) {

        return data.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public void NotifyAdapter(List<LiveMsg> data) {

        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            convertView = View.inflate(mContext, R.layout.item_messageadapter, null);
        }

        //头像
        ImageView iv=ViewHolderUtil.getView(convertView, R.id.img);
        Glide.with(convertView.getContext())
                .load(data.get(position).getUrl())
                .thumbnail(Glide.with(convertView.getContext())
                        .load(data.get(position).getUrl()))
                .centerCrop()
                .placeholder(R.drawable.ml_w_lv_8)
                .into(iv);
        //用户
        TextView tv_fans = ViewHolderUtil.getView(convertView, R.id.tv_fans);
        tv_fans.setText(data.get(position).getFansName());
        // 评论
        TextView tv_msg = ViewHolderUtil.getView(convertView, R.id.tv_msg);
        tv_msg.setText(data.get(position).getMsg());
        int type=data.get(position).getType();
        if(type==0){ //超级管理员
            tv_fans.setTextColor(Color.parseColor("#FF4500"));
            tv_msg.setTextColor(Color.parseColor("#ff0000"));
        }else if (type==1){  //特殊消息
            tv_fans.setTextColor(Color.parseColor("#FFFFFF"));
            tv_msg.setTextColor(Color.parseColor("#00FF00"));
        }else if(type==2){ //普通消息
            tv_fans.setTextColor(Color.parseColor("#A9A9A9"));
            tv_msg.setTextColor(Color.parseColor("#ffffff"));
        }else if(type==3){//礼物消息	#FF69B4
            tv_fans.setTextColor(Color.parseColor("#0000FF"));
            tv_msg.setTextColor(Color.parseColor("#FF69B4"));
        }else {
            tv_fans.setTextColor(Color.parseColor("#BA55D3"));
            tv_msg.setTextColor(Color.parseColor("#FFC0CB"));
        }
        return convertView;
    }
}
