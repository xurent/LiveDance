package com.xurent.livedance.websoket;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.xurent.livedance.common.Constants;
import com.xurent.livedance.model.LiveMsg;
import com.xurent.livedance.model.MessageData;
import com.xurent.myplayer.log.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class WsManager {

    public enum WsStatus {
        CONNECT_SUCCESS,//连接成功
        CONNECT_FAIL,//连接失败
        CONNECTING;//正在连接
    }
    private static final int FRAME_QUEUE_SIZE = 5;
    private static final int CONNECT_TIMEOUT = 5000;
    private static WsManager mInstance;
    private WsStatus mStatus;
    private WebSocket ws;
    private WsListener mListener;
    private String url;
    private  Context context;
    private static WsCallBack wsCallBack;
    private static boolean reConnect=true;

    public static  void setReConnect(boolean Connect) {
        reConnect = Connect;
    }

    public static void setWsCallBack(WsCallBack CallBack) {
        wsCallBack = CallBack;
    }

    public  WebSocket getWs() {
        return ws;
    }

    private WsManager() {
    }
    public static WsManager getInstance(){
        if(mInstance == null){
            synchronized (WsManager.class){
                if(mInstance == null){
                    mInstance = new WsManager();
                }
            }
        }
        return mInstance;
    }

    public void init(String url,Context context){
        try {
            /**
             * configUrl其实是缓存在本地的连接地址
             * 这个缓存本地连接地址是app启动的时候通过http请求去服务端获取的,
             * 每次app启动的时候会拿当前时间与缓存时间比较,超过6小时就再次去服务端获取新的连接地址更新本地缓存
             */
            this.url=url;
            this.context=context;
            System.out.println(url);
            ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(mListener = new WsListener())//添加回调监听
                    .connectAsynchronously();//异步连接
            setStatus(WsStatus.CONNECTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class WsListener extends WebSocketAdapter {
        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            MyLog.i("ws:"+text);
            try{
                JSONObject object=new JSONObject(text);
                System.out.println(object==null);
                if(object==null)return;
                if(wsCallBack!=null){
                    wsCallBack.getMessage(object);;
                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }


        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
                throws Exception {
            super.onConnected(websocket, headers);
            setStatus(WsStatus.CONNECT_SUCCESS);
            MyLog.i("ws:"+"连接成功"+headers.toString());
            cancelReconnect();//连接成功的时候取消重连,初始化连接次数
        }


        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception)
                throws Exception {
            super.onConnectError(websocket, exception);
            setStatus(WsStatus.CONNECT_FAIL);
         /*   Dialog dialog=new Dialog(context);
            dialog.setTitle("正在重连...");
            dialog.create();
            dialog.show();*/
            reconnect();//连接错误的时候调用重连方法
        }


        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
                throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            setStatus(WsStatus.CONNECT_FAIL);
            MyLog.i("ws:"+"断开连接");
            MessageData data= MessageData.ofMsg(10002, new LiveMsg(Constants.headImage,Constants.nickName,"感谢您的收看!",1));
            Gson gson=new Gson();
            websocket.sendText(gson.toJson(gson));
            if(reConnect){
                reconnect();//连接断开的时候调用重连方法
            }
        }
    }

    private void setStatus(WsStatus status){
        this.mStatus = status;
    }

    private WsStatus getStatus(){
        return mStatus;
    }

    public void disconnect(){
        if(ws != null)
            ws.disconnect();
    }


    private Handler mHandler = new Handler();

    private int reconnectCount = 0;//重连次数
    private long minInterval = 3000;//重连最小时间间隔
    private long maxInterval = 60000;//重连最大时间间隔


    public void reconnect() {
        if(!reConnect){
            return;
        }
        if (!isNetConnect()) {
            reconnectCount = 0;
            MyLog.d("重连失败网络不可用");
            return;
        }

        //这里其实应该还有个用户是否登录了的判断 因为当连接成功后我们需要发送用户信息到服务端进行校验
        if (ws != null &&
                !ws.isOpen() &&//当前连接断开了
                getStatus() != WsStatus.CONNECTING) {//不是正在重连状态

            reconnectCount++;
            setStatus(WsStatus.CONNECTING);

            long reconnectTime = minInterval;
            if (reconnectCount > 3) {
                long temp = minInterval * (reconnectCount - 2);
                reconnectTime = temp > maxInterval ? maxInterval : temp;
            }
            MyLog.i("准备开始第"+reconnectCount+"次重连,重连间隔"+reconnectTime+"-- url:"+url);

            mHandler.postDelayed(mReconnectTask, reconnectTime);
        }
    }


    private Runnable mReconnectTask = new Runnable() {

        @Override
        public void run() {
            try {
                ws = new WebSocketFactory().createSocket(url, CONNECT_TIMEOUT)
                        .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                        .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                        .addListener(mListener = new WsListener())//添加回调监听
                        .connectAsynchronously();//异步连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private void cancelReconnect() {
        reconnectCount = 0;
        mHandler.removeCallbacks(mReconnectTask);
    }


    private boolean isNetConnect() {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    public interface WsCallBack{

        void getMessage(JSONObject data) throws JSONException;

    }

}
