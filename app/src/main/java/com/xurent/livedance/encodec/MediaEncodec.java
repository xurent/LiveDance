package com.xurent.livedance.encodec;

import android.content.Context;

public class MediaEncodec extends BaseMediaEncoder {

    private EncodecRender encodecRender;

    public MediaEncodec(Context context, int textrueId) {
        super(context);
        encodecRender =new EncodecRender(context,textrueId);

        setRender(encodecRender);
        setmRenderMode(BaseMediaEncoder.RENDERMODE_CONTINUOUSLY);

    }
}
