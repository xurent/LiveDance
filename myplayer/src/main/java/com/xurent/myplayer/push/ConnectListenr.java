package com.xurent.myplayer.push;

public interface ConnectListenr {

    void onConnecting();

    void onConnectSuccess();

    void onConnectFail(String msg);

}
