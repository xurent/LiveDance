package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.xurent.livedance.R;
import com.xurent.livedance.model.Music;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private Context mContext;
    private List<Music> mdata ;
    private OnItemSelect onItemSelect;

    public void setOnItemSelect(OnItemSelect onItemSelect) {
        this.onItemSelect = onItemSelect;
    }

    public MusicAdapter(Context mContext, List<Music> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
    }


    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View tv=LayoutInflater.from(mContext).inflate(R.layout.item_music, parent, false);
        ViewHolder holder=new ViewHolder(tv);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ViewHolder holder, int position) {
        Music music=mdata.get(position);
        Glide.with(mContext)
                .load(music.getImageUrl())
                .thumbnail(Glide.with(mContext)
                        .load(music.getImageUrl()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(holder.musicImg);
      holder.musicNmae.setText(music.getName());
      holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
          @Override
          public void onFocusChange(View view, boolean b) {
              if(onItemSelect!=null){
                  System.out.println("选择。。。。");
                  onItemSelect.OnItemselect(view,position);
              }
          }
      });
    }
    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView musicNmae;
        private ShapeableImageView musicImg;
        public ViewHolder(View view) {
            super(view);
            musicNmae=view.findViewById(R.id.music_name);
            musicImg=view.findViewById(R.id.music_img);
        }

    }

    public interface OnItemSelect{

        void OnItemselect(View v,int position);
    }

}
