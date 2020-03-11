package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.xurent.livedance.R;
import com.xurent.livedance.model.Gift;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.ViewHolder> {

    private Context mContext;
    private List<Gift> mdata ;
    private OnItemListner onItemListner;

    public void setOnItemListner(OnItemListner onItemListner) {
        this.onItemListner = onItemListner;
    }

    public GiftAdapter(Context mContext, List<Gift> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }


    @NonNull
    @Override
    public GiftAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.gift_menu_item, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GiftAdapter.ViewHolder holder, int position) {
        Gift gift=mdata.get(position);
        int src=R.drawable.zem63;
        switch (gift.getCode()){
            case "gift01":
                src=R.drawable.zem72;
                break;
            case "gift02":
                src=R.drawable.zem70;
                break;
            case "gift03":
                src=R.drawable.zem68;
                break;
            case "gift04":
                src=R.drawable.zem63;
                break;
        }
        holder.gift.setImageResource(src);
        holder.gift_name.setText(gift.getName());
        holder.price.setText(gift.getPrice()+"");

        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(onItemListner!=null){
                        onItemListner.onItemClick(view,position);
                    }
                }
            }
        });

    }
    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gift;
        private TextView  gift_name;
        private TextView price;
        public ViewHolder(View view) {
            super(view);
            gift=view.findViewById(R.id.image);
            gift_name=view.findViewById(R.id.gift_name);
            price=view.findViewById(R.id.price);
        }

    }

    public interface  OnItemListner{
        void onItemClick(View view, int position);
    }

}
