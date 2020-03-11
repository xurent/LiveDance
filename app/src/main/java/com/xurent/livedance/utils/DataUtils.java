package com.xurent.livedance.utils;

import com.xurent.livedance.model.Bean;

import java.util.ArrayList;

public class DataUtils {
    public static ArrayList<Bean> getDatas() {
        ArrayList<Bean> videoList = new ArrayList<>();
        videoList.add(new Bean("http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4"));
        videoList.add(new Bean("http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"));
        videoList.add(new Bean("http://video.newsapp.cnr.cn/data/video/2019/27675/index.m3u8"));
        videoList.add(new Bean("http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4"));
        videoList.add(new Bean("http://vfx.mtime.cn/Video/2019/03/14/mp4/190314223540373995.mp4"));
        return videoList;
    }

    public static ArrayList<Bean> getVRDatas() {
        ArrayList<Bean> videoList = new ArrayList<>();
        videoList.add(new Bean("http://cnvod.cnr.cn/audio2017/ondemand/transcode/l_target/wcm_system/video/20190403/xw0219xwyt22_56/index.m3u8"));
        videoList.add(new Bean("http://video.newsapp.cnr.cn/data/video/2019/27675/index.m3u8"));
        videoList.add(new Bean("http://7778-wx-nopa-3ea34a-1259340099.tcb.qcloud.la/2.mp4?sign=05888340e80f062212e84b0dd28e743f&t=1581408164"));
        videoList.add(new Bean("http://7778-wx-nopa-3ea34a-1259340099.tcb.qcloud.la/1.mp4?sign=5183500d128583b01efd91b7507c9287&t=1581415106"));
        videoList.add(new Bean("https://7778-wx-nopa-3ea34a-1259340099.tcb.qcloud.la/3.mp4?sign=479c8a8b6aca5f8699a3b8b813ece334&t=1581415217"));
        return videoList;
    }
}
