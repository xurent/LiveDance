package com.xurent.myplayer.push;

import android.text.TextUtils;

public class PushVideo {

    private ConnectListenr connectListenr;
    static {
        System.loadLibrary("native-lib");
    }


    public void setConnectListenr(ConnectListenr connectListenr) {
        this.connectListenr = connectListenr;
    }


    private void onConnecting()
    {
        if(connectListenr != null)
        {
            connectListenr.onConnecting();
        }
    }

    private void onConnectSuccess()
    {
        if(connectListenr != null)
        {
            connectListenr.onConnectSuccess();
        }
    }

    private void onConnectFail(String msg)
    {
        if(connectListenr != null)
        {
            connectListenr.onConnectFail(msg);
        }
    }


    public void initLivePush(String url){
        if(!TextUtils.isEmpty(url)){
            initPush(url);
            System.out.println("输入直播网址");
        }
    }

    public void pushSPSPPS(byte[] sps,byte[] pps){

        if(sps!=null&&pps!=null){
            pushSPSPPS(sps,sps.length,pps,pps.length);
        }
    }

    public void pushVideoData(byte[] data,boolean keyframe){

        if(data!=null){
            pushVideoData(data,data.length,keyframe);
        }

    }

    public void pushAudioData(byte[] data)
    {
        if(data != null)
        {
            pushAudioData(data, data.length);

        }
    }

    public void stopPush()
    {
        pushStop();
    }


    private native void initPush(String pushUrl);

    private native  void pushSPSPPS(byte[] sps,int sps_len,byte[] pps,int pps_len);

    private  native  void pushVideoData(byte[] data,int data_len,boolean keyframe);

    private native void pushAudioData(byte[] data, int data_len);

    private native void pushStop();
}
