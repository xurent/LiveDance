package com.xurent.livedance.push;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.MessageAdapter;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoom.utils.MagicTextView;
import com.xurent.livedance.R;
import com.xurent.livedance.camera.CameraView;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.Fans;
import com.xurent.livedance.model.Gift;
import com.xurent.livedance.model.LiveMsg;
import com.xurent.livedance.model.MessageData;
import com.xurent.livedance.ui.adapter.FansAdapter;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.livedance.websoket.WsManager;
import com.xurent.myplayer.log.MyLog;
import com.xurent.myplayer.push.ConnectListenr;
import com.xurent.myplayer.push.PushVideo;
import com.xurent.myplayer.util.WlTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LivePushActivity extends AppCompatActivity {
    private static final int UPDATE_MSG = 0;
    private static final int UPDATE_GIFT = 1;
    private static final int UPDATE_SYSYTEM_MSG_ADD=2;
    private static final int UPDATE_SYSYTEM_MSG_SUB=3;
    private static final int UPDATE_TIME = 4;
    private static final int OPEN_LIVE=5;
    private static final int STOP_LIVE=6;
    // 礼物
    private int[] GiftIcon = new int[]{R.drawable.zem72,
            R.drawable.zem70,
            R.drawable.zem68,
            R.drawable.zem63};
    private PushVideo wlPushVideo;
    private CameraView wlCameraView;
    private PushEncodec wlPushEncodec;
    private Button bt_push;
    private CircleImageView top_head;
    private TextView nickeName;
    private  TextView online;
    private ConstraintLayout behaver;
    private  boolean danmuState=true;
    private ImageView danmu;
    private ListView list_message;
    private boolean cameraState=true;
    private RelativeLayout utils;
    private List<LiveMsg> messageData = new LinkedList<>();
    private MessageAdapter messageAdapter;
    private LinearLayout ll_gift_group;
    private TranslateAnimation outAnim;
    private NumberAnim giftNumberAnim;
    private TranslateAnimation inAnim;
    private TextView state;
    private ImageView StateImg;
    private  TextView time;
    private boolean flag = true;   //用于判断直播是否在连接中
    private UpdateLiveTime updateLiveTime; //更新直播时间
    private int liveTime=0;
    private List<Fans> fanslist=new ArrayList<>();
    private FansAdapter fansAdapter;
    private RecyclerView fan_recyclerView;
    private BottomSheetDialog rankDialog;
    private ImageView rank;
    private TabLayout tabLayout;
    private Retrofit retrofit=null;
    private EditText et_chat;
    private LinearLayout ll_inputparent;
    private ImageView tv_chat;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);
        wlCameraView=findViewById(R.id.cameraview);
        bt_push=findViewById(R.id.push_bt);
        top_head=findViewById(R.id.lv_anchorIcon);
        online=findViewById(R.id.online_num);
        nickeName=findViewById(R.id.nickname);
        state=findViewById(R.id.state);
        nickeName.setText(Constants.nickName);
        behaver=findViewById(R.id.behaver);
        danmu=findViewById(R.id.danmu_open);
        utils=findViewById(R.id.utils);
        et_chat = findViewById(R.id.et_chat);
        time=findViewById(R.id.time);
        list_message=findViewById(R.id.lv_message);
        ll_gift_group=findViewById(R.id.ll_gift_group);
        ll_inputparent =findViewById(R.id.ll_inputparent);
        tv_chat = findViewById(R.id.tv_chat);
        StateImg=findViewById(R.id.state_img);
        wlCameraView.setWaterMark(Constants.nickName);
        Glide.with(this)
                .load(Constants.headImage)
                .thumbnail(Glide.with(this)
                        .load(Constants.headImage))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(top_head);
        initLive();//初始化Live
    }

    public void initData(){
        initNet();//初始化网络
        initMessage();//初始化消息
        clearTiming(); // 开启定时清理礼物列表
        initAnim();//初始化动画

    }
    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
    }

    /**
     * 初始化动画
     */
    private void initAnim() {
        giftNumberAnim = new NumberAnim(); // 初始化数字动画
        inAnim = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.gift_in); // 礼物进入时动画
        outAnim = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.gift_out); // 礼物退出时动画
    }

    public void initMessage(){
        LiveMsg msg=new LiveMsg();
        msg.setFansName("【直播公告】");
        msg.setMsg("为维护直播氛围,请勿直播色情,暴力等内容,违者将封号噢!");
        messageData.add(msg);
        LiveMsg msg1=new LiveMsg();
        msg1.setUrl("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3017334715,3640667871&fm=26&gp=0.jpg");
        msg1.setFansName("【房管】陈甫佳");
        msg1.setMsg("你怎么长这么好看!");
        msg1.setType(1);
        messageData.add(msg1);
        LiveMsg msg2=new LiveMsg();
        msg2.setFansName("粉丝唐三");
        msg2.setUrl("https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3953708325,2497493931&fm=175&app=25&f=JPEG?w=640&h=640&s=0BC4B844945277DCBC6DE09C03009089");
        msg2.setMsg("小舞快来打这个房管");
        msg2.setType(3);
        messageData.add(msg2);
        messageAdapter = new MessageAdapter(this, messageData);
        list_message.setAdapter(messageAdapter);
        list_message.setSelection(messageData.size());
    }

