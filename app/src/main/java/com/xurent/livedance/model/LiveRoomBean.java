package com.xurent.livedance.model;

import java.io.Serializable;
import java.util.Date;

public class LiveRoomBean implements Serializable {


    private Integer id;

    private String username;  //主播ID

    private String title;    //标题

    private String roomImg;   //房间封面

    private String kind;    //分类

    private String announcement;//公告

    private Date date; //时间

    private Integer state=0; //  0未开播 1直播中

    private Integer online=0; //在线人数
    private String nickname; //主播名

    private String anchorImg; //主播头像

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRoomImg() {
        return roomImg;
    }

    public void setRoomImg(String roomImg) {
        this.roomImg = roomImg;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAnchorImg() {
        return anchorImg;
    }

    public void setAnchorImg(String anchorImg) {
        this.anchorImg = anchorImg;
    }

    @Override
    public String toString() {
        return "LiveRoomBean{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", roomImg='" + roomImg + '\'' +
                ", kind='" + kind + '\'' +
                ", announcement='" + announcement + '\'' +
                ", date=" + date +
                ", state=" + state +
                ", online=" + online +
                ", nickname='" + nickname + '\'' +
                ", anchorImg='" + anchorImg + '\'' +
                '}';
    }
}
