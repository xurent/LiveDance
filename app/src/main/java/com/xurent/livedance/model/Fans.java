package com.xurent.livedance.model;

import java.io.Serializable;

public class Fans implements Serializable {

    private  String uid;//用户ID

    private  String nickName;

    private  String headImg;

    private long acount;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public long getAcount() {
        return acount;
    }

    public void setAcount(long acount) {
        this.acount = acount;
    }

    @Override
    public String toString() {
        return "Fans{" +
                "uid='" + uid + '\'' +
                ", nickName='" + nickName + '\'' +
                ", headImg='" + headImg + '\'' +
                ", acount=" + acount +
                '}';
    }
}
