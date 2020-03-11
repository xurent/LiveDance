package com.xurent.livedance.model;

public class MessageData {

    private int code;
    private LiveMsg msg;
    private Gift gift;

    public MessageData() {
    }

    public MessageData(int code, LiveMsg msg) {
        this.code = code;
        this.msg = msg;
    }

    public MessageData(int code, LiveMsg msg, Gift gift) {
        this.code = code;
        this.msg = msg;
        this.gift = gift;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LiveMsg getMsg() {
        return msg;
    }

    public void setMsg(LiveMsg msg) {
        this.msg = msg;
    }

    public Gift getGift() {
        return gift;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public static MessageData ofMsg(int code, LiveMsg msg){

        return new MessageData( code,msg);
    }

    public static MessageData ofMsg(int code, LiveMsg msg,Gift gift){

        return new MessageData( code,msg,gift);
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "code=" + code +
                ", msg=" + msg +
                ", gift=" + gift +
                '}';
    }
}
