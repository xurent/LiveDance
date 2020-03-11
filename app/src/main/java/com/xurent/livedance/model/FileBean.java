package com.xurent.livedance.model;

import java.io.Serializable;
import java.util.Date;

public class FileBean implements Serializable {

    private int id;
    private String userId; //用户ID
    private String nickName;//昵称
    private String fileName;
    private String thumbleImg;//预览图
    private String  url;
    private Integer type; //  0图片  1 视频 2VR视频
    private Date createTime;
    private Integer likeNumber=0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getLikeNumber() {
        return likeNumber;
    }

    public void setLikeNumber(Integer likeNumber) {
        this.likeNumber = likeNumber;
    }

    public String getThumbleImg() {
        return thumbleImg;
    }

    public void setThumbleImg(String thumbleImg) {
        this.thumbleImg = thumbleImg;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", thumbleImg='" + thumbleImg + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                ", createTime=" + createTime +
                ", likeNumber=" + likeNumber +
                '}';
    }
}
