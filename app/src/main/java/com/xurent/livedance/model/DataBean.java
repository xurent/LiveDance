package com.xurent.livedance.model;

import com.xurent.livedance.R;

import java.util.ArrayList;
import java.util.List;

public class DataBean {

    public int imageUrl;

    public DataBean(int url){
        this.imageUrl=url;
    }

    public static List<DataBean> getTestData(){
        List<DataBean> videoList = new ArrayList<>();
       /* videoList.add(new DataBean("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1528317444367&di=97041b2323d954ae560b9990de5f106d&imgtype=0&src=http%3A%2F%2Fbpic.ooopic.com%2F15%2F64%2F82%2F15648210-a9052bd9a5a151bf15833413af06b8c4-3.jpg"));
        videoList.add(new DataBean("http://www.005.tv/templets/muban/images/20191127/350_264.jpg"));
        videoList.add(new DataBean("http://pic.xiaomingming.org/FileUpload/2016101216355362176.jpg"));*/

        videoList.add(new DataBean(R.drawable.banner1));
        videoList.add(new DataBean(R.drawable.banner2));
        videoList.add(new DataBean(R.drawable.banner3));
        videoList.add(new DataBean(R.drawable.banner4));

        return videoList;
    }

    public static List<DataBean> getTestData1(){
        List<DataBean> videoList = new ArrayList<>();
        videoList.add(new DataBean(R.drawable.vrbanner));
        videoList.add(new DataBean(R.drawable.vrbanner1));
        return videoList;
    }

}
