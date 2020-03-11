package com.xurent.livedance.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xurent.livedance.R;
import com.xurent.livedance.model.Bean;
import com.xurent.livedance.utils.BitmapUtil;
import com.xurent.myplayer.TimeInfoBean;
import com.xurent.myplayer.player.WLPlayer;
import com.xurent.myplayer.util.WlTimeUtil;
import com.xurent.myplayer.vr.VrSurfaceView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VideoVrAdapter extends RecyclerView.Adapter<VideoVrAdapter.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private ArrayList<Bean> mDatas;
    private ViewHolder holder;




    public VideoVrAdapter(Context context, ArrayList<Bean> datas) {
        mContext = context;
        mDatas = datas;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ViewHolder tv =new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false));
        View tv=LayoutInflater.from(mContext).inflate(R.layout.item_vr_layout, parent, false);
        tv.setOnClickListener(this);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.wlPlayer.setSource(mDatas.get(position).url);
        this.holder=holder;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onClick(View view) {


    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private WLPlayer wlPlayer;
        private VrSurfaceView vrSurfaceView;
        private ImageView changeDisplayModeBtn;
        private ImageView changeInteRactionModeBtn;

        private SeekBar seekBar;
        private LinearLayout linearLayout;
        private  int position;
        private  boolean seek=false;
        private TextView tv_Time;
        private ImageView playState;
        private boolean state=true;
        private boolean ontouch=false;
        private ImageView likeImg;
        private boolean likeState=false;
        private ImageView headImg;
        private TextView numberLike;

        private boolean glasses=false;
        private boolean state_3d=true;

        public void setWlPlayer(WLPlayer wlPlayer) {
            this.wlPlayer = wlPlayer;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            /*if(wlPlayer==null){
                wlPlayer=new WLPlayer();
                wlPlayer.setOpenVr(true);
            }*/
            //vrSurfaceView=itemView.findViewById(R.id.video_vr_view);
            wlPlayer.setVrSurfaceView(vrSurfaceView);
            seekBar=itemView.findViewById(R.id.seekbar);
            tv_Time=itemView.findViewById(R.id.tv_time);
            playState=itemView.findViewById(R.id.play_state);
            linearLayout=itemView.findViewById(R.id.linerlayout);
            likeImg=itemView.findViewById(R.id.like);
            headImg=itemView.findViewById(R.id.head);
            numberLike=itemView.findViewById(R.id.number_like);
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.head);
            //设置bitmap.getWidth()可以获得圆形
            Bitmap bitmap1 = BitmapUtil.ClipSquareBitmap(bitmap,200,bitmap.getWidth());
            headImg.setImageBitmap(bitmap1);

            linearLayout.setVisibility(View.GONE);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    position=progress*wlPlayer.getDuration()/100;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    seek=true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    wlPlayer.seek(position);
                    seek=false;
                }
            });
            vrSurfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ontouch=!ontouch;
                    if(ontouch){
                        linearLayout.setVisibility(View.VISIBLE);

                    }else{
                        linearLayout.setVisibility(View.GONE);
                    }
                }
            });
            changeDisplayModeBtn = itemView.findViewById(R.id.change_display_mode_btn);
            changeInteRactionModeBtn = itemView.findViewById(R.id.change_interaction_mode_btn);

            changeDisplayModeBtn.setOnClickListener(this);
            changeInteRactionModeBtn.setOnClickListener(this);
            likeImg.setOnClickListener(this);
            playState.setOnClickListener(this);
        }


        private void changeInteRactionMode() {
            vrSurfaceView.getVrGlassGLVideoRenderer().changeInteractionMode();
            if(changeInteRactionModeBtn==null)return;
            state_3d=!state_3d;
            if(state_3d){
                changeInteRactionModeBtn.setImageResource(R.mipmap.on_3d);
            }else {
                changeInteRactionModeBtn.setImageResource(R.mipmap.off_3d);
            }
        }


        private void changeDisplayMode() {
            vrSurfaceView.getVrGlassGLVideoRenderer().changeDisplayMode();
            glasses=!glasses;
            if(glasses){
                changeDisplayModeBtn.setImageResource(R.mipmap.glasses_3d);
            }else{
                changeDisplayModeBtn.setImageResource(R.mipmap.glasses_2d);
            }

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.change_display_mode_btn:
                    changeDisplayMode();
                    break;
                case R.id.change_interaction_mode_btn:
                    changeInteRactionMode();
                    break;
                case R.id.like:
                    LikeState();
                    break;
                case R.id.play_state:
                    ChanegState();
                    break;

            }
        }
    //点赞
        public void LikeState() {
            likeState=!likeState;
            if(likeState){
                likeImg.setImageResource(R.mipmap.like);
                numberLike.setText("1");

            }else {
                likeImg.setImageResource(R.mipmap.dislike);
                numberLike.setText("0");
            }
        }

        //暂停播放
        public void ChanegState() {
            state=!state;
            if(state){
                playState.setImageResource(R.mipmap.pause);
                wlPlayer.resume();
            }else{
                playState.setImageResource(R.mipmap.play);
                wlPlayer.pause();
            }
        }

        Handler handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what==1){
                    TimeInfoBean timeInfoBean= (TimeInfoBean) msg.obj;
                    tv_Time.setText(WlTimeUtil.secdsToDateFormat(timeInfoBean.getTotalTime(),timeInfoBean.getTotalTime())
                            +"/"+WlTimeUtil.secdsToDateFormat(timeInfoBean.getCurrentTime(),timeInfoBean.getTotalTime()));

                    if(!seek&&timeInfoBean.getTotalTime()>0){
                        seekBar.setProgress(timeInfoBean.getCurrentTime()*100/timeInfoBean.getTotalTime());
                    }
                }

            }
        };

    }



}
