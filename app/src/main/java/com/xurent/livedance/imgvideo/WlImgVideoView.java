package com.xurent.livedance.imgvideo;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.xurent.livedance.egl.EGLSurfaceView;

public class WlImgVideoView extends EGLSurfaceView {

    private WlImgVideoRender wlImgVideoRender;
    private int fbotextureid;

    public WlImgVideoView(Context context) {
        this(context,null);
    }

    public WlImgVideoView(Context context, AttributeSet attrs) {
       this(context, attrs,0);
    }

    public WlImgVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlImgVideoRender=new WlImgVideoRender(context);
        setRender(wlImgVideoRender);
        setRenderMode(EGLSurfaceView.RENDERMODE_WHEN_DIRTY);
        wlImgVideoRender.setOnRenderCreateListener(new WlImgVideoRender.OnRenderCreateListener() {
            @Override
            public void onCreate(int textid) {
                fbotextureid = textid;
            }
        });
    }

//id
    public void setCurrentImg(int imgsr)
    {
        if(wlImgVideoRender != null)
        {
            wlImgVideoRender.setCurrentImgSrc(imgsr);
            requestRender();
        }
    }
//bitmap
    public void setCurrentImg(Bitmap imgsr)
    {
        if(wlImgVideoRender != null)
        {
            wlImgVideoRender.setCurrentImgSrc(imgsr);
            requestRender();
        }
    }


    public int getFbotextureid() {
        return fbotextureid;
    }
}
