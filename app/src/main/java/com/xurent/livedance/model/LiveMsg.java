package com.xurent.livedance.model;

public class LiveMsg {

    private  String url;
    private  String fansName;
    private  String msg;
    private String uid;
    private  int type=0;


    public LiveMsg() {
    }

    public LiveMsg(String url, String fansName, String msg) {
        this.url = url;
        this.fansName = fansName;
        this.msg = msg;
    }

    public LiveMsg(String url, String fansName, String msg, int type) {
        this.url = url;
        this.fansName = fansName;
        this.msg = msg;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFansName() {
        return fansName;
    }

    public void setFansName(String fansName) {
        this.fansName = fansName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "LiveMsg{" +
                "url='" + url + '\'' +
                ", fansName='" + fansName + '\'' +
                ", msg='" + msg + '\'' +
                ", uid='" + uid + '\'' +
                ", type=" + type +
                '}';
    }
}
