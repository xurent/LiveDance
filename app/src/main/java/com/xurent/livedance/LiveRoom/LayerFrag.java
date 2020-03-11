package com.xurent.livedance.LiveRoom;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xurent.livedance.LiveRoom.utils.CircleImageView;
import com.xurent.livedance.LiveRoom.utils.DisplayUtil;
import com.xurent.livedance.LiveRoom.utils.HorizontalListView;
import com.xurent.livedance.LiveRoom.utils.MagicTextView;
import com.xurent.livedance.LiveRoom.utils.SoftKeyBoardListener;
import com.xurent.livedance.LiveRoomActivity;
import com.xurent.livedance.NetStatusReceiver;
import com.xurent.livedance.R;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.http.RoomService;
import com.xurent.livedance.model.Fans;
import com.xurent.livedance.model.Gift;
import com.xurent.livedance.model.LiveMsg;
import com.xurent.livedance.model.LiveRoomBean;
import com.xurent.livedance.model.MessageData;
import com.xurent.livedance.push.LivePushActivity;
import com.xurent.livedance.ui.adapter.FansAdapter;
import com.xurent.livedance.ui.adapter.GiftAdapter;
import com.xurent.livedance.utils.OKhttpUtils;
import com.xurent.livedance.websoket.WsManager;
import com.xurent.myplayer.util.WlTimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LayerFrag extends Fragment implements View.OnClickListener {

    private static final int UPDATE_MSG = 0;
    private static final int UPDATE_GIFT = 1;
    private static final int UPDATE_SYSYTEM_MSG_ADD=2;
    private static final int UPDATE_SYSYTEM_MSG_SUB=3;
    // 礼物
    private int[] GiftIcon = new int[]{R.drawable.zem72,
            R.drawable.zem70,
            R.drawable.zem68,
            R.drawable.zem63};
    private  GiftAdapter adapter;
    private NumberAnim giftNumberAnim;
    private List<LiveMsg> messageData = new LinkedList<>();
    private MessageAdapter messageAdapter;
    private ListView lv_message;
    //private HorizontalListView hlv_audience;
    private LinearLayout ll_gift_group;
    private TranslateAnimation outAnim;
    private TranslateAnimation inAnim;
    private LinearLayout ll_inputparent;
    private ImageView tv_chat;
    private TextView tv_send;
    private EditText et_chat;
    private RelativeLayout ll_anchor;
    private RelativeLayout rl_num;
    private RecyclerView recyclerView;
    private Context myContext;
    private ImageView iv_gift;
    private View view;
    private LiveRoomBean achor;
    private TextView anchorName;
    private  TextView online;
    private CircleImageView headTop;
    private TextView id;
    private MaterialButton focusAnchor;
    private boolean focus=false;
    private Retrofit retrofit=null ;
    private TextInputEditText gift_number;
    private MaterialButton sendGift;
    private long MyBlance=0;
    private  BottomSheetDialog bottomSheetDialog;
    private BottomSheetDialog rankDialog;
    private  TextView balance;
    private ImageView rank;
    private TabLayout tabLayout;
    private OnOpenLiveListner onOpenLiveListner;
    private NetStatusReceiver netStatusReceiver ;
    public void setOnOpenLiveListner(OnOpenLiveListner onOpenLiveListner) {
        System.out.println("设置监听成功");
        this.onOpenLiveListner = onOpenLiveListner;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_layer, null);
        myContext = getActivity();
        LiveRoomActivity activity= (LiveRoomActivity) myContext;
        achor=activity.data;
        initView(view);
        initData();
        // 动态注册广播接收者
        netStatusReceiver = new NetStatusReceiver ();
        // 创建IntentFilter对象
        IntentFilter filter = new IntentFilter();
        // 添加要注册的action
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // 动态注册广播接收者
       if(myContext!=null){
           try {
               myContext.registerReceiver(netStatusReceiver,filter);
           }catch (Exception e){
               e.printStackTrace();
           }
       }
        return view;
    }


    public void initView(View view) {
        lv_message = view.findViewById(R.id.lv_message);
        //hlv_audience = view.findViewById(R.id.hlv_audience);
        ll_gift_group = view.findViewById(R.id.ll_gift_group);
        ll_inputparent = view.findViewById(R.id.ll_inputparent);
        tv_chat = view.findViewById(R.id.tv_chat);
        tv_send = view.findViewById(R.id.tv_send);
        et_chat = view.findViewById(R.id.et_chat);
        ll_anchor = view.findViewById(R.id.ll_anchor);
        rl_num = view.findViewById(R.id.rl_num);
        iv_gift = view.findViewById(R.id.gift);
        headTop=view.findViewById(R.id.lv_anchorIcon);
        anchorName=view.findViewById(R.id.anchorName);
        focusAnchor=view.findViewById(R.id.focusAnchor);
        rank=view.findViewById(R.id.rank);
        online=view.findViewById(R.id.online);
        id=view.findViewById(R.id.tv_momocode);
        id.setText("ID: "+achor.getUsername());
        online.setText(achor.getOnline()+"");
        online.setTag(achor.getOnline());
        anchorName.setText(achor.getNickname());
        Glide.with(this)
                .load(achor.getAnchorImg())
                .thumbnail(Glide.with(this)
                        .load(achor.getAnchorImg()))
                .centerCrop()
                .placeholder(R.drawable.default_tv)
                .into(headTop);

    }

    private int choose_gift=0;

    private void initGift(Context context){
        List<Gift> list=new ArrayList<>();
        Gift gift =new Gift();
        gift.setName("完美蛋糕");
        gift.setPrice(5000);
        gift.setCode("gift04");

        Gift gift1 =new Gift();
        gift1.setName("比心");
        gift1.setPrice(2000);
        gift1.setCode("gift03");

        Gift gift2 =new Gift();
        gift2.setName("玫瑰花");
        gift2.setPrice(888);
        gift2.setCode("gift02");

        Gift gift3 =new Gift();
        gift3.setName("肥皂");
        gift3.setPrice(6666);
        gift3.setCode("gift01");

        list.add(gift);
        list.add(gift1);
        list.add(gift2);
        list.add(gift3);
        adapter=new GiftAdapter(getContext(),list);
        GridLayoutManager layoutManager = new GridLayoutManager(context,1);
        layoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(true);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemListner(new GiftAdapter.OnItemListner() {
            @Override
            public void onItemClick(View view, int pos) {
                System.out.println("av"+","+pos);
                choose_gift=pos;
            }

        });
        sendGift.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
               Gift gift= list.get(choose_gift);
               String num=gift_number.getText().toString().trim();
               int number =Integer.parseInt(num);
               gift.setNumber(number);
                if (number > 0) {
                    if(MyBlance-gift.getPrice()*number>=0){
                        GveMoney((gift.getPrice()*number),gift);
                    }else{
                        Toast.makeText(getContext(),"余额不足",0).show();
                    }
                }
            }
        });
    }

    private void getPrice(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.myAcount();
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    MyBlance= data.get("data").getAsLong();
                }
            }
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            if(getContext()!=null){
               Toast.makeText(getContext(),"网络异常",0).show();
             }
            }
        });
    }

    /**
     * 打赏
     */
    private void GveMoney(long acount,Gift gift){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.giveGift(achor.getUsername(),acount);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    MyBlance= data.get("data").getAsLong();
                    System.out.println(gift.toString());
                    sendGift(gift.getCode(),gift.getNumber());
                }else {
                    Toast.makeText(getContext(),"余额不足",0).show();
                }
                bottomSheetDialog.cancel();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("获取金额失败");
            }
        });
    }


    private void initData() {
        //initAudience(); // 初始化观众
        initMessage(); // 初始化评论
        initListener();
        clearTiming(); // 开启定时清理礼物列表
        initAnim(); // 初始化动画
        initNet();//初始化网络
        getIsFocus();
        InitWebSoket();//初始化通讯
        getPrice();//获取金额
    }



    /**
     * 初始化评论列表
     */
    private void initMessage() {
        if(TextUtils.isEmpty(achor.getAnnouncement())){
            return;
        }
        LiveMsg msg=new LiveMsg();
        msg.setMsg(achor.getAnnouncement());
        msg.setType(0);
        msg.setFansName("【公告】");
        messageData.add(msg);
        messageAdapter = new MessageAdapter(getActivity(), messageData);
        lv_message.setAdapter(messageAdapter);
        lv_message.setSelection(messageData.size());
    }

    private void initListener() {
     /*   btn_gift01.setOnClickListener(this);
        btn_gift02.setOnClickListener(this);
        btn_gift03.setOnClickListener(this);*/
        rank.setOnClickListener(this);
        iv_gift.setOnClickListener(this);
        tv_chat.setOnClickListener(this);
        tv_send.setOnClickListener(this);
        focusAnchor.setOnClickListener(this);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ll_inputparent.getVisibility() == View.VISIBLE) {
                    tv_chat.setVisibility(View.VISIBLE);
                    ll_inputparent.setVisibility(View.GONE);
                    hideKeyboard();
                }
            }
        });

        // 软键盘监听
        SoftKeyBoardListener.setListener(getActivity(), new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {/*软键盘显示：执行隐藏title动画，并修改listview高度和装载礼物容器的高度*/

                // 输入文字时的界面退出动画
                AnimatorSet animatorSetHide = new AnimatorSet();
                ObjectAnimator leftOutAnim = ObjectAnimator.ofFloat(rl_num, "translationX", 0, -rl_num.getWidth());
                ObjectAnimator topOutAnim = ObjectAnimator.ofFloat(ll_anchor, "translationY", 0, -ll_anchor.getHeight());
                animatorSetHide.playTogether(leftOutAnim, topOutAnim);
                animatorSetHide.setDuration(300);
                animatorSetHide.start();
                // 改变listview的高度
                dynamicChangeListviewH(90);
                dynamicChangeGiftParentH(true);
            }

            @Override
            public void keyBoardHide(int height) {/*软键盘隐藏：隐藏聊天输入框并显示聊天按钮，执行显示title动画，并修改listview高度和装载礼物容器的高度*/
                tv_chat.setVisibility(View.VISIBLE);
                ll_inputparent.setVisibility(View.GONE);
                // 输入文字时的界面进入时的动画
                AnimatorSet animatorSetShow = new AnimatorSet();
                ObjectAnimator leftInAnim = ObjectAnimator.ofFloat(rl_num, "translationX", -rl_num.getWidth(), 0);
                ObjectAnimator topInAnim = ObjectAnimator.ofFloat(ll_anchor, "translationY", -ll_anchor.getHeight(), 0);
                animatorSetShow.playTogether(leftInAnim, topInAnim);
                animatorSetShow.setDuration(300);
                animatorSetShow.start();

                // 改变listview的高度
                dynamicChangeListviewH(150);
                dynamicChangeGiftParentH(false);
            }
        });


    }

    private void release(){
        WsManager.setReConnect(false);//关闭重连机制
        WsManager.getInstance().disconnect();
        if(messageData!=null){
            messageData.clear();
            messageData=null;
        }
        if(fanslist!=null){
            fanslist.clear();
            fanslist=null;
        }
        retrofit=null;
        if(myContext!=null){
            myContext.unregisterReceiver(netStatusReceiver);
        }

    }
    private void initNet(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.room_url) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .client(OKhttpUtils.getOkHttpClient(getContext()))
                .build();
    }


    private void InitWebSoket(){
        String token=Constants.token.toString().split(";")[0].replaceAll("token=","");
        WsManager.getInstance().init(Constants.ws_room+achor.getUsername()+"/"+token,getContext());
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
        if(msg.getUid().equals(achor.getUsername())){//房主进入
            if(onOpenLiveListner!=null){
                onOpenLiveListner.openLive();
            }
        }
    }



    private void TextMsg(JSONObject data) throws JSONException {
        Gson gson=new Gson();
        MessageData msg= gson.fromJson(data.getString("data"),MessageData.class);
        LiveMsg liveMsg=  msg.getMsg();
        if(!liveMsg.getUid().equals(achor.getUsername())){
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


    /**
     * 初始化动画
     */
    private void initAnim() {
        giftNumberAnim = new NumberAnim(); // 初始化数字动画
        inAnim = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.gift_in); // 礼物进入时动画
        outAnim = (TranslateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.gift_out); // 礼物退出时动画
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rank: // 排行榜
                    Rank();
                break;
            case R.id.gift:
                MenuGift();
                break;
            case R.id.tv_chat:// 聊天
                tv_chat.setVisibility(View.GONE);
                ll_inputparent.setVisibility(View.VISIBLE);
                ll_inputparent.requestFocus(); // 获取焦点
                showKeyboard();
                break;
            case R.id.tv_send:// 发送消息
                String chatMsg = et_chat.getText().toString();
                if (!TextUtils.isEmpty(chatMsg)) {
                    et_chat.setText("");
                    sendChat(chatMsg);
                }
                hideKeyboard();
                break;
            case R.id.focusAnchor:
                if(focus){
                    LikeHer(0);
                }else{
                    LikeHer(1);
                }

                break;
        }
    }

    private List<Fans> fanslist=new ArrayList<>();
    private  FansAdapter fansAdapter;
    private RecyclerView fan_recyclerView;
    private void Rank(){
        rankDialog = new BottomSheetDialog(getContext());
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
        fansAdapter=new FansAdapter(getContext(),fanslist);
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
        Call<JsonObject> call=service.getFansAndOther(achor.getUsername(),type);
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


    private void MenuGift(){
        // Set up BottomSheetDialog
         bottomSheetDialog = new BottomSheetDialog(getContext(),R.style.GiftDialog);
        bottomSheetDialog.setContentView(R.layout.cat_bottomsheet_scrollable_content);
        View modalBottomSheetChildView = bottomSheetDialog.findViewById(R.id.bottom_drawer);
        recyclerView=bottomSheetDialog.findViewById(R.id.gift_recycle);
         gift_number=bottomSheetDialog.findViewById(R.id.gift_num);
        sendGift=bottomSheetDialog.findViewById(R.id.send_gift);
        balance=bottomSheetDialog.findViewById(R.id.balance);
        balance.setText(""+MyBlance);
        initGift( bottomSheetDialog.getContext());

        ViewGroup.LayoutParams layoutParams = modalBottomSheetChildView.getLayoutParams();
        layoutParams.height=175;
        modalBottomSheetChildView.setLayoutParams(layoutParams);
        bottomSheetDialog.show();

    }

    /**
     * 发送聊天
     * @param text
     */
    private void sendChat(String text){
        LiveMsg msg=new LiveMsg(Constants.headImage,Constants.nickName,text,2);
        msg.setUid(Constants.uid);
        Gson gson=new Gson();
        String data=gson.toJson(MessageData.ofMsg(10003,msg));
        WsManager.getInstance().getWs().sendText(data);
    }

    //获取是否关注
    private void getIsFocus(){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.isFocus(achor.getUsername());
        System.out.println(call.request());
       call.enqueue(new Callback<JsonObject>() {
           @Override
           public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
               JsonObject obj=response.body();
               if(obj.get("code").getAsInt()==0){
                   if(obj.get("data").getAsInt()==1){
                       focus=true;
                       focusAnchor.setText("取消");
                       System.out.println("已经关注，");
                   }else{
                       focus=false;
                       focusAnchor.setText("关注");
                       System.out.println("没有关注，");
                   }
               }
           }

           @Override
           public void onFailure(Call<JsonObject> call, Throwable t) {

           }
       });

    }

    //关注
    private void LikeHer(int type){
        RoomService service=retrofit.create(RoomService.class);
        Call<JsonObject> call=service.FoucusAnchor(achor.getUsername(),type);
        System.out.println(call.request());
        call.enqueue(new Callback<JsonObject>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject data=response.body();
                System.out.println(data);
                if(data.get("code").getAsInt()==0){
                    Toast.makeText(getContext(),"成功!",0).show();
                    if(type==0){
                        focusAnchor.setText("关注");
                    }else if(type==1){
                        focusAnchor.setText("取消");
                    }
                    focus=!focus;
                }else{
                    Toast.makeText(getContext(),"操作失败!",0).show();
                }
            }

            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(),"网络异常!",0).show();
            }
        });
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

    /**
     * 刷礼物的方法
     */
    private void showGift(String tag,String fans,Integer num,String url) {
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
            newGiftView = getNewGiftView(tag,num,fans);
            ll_gift_group.addView(newGiftView);

            CircleImageView iv_head=newGiftView.findViewById(R.id.cv_send_gift_userIcon);
            Glide.with(newGiftView.getContext())
                    .load(url)
                    .thumbnail(Glide.with(newGiftView.getContext())
                            .load(url))
                    .centerCrop()
                    .placeholder(R.drawable.head)
                    .into(iv_head);
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
            CircleImageView iv_head=newGiftView.findViewById(R.id.cv_send_gift_userIcon);
            Glide.with(newGiftView.getContext())
                    .load(url)
                    .thumbnail(Glide.with(newGiftView.getContext())
                            .load(url))
                    .centerCrop()
                    .placeholder(R.drawable.head)
                    .into(iv_head);

            // 更新标识，更新记录礼物个数
            MagicTextView mtv_giftNum = newGiftView.findViewById(R.id.mtv_giftNum);
            int giftCount = (int) mtv_giftNum.getTag() + 1; // 递增
            mtv_giftNum.setText("x" + giftCount);
            mtv_giftNum.setTag(giftCount);
            giftNumberAnim.showAnimator(mtv_giftNum);
        }
    }


    /**
     * 发送礼物
     */

    private void sendGift(String tag,int num){
        Gift gift=new Gift();
        gift.setCode(tag);
        gift.setNumber(num);
        gift.setName("礼物");
        gift.setPrice(100);
        LiveMsg msg=new LiveMsg(Constants.headImage,Constants.nickName,"送主播礼物",3);
        Gson gson=new Gson();
        String data=gson.toJson(MessageData.ofMsg(10004,msg,gift));
        WsManager.getInstance().getWs().sendText(data);
    }

    /**
     * 获取礼物
     */
    private View getNewGiftView(String tag,Integer num,String name) {

        // 添加标识, 该view若在layout中存在，就不在生成（用于findViewWithTag判断是否存在）
        View giftView = LayoutInflater.from(myContext).inflate(R.layout.item_gift, null);
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
       if(getActivity()!=null){
           getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   removeGiftView.startAnimation(outAnim);
               }
           });
       }
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
     * 显示软键盘
     */
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_chat, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_chat.getWindowToken(), 0);
    }


    /**
     * 动态的修改listview的高度
     */
    private void dynamicChangeListviewH(int heightPX) {
        ViewGroup.LayoutParams layoutParams = lv_message.getLayoutParams();
        layoutParams.height = DisplayUtil.dip2px(getActivity(), heightPX);
        lv_message.setLayoutParams(layoutParams);
    }

    /**
     * 动态修改礼物父布局的高度
     */
    private void dynamicChangeGiftParentH(boolean showhide) {
        if (showhide) {// 如果软键盘显示中
            if (ll_gift_group.getChildCount() != 0) {

                // 判断是否有礼物显示，如果有就修改父布局高度，如果没有就不作任何操作
                ViewGroup.LayoutParams layoutParams = ll_gift_group.getLayoutParams();
                layoutParams.height = ll_gift_group.getChildAt(0).getHeight();
                ll_gift_group.setLayoutParams(layoutParams);
            }
        } else {
            // 如果软键盘隐藏中
            // 就将装载礼物的容器的高度设置为包裹内容
            ViewGroup.LayoutParams layoutParams = ll_gift_group.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            ll_gift_group.setLayoutParams(layoutParams);
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
                    int num= (int) online.getTag()+1;
                    online.setText(num+"");
                    online.setTag(num);
                    break;
                case UPDATE_SYSYTEM_MSG_SUB:
                    int sub_num= (int) online.getTag()-1;
                    online.setText(sub_num+"");
                    online.setTag(sub_num);
                    break;
                case  UPDATE_GIFT:
                    MessageData code= ( MessageData) msg.obj;
                    if(code.getGift()!=null)
                        showGift(code.getGift().getCode(),code.getMsg().getFansName(),code.getGift().getNumber(),code.getMsg().getUrl());
                    break;
            }
            messageAdapter.NotifyAdapter(messageData);
            lv_message.setSelection(messageData.size());
        }
    };

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }
}
