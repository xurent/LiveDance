package com.xurent.livedance.push;

import android.content.Context;

import com.xurent.livedance.encodec.BaseMediaEncoder;


public class PushEncodec extends BasePushEncoder {

    private EncodecPushRender encodecPushRender;

    public PushEncodec(Context context, int textrueId) {
        super(context);
        encodecPushRender =new EncodecPushRender(context,textrueId);

        setRender(encodecPushRender);
        setmRenderMode(BaseMediaEncoder.RENDERMODE_CONTINUOUSLY);

    }

    public void setWaterMark(String text){

        encodecPushRender.setWatermark(text);
    }

}
