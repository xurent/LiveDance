package com.xurent.livedance.model;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private  String userName;

    private  String nickName;

    private  String phone;

    private  String headImg;

    private  String introduction;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userName='" + userName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", phone='" + phone + '\'' +
                ", headImg='" + headImg + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
