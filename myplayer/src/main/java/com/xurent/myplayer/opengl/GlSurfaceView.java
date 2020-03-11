package com.xurent.myplayer.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;

import com.xurent.myplayer.listener.MyLaoutCallBackListener;

public class GlSurfaceView extends GLSurfaceView  implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private WlRender wlRender;
    private Context context;
    private boolean listen=true;

    public void setListen(boolean listen) {
        this.listen = listen;
    }

    public GlSurfaceView(Context context) {
        this(context, null);
        this.context=context;
        //setGestureListener();
        //设置Touch监听
        this.setOnTouchListener(this);
        //允许长按
        this.setLongClickable(true);
    }

    public GlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setEGLContextClientVersion(2);
        wlRender = new WlRender(context);
        setRenderer(wlRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        wlRender.setOnRenderListener(new WlRender.OnRenderListener() {
            @Override
            public void onRender() {
                requestRender();

            }
        });

        //setGestureListener();
        //设置Touch监听
        this.setOnTouchListener(this);
        //允许长按
        this.setLongClickable(true);

    }

    public void setYUVData(int width, int height, byte[] y, byte[] u, byte[] v) {
        if (wlRender != null) {
            wlRender.setYUVRenderData(width, height, y, u, v);
            requestRender();
        }
    }

    public WlRender getWlRender() {
        return wlRender;
    }



    private static final int FLING_MIN_DISTANCE = 20;// 移动最小距离
    private static final int FLING_MIN_VELOCITY = 200;// 移动最大速度

    private MyLaoutCallBackListener myLayoutCallBack;
    //构建手势探测器
    GestureDetector mygesture = new GestureDetector(context,this);

    public void setMyLayoutCallBack(MyLaoutCallBackListener myLayoutCallBack) {
        this.myLayoutCallBack = myLayoutCallBack;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("pingan", "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;

    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        // e1：第1个ACTION_DOWN MotionEvent
        // e2：最后一个ACTION_MOVE MotionEvent
        // velocityX：X轴上的移动速度（像素/秒）
        // velocityY：Y轴上的移动速度（像素/秒）

        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
        //向
        if(listen){
            if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE){
//                     && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

                myLayoutCallBack.onDown();
            }
            //向上
            if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                myLayoutCallBack.onUp();
            }
        }
        return false;

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mygesture.onTouchEvent(event);
    }



}