public void initLive(){

    wlPushVideo=new PushVideo();
    wlPushVideo.setConnectListenr(new ConnectListenr() {
        @Override
        public void onConnecting() {
            Log.d("xurent","连接流媒体服务器中...");
        }

        @Override
        public void onConnectSuccess() {
            Log.d("xurent","连接服务器成功!");
            wlPushEncodec=new PushEncodec(LivePushActivity.this,wlCameraView.getTextrueId());
            wlPushEncodec.initEncodec(wlCameraView.getEglContext(),720,1280);
            wlPushEncodec.setWaterMark(Constants.nickName);
            wlPushEncodec.startRecord();
            //
           Message msg=Message.obtain();
           msg.what=OPEN_LIVE;
           handler.sendMessage(msg);
            wlPushEncodec.setOnMediaInfoListener(new BasePushEncoder.OnMediaInfoListener() {
                @Override
                public void onMediaTime(int times) {
                    MyLog.d("消息:"+times);

                }
                @Override
                public void onSPSPPSInfo(byte[] sps, byte[] pps) {

                    wlPushVideo.pushSPSPPS(sps,pps);
                }

                @Override
                public void onVideoInfo(byte[] data, boolean keyframe) {

                    wlPushVideo.pushVideoData(data,keyframe);
                }

                @Override
                public void onAudioInfo(byte[] data) {
                    wlPushVideo.pushAudioData(data);
                }
            });
        }

        @Override
        public void onConnectFail(String msg) {
            Log.d("xurent",msg);
            Message msg0=Message.obtain();
            msg0.what=STOP_LIVE;
            handler.sendMessage(msg0);
        }
    });
}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
            new MaterialAlertDialogBuilder(this)
                    .setTitle("退出房间")
                    .setPositiveButton("继续直播", null)
                    .setNeutralButton("关闭直播", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            release();
                            finish();
                        }
                    }).create().show();
            return false;
        }
        return super.onKeyDown(keyCode, event);

    }

    public void startpush(View view) {
            utils.setVisibility(View.VISIBLE);
            behaver.setVisibility(View.VISIBLE);
            bt_push.setVisibility(View.GONE);
            initData();
            ListenWebsoket();
            Push();

    }


    private void initNet(){
         retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(this))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private void Push(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.openLive();
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data.toString());
                int code=data.get("code").getAsInt();
                if(code==0){
                  String url=  data.getAsJsonObject("data").get("pushUrl").getAsString();
                  wlPushVideo.initLivePush(url);
                  System.out.println(url);
                }else {
                    Toast.makeText(LivePushActivity.this,"推流失败,请退出重试",0).show();
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(LivePushActivity.this,"网络异常请重试",0).show();
            }
        });

    }

    private void  ListenWebsoket(){
        String []u=Constants.token.split(";");
        WsManager.getInstance().init(Constants.ws_room+Constants.uid+"/"+u[0].replaceAll("token=",""),this);
        WsManager.setReConnect(true);//允许重连
        WsManager.setWsCallBack(new WsManager.WsCallBack() {
            @Override
            public void getMessage(JSONObject data) throws JSONException {
                int code=data.getInt("code");
                switch (code){
                    case 10001:SystemMsg(data,10001);
                        break;
                    case 10002:SystemMsg(data,10002);
                        break;
                    case 10003:TextMsg(data);
                        break;
                    case 10004:GiftMsg(data);
                        break;
                }
            }
        });
    }

    private void SystemMsg(JSONObject data,int type) throws JSONException {
        LiveMsg msg=new LiveMsg();
        msg.setMsg(data.getString("msg"));
        msg.setFansName("【直播间】");
        msg.setType(4);
        messageData.add(msg);
        Message M=Message.obtain();
        M.what=UPDATE_SYSYTEM_MSG_ADD;
        if(type==10001){
            M.what=UPDATE_SYSYTEM_MSG_ADD;
        }else if(type==10002){
            M.what=UPDATE_SYSYTEM_MSG_SUB;
        }
        handler.sendMessage(M);
    }



    private void TextMsg(JSONObject data) throws JSONException {
        Gson gson=new Gson();
        MessageData msg= gson.fromJson(data.getString("data"),MessageData.class);
        LiveMsg liveMsg=  msg.getMsg();
        if(!liveMsg.getUid().equals(Constants.uid)){
            liveMsg.setType(2);
        }
        messageData.add(liveMsg);
        Message M=Message.obtain();
        M.what=UPDATE_MSG;
        handler.sendMessage(M);
    }

    private void GiftMsg(JSONObject data) throws JSONException {
        Gson gson=new Gson();
        MessageData msg= gson.fromJson(data.getString("data"),MessageData.class);
        LiveMsg liveMsg=  msg.getMsg();
        Gift gift=msg.getGift();
        liveMsg.setMsg(data.getString("msg")+gift.getName()+"数量:"+gift.getNumber());
        liveMsg.setType(3);
        messageData.add(liveMsg);
        Message M=Message.obtain();
        M.what=UPDATE_GIFT;
        M.obj=msg;
        handler.sendMessage(M);

    }


    //退出房间
    public void exit(View view) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("退出房间")
                .setPositiveButton("继续直播", null)
                .setNeutralButton("关闭直播", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        release();
                        finish();
                    }
                }).create().show();
    }
    /**
     * 刷礼物的方法
     */
    private void showGift(String tag, String name, int num) {
        View newGiftView = ll_gift_group.findViewWithTag(tag);
        // 是否有该tag类型的礼物
        if (newGiftView == null) {
            // 判断礼物列表是否已经有3个了，如果有那么删除掉一个没更新过的, 然后再添加新进来的礼物，始终保持只有3个
            if (ll_gift_group.getChildCount() >= 3) {
                // 获取前2个元素的最后更新时间
                View giftView01 = ll_gift_group.getChildAt(0);
                ImageView iv_gift01 = giftView01.findViewById(R.id.iv_gift);
                long lastTime1 = (long) iv_gift01.getTag();

                View giftView02 = ll_gift_group.getChildAt(1);
                ImageView iv_gift02 = giftView02.findViewById(R.id.iv_gift);
                long lastTime2 = (long) iv_gift02.getTag();

                if (lastTime1 > lastTime2) { // 如果第二个View显示的时间比较长
                    removeGiftView(1);
                } else { // 如果第一个View显示的时间长
                    removeGiftView(0);
                }
            }

            // 获取礼物
            newGiftView = getNewGiftView(tag,num,name);
            ll_gift_group.addView(newGiftView);

            // 播放动画
            newGiftView.startAnimation(inAnim);
            final MagicTextView mtv_giftNum = newGiftView.findViewById(R.id.mtv_giftNum);
            inAnim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    giftNumberAnim.showAnimator(mtv_giftNum);
                }
            });
        } else {
            // 如果列表中已经有了该类型的礼物，则不再新建，直接拿出
            // 更新标识，记录最新修改的时间，用于回收判断
            ImageView iv_gift = newGiftView.findViewById(R.id.iv_gift);
            iv_gift.setTag(System.currentTimeMillis());
            TextView fansName=newGiftView.findViewById(R.id.fans_name);
            fansName.setText(name);

            // 更新标识，更新记录礼物个数
            MagicTextView mtv_giftNum = newGiftView.findViewById(R.id.mtv_giftNum);
            int giftCount = (int) mtv_giftNum.getTag() + num; // 递增
            mtv_giftNum.setText("x" + giftCount);
            mtv_giftNum.setTag(giftCount);
            giftNumberAnim.showAnimator(mtv_giftNum);
        }
    }
    /**
     * 获取礼物
     */
    private View getNewGiftView(String tag,Integer num,String name) {

        // 添加标识, 该view若在layout中存在，就不在生成（用于findViewWithTag判断是否存在）
        View giftView = LayoutInflater.from(this).inflate(R.layout.item_gift, null);
        giftView.setTag(tag);

        // 添加标识, 记录生成时间，回收时用于判断是否是最新的，回收最老的
        ImageView iv_gift = giftView.findViewById(R.id.iv_gift);
        iv_gift.setTag(System.currentTimeMillis());

        TextView fans_name=giftView.findViewById(R.id.fans_name);
        fans_name.setText(name);
        // 添加标识，记录礼物个数
        MagicTextView mtv_giftNum = giftView.findViewById(R.id.mtv_giftNum);
        mtv_giftNum.setTag(num);
        mtv_giftNum.setText("x"+num);

        switch (tag) {
            case "gift01":
                iv_gift.setImageResource(GiftIcon[0]);
                break;
            case "gift02":
                iv_gift.setImageResource(GiftIcon[1]);
                break;
            case "gift03":
                iv_gift.setImageResource(GiftIcon[2]);
                break;
            case "gift04":
                iv_gift.setImageResource(GiftIcon[3]);
                break;
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 10;
        giftView.setLayoutParams(lp);

        return giftView;
    }

    private void release(){

        if(wlPushEncodec!=null){
               wlPushEncodec.stopRecord();
               wlPushVideo.stopPush();
               wlPushEncodec=null;
           }
        flag=false;
        messageData.clear();
        messageData=null;
        WsManager.setReConnect(false);//关闭重连
        WsManager.getInstance().disconnect();
    }

    /**
     * 定时清理礼物列表信息
     */
    private void clearTiming() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                int childCount = ll_gift_group.getChildCount();
                long nowTime = System.currentTimeMillis();
                for (int i = 0; i < childCount; i++) {

                    View childView = ll_gift_group.getChildAt(i);
                    ImageView iv_gift = (ImageView) childView.findViewById(R.id.iv_gift);
                    long lastUpdateTime = (long) iv_gift.getTag();

                    // 更新超过3秒就刷新
                    if (nowTime - lastUpdateTime >= 3000) {
                        removeGiftView(i);
                    }
                }
            }
        }, 0, 3000);
    }

    /**
     * 移除礼物列表里的giftView
     */
    private void removeGiftView(final int index) {
        // 移除列表，外加退出动画
        final View removeGiftView = ll_gift_group.getChildAt(index);
        outAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ll_gift_group.removeViewAt(index);
            }
        });

        // 开启动画，因为定时原因，所以可能是在子线程
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeGiftView.startAnimation(outAnim);
            }
        });
    }
    //弹幕
    public void Danmu(View view) {
        if(danmuState){
            danmu.setImageResource(R.mipmap.mopen);
            list_message.setVisibility(View.VISIBLE);
        }else{
            danmu.setImageResource(R.mipmap.moff);
            list_message.setVisibility(View.GONE);
        }
        danmuState=!danmuState;
    }

    //切换摄像头
    public void ChangeCamera(View view) {
        cameraState=!cameraState;
        if(cameraState){
            wlCameraView.setCameraId(CameraView.FRONT);
        }else{
            wlCameraView.setCameraId(CameraView.BACK);
        }
    }

    //礼物榜
    public void showGift(View view) {
        Rank();
    }

    private void Rank(){
        rankDialog = new BottomSheetDialog(this);
        rankDialog.setContentView(R.layout.rank_bottom_sheet);
        View modalBottomSheetChildView = rankDialog.findViewById(R.id.bottom_drawer);
        fan_recyclerView=rankDialog.findViewById(R.id.fans_recycle);
        tabLayout=rankDialog.findViewById(R.id.tabLayoutHome);
        initDialogFans( rankDialog.getContext());

        ViewGroup.LayoutParams layoutParams = modalBottomSheetChildView.getLayoutParams();
        layoutParams.height=400;
        modalBottomSheetChildView.setLayoutParams(layoutParams);
        rankDialog.show();

    }

    private void initDialogFans(Context context){
        fansAdapter=new FansAdapter(this,fanslist);
        getFansData(0);
        fan_recyclerView.setAdapter(fansAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(context,1);
        fan_recyclerView.setLayoutManager(layoutManager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index= tab.getPosition();
                if(index==0){
                    getFansData(0);
                }else  if(index==1){
                    getFansData(1);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getFansData(int type){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.getFansAndOther(Constants.uid,type);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    if(data.get("data").isJsonNull()){
                        fanslist.clear();
                        fansAdapter.notifyDataSetChanged();
                        return;
                    }
                    JsonArray array=data.get("data").getAsJsonArray();
                    Gson gson=new Gson();
                    Type type = new TypeToken<List<Fans>>(){}.getType();
                    ArrayList<Fans> datas=gson.fromJson(array.toString(), type);
                    fanslist.clear();
                    fanslist.addAll(datas);
                    fansAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }

    //点击显示发送消息的文本框
    public void SendText(View view) {
        tv_chat.setVisibility(View.GONE);
        ll_inputparent.setVisibility(View.VISIBLE);
        ll_inputparent.requestFocus(); // 获取焦点
        showKeyboard();

    }

    //发送消息
    public void tvSend(View view) {
        String chatMsg = et_chat.getText().toString();
        if (!TextUtils.isEmpty(chatMsg)) {
            et_chat.setText("");
            sendChat(chatMsg);
        }
        hideKeyboard();
        if (ll_inputparent.getVisibility() == View.VISIBLE) {
            tv_chat.setVisibility(View.VISIBLE);
            ll_inputparent.setVisibility(View.GONE);
            hideKeyboard();
        }
    }

    /**
     * 发送聊天内容
     * @param text
     */
    private void sendChat(String text){
        LiveMsg msg=new LiveMsg(Constants.headImage,"【房主】",text,0);
        msg.setUid(Constants.uid);
        Gson gson=new Gson();
        String data=gson.toJson(MessageData.ofMsg(10003,msg));
        WsManager.getInstance().getWs().sendText(data);
    }

    /**
     * 显示软键盘
     */
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_chat, InputMethodManager.SHOW_FORCED);
    }

    public  class NumberAnim {
        private Animator lastAnimator;

        public void showAnimator(View v) {

            if (lastAnimator != null) {
                lastAnimator.removeAllListeners();
                lastAnimator.cancel();
                lastAnimator.end();
            }
            ObjectAnimator animScaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.3f, 1.0f);
            ObjectAnimator animScaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.3f, 1.0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animScaleX, animScaleY);
            animSet.setDuration(200);
            lastAnimator = animSet;
            animSet.start();
        }
    }


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_MSG:
                    break;
                case UPDATE_SYSYTEM_MSG_ADD:
                    try {
                        int num=Integer.parseInt(online.getText().toString())+1;
                        online.setText(String.valueOf(num));
                    }catch (Exception e){
                    }
                    break;
                case UPDATE_SYSYTEM_MSG_SUB:
                    try {
                        int tnum=Integer.parseInt(online.getText().toString())-1;
                        online.setText(String.valueOf(tnum));
                    }catch (Exception e){
                    }
                    break;
                case  UPDATE_GIFT:
                    MessageData code= (MessageData) msg.obj;
                    if(code.getGift()!=null)
                    showGift(code.getGift().getCode(),code.getMsg().getFansName(),code.getGift().getNumber());
                    break;
                case  UPDATE_TIME:
                    time.setText(WlTimeUtil.secdsToDateFormat(++liveTime,Integer.MAX_VALUE));
                    return;
                case OPEN_LIVE:
                    StateImg.setImageResource(R.mipmap.green_piont);
                    state.setText("正在直播");
                    updateLiveTime=new UpdateLiveTime();
                    flag=true;
                    new Thread(updateLiveTime).start();
                    return;
                case STOP_LIVE:
                    StateImg.setImageResource(R.mipmap.red_piont);
                    state.setText("直播停止");
                    flag=false;
                    return;
            }
            messageAdapter.NotifyAdapter(messageData);
            list_message.setSelection(messageData.size());
        }
    };

    //每隔1秒更新一下进度条线程
    class UpdateLiveTime implements Runnable {
        @Override
        public void run() {
            handler.sendEmptyMessage(UPDATE_TIME);
            if (flag) {
                handler.postDelayed(updateLiveTime, 1000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(WsManager.getInstance()!=null){
            WsManager.getInstance().disconnect();
        }
        flag=false;
       if(messageData!=null){
           messageData.clear();
           messageData=null;
       }
        updateLiveTime=null;
        super.onDestroy();
    }
}
