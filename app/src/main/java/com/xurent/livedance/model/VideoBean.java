package com.xurent.livedance.model;

import java.util.Date;

public class VideoBean {

    private String userId;//视频作者ID
    private  String  nickName;//用户名
    private  String  url;//视频地址
    private  int type=0;//视频类型
    private  int like_num;//点赞数量
    private  String date; //时间
    private  String imgUrl;//封面地址


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLike_num() {
        return like_num;
    }

    public void setLike_num(int like_num) {
        this.like_num = like_num;
    }



    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "userId='" + userId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                ", like_num=" + like_num +
                ", date='" + date + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
